package fr.gouv.monprojetsup.app.services.info;

import com.google.gson.Gson;
import fr.gouv.monprojetsup.app.server.MyService;
import fr.gouv.monprojetsup.data.Helpers;
import fr.gouv.monprojetsup.data.dto.*;
import fr.gouv.monprojetsup.common.server.ResponseHeader;
import fr.gouv.monprojetsup.data.ServerData;
import fr.gouv.monprojetsup.data.distances.Distances;
import fr.gouv.monprojetsup.data.dto.GetFormationsAffinitiesServiceDTO.Affinity;
import fr.gouv.monprojetsup.data.model.Explanation;
import fr.gouv.monprojetsup.data.model.stats.StatsContainers;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

import static fr.gouv.monprojetsup.data.distances.Distances.getGeoExplanations;
import static java.lang.Math.min;
import static org.springframework.http.RequestEntity.post;

@Service
public class RechercheService extends MyService<RechercheService.Request, RechercheService.Response> {

    protected static boolean USE_LOCAL_URL = true;

    public static final String LOCAL_URL = "http://localhost:8004/";
    public static final String REMOTE_URL = "https://monprojetsup.fr/";


    public RechercheService() {
        super(Request.class);
    }

    public record Request(

            @Schema(description = "Recherche de formation.")
            boolean includeFormations,

            @Schema(description = "Recherche de metier.")
            boolean includeMetiers,

            @Schema(description = "Taille de page.")
            int pageSize,

            @Schema(description = "Numéro de page.")
            int pageNb,

            @Schema(description = "Contenu de la barre de recherche.")
            @NotNull String recherche,

            @Schema(description = "Profil utilisé pour générer les détails.")
            @NotNull ProfileDTO profile

    ) {

    }

    public record ResultatRecherche(
            @Schema(description = "clé", example = "fl2014")
            @NotNull String key,
            @Schema(description = "affinite entre 0.0 et 1.0, arrondie à 6 décimales", example = "0.8")
            double affinity,
            @Schema(description = "type", example = "formation", allowableValues = {"formation", "metier"})
            @NotNull String type,
            @ArraySchema(arraySchema = @Schema(description = "formations d'intérêt", example = "[\"ta201\",\"ta123\"]"))
            @NotNull List<String> fois,
            @ArraySchema(arraySchema = @Schema(description = "villes disponibles, triées par affinité décroissantes", example = "[\"Nantes\",\"Melun\"]"))
            @NotNull List<String> cities,
            @Schema(description = "statistiques 'admission",  required = true)
            @NotNull StatsContainers.SimpleStatGroupParBac stats,

            @ArraySchema(arraySchema = @Schema(description = "explications", allOf = Explanation.class))
            @NotNull List<Explanation> explanations,
            @ArraySchema(arraySchema = @Schema(description = "examples de métiers, triés par affinité décroissante", example = "[\"met_129\",\"met_84\",\"met_5\"]"))
            @NotNull List<String> examples

    ) {
    }
    public record Response(
            ResponseHeader header,
            @Schema(
                    name = "details",
                    description =
                            """
                               Permet de récupérer des détails sur une liste de formations, en fonction d'un profil.
                               Chaque détail inclut:
                               * des explications sur les éléments de proximité entre le profil et la formation
                               * des exemples de métiers associées, triés par affinité décroissante
                               * des lieux de formations d'intérêts, proches des villes d'intérêts du profil
                               * des villes disponibles pour faire cette formation, triées par ordre décroissant de disctance avec les villes d'intérêts du profil
                               * des stats sur la formation, adaptées au type de bac du profil
                               """,
                    required = true
            )
            List<ResultatRecherche> details
    ) {

        public Response(@NotNull List<ResultatRecherche>  suggestions) {
            this(
                    new ResponseHeader(),
                    suggestions
            );
        }

    }

    @Override
    protected @NotNull Response handleRequest(@NotNull RechercheService.Request req) throws Exception {

        Set<String> allKeys = ServerData.search(req.recherche);

        Set<String> keysFormations
                = req.includeFormations ? allKeys.stream().filter(Helpers::isFiliere).collect(Collectors.toSet()) : Set.of();

        Set<String> keysMetiers
                = req.includeMetiers
                ? allKeys.stream().filter(Helpers::isMetier).collect(Collectors.toSet())
                : Set.of();

        //equivalent d'un appel à /affinite/formations
        final List<Affinity> affinites = getFormationsAffinities(
                req.profile
        );
        Queue<String> keysf = new LinkedList<>(
                affinites.stream()
                        .map(p -> p.key())
                        .filter(key -> keysFormations.contains(key))
                        .toList());


        //LOGGER.info("HAndling request " + req);
        final @NotNull Queue<String> keysm = new LinkedList<>(sortMetiersByAffinites(
                req.profile,
                keysMetiers
        ));

        List<String> keys = new ArrayList<>();
        while(!keysf.isEmpty() || !keysm.isEmpty()) {
            if(!keysf.isEmpty()) keys.add(keysf.remove());
            if(!keysm.isEmpty()) keys.add(keysm.remove());
        }

        List<String> keysPage = keys.stream().skip(req.pageNb * req.pageSize).limit(req.pageSize).toList();

        //LOGGER.info("HAndling request " + req);
        final @NotNull List<ResultatRecherche> suggestions = getDetails(
                req.profile,
                keysPage,
                affinites.stream().collect(Collectors.toMap(Affinity::key, Affinity::affinite))
        );

        return new RechercheService.Response(suggestions);
    }

    static private List<String> sortMetiersByAffinites(ProfileDTO profile, Set<String> keysMetiers) throws IOException, InterruptedException {
        if(keysMetiers.isEmpty()) return List.of();
        val request = new SortMetiersByAffinityServiceDTO.Request(profile, keysMetiers.stream().toList());
        String responseJson = post((USE_LOCAL_URL ? LOCAL_URL : REMOTE_URL) + "api/1.1/affinite/metiers", request);
        val response = new Gson().fromJson(responseJson, SortMetiersByAffinityServiceDTO.Response.class);
        return response.clesTriees();
    }

    //
    static private List<Affinity> getFormationsAffinities(ProfileDTO profile) throws IOException, InterruptedException {
        val request = new GetFormationsAffinitiesServiceDTO.Request(profile);
        String responseJson = post((USE_LOCAL_URL ? LOCAL_URL : REMOTE_URL) + "api/1.1/affinite/formations", request);
        val response = new Gson().fromJson(responseJson, GetFormationsAffinitiesServiceDTO.Response.class);
        return response.affinites();
    }

    static private @NotNull List<GetExplanationsAndExamplesServiceDTO.ExplanationAndExamples> getExplanationsandExamples(
            ProfileDTO profile,
            List<String> keys)
            throws IOException, InterruptedException {
        val request = new GetExplanationsAndExamplesServiceDTO.Request(profile, keys);
        String responseJson = post((USE_LOCAL_URL ? LOCAL_URL : REMOTE_URL) + "api/1.1/explanations", request);
        val response = new Gson().fromJson(responseJson, GetExplanationsAndExamplesServiceDTO.Response.class);
        return response.liste();
    }


    private static synchronized String post(String url, Object obj) throws IOException, InterruptedException {
        String requestBody = new Gson().toJson(obj);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response =
                HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            throw new RuntimeException("Error: " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }

    /**
     * Get all details about some formations
     *
     * @param pf        the profile
     * @param keys      the list of keys for which details are required
     * @param affinites the affinities
     * @return the details
     */
    public static List<ResultatRecherche> getDetails(
            ProfileDTO pf,
            List<String> keys,
            @NotNull Map<String, Double> affinites
    ) throws IOException, InterruptedException {

        List<ResultatRecherche> result = new ArrayList<>();

        for (String key : keys) {/* formations of interest */
            val fois = getGeographicInterests(
                    List.of(key),
                    pf.geo_pref(),
                    2
            ).stream().map(ExplanationGeo::form).toList();

            /* cities */
            val citiesDistances = new HashMap<String, Integer>();
            getGeographicInterests(
                    List.of(key),
                    pf.geo_pref(),
                    Integer.MAX_VALUE
            ).forEach(e ->
                    citiesDistances.put(
                            e.city(),
                            min(citiesDistances.getOrDefault(e.city(), Integer.MAX_VALUE), e.distance())
                    )
            );

            val cities = Distances.getCities(key, pf.geo_pref());//citiesDistances.keySet().stream().sorted(Comparator.comparing(citiesDistances::get)).toList();

            val stats = ServerData.getSimpleGroupStats(
                    pf.bac(),
                    key
            );

            val eae = getExplanationsandExamples(pf, List.of(key));
            if(eae.size() != 1) throw new RuntimeException("Error: " + eae.size() + " explanations for " + key + " " + eae);
            val eaee = eae.get(0);

            result.add(
                    new ResultatRecherche(
                            key,
                            affinites.getOrDefault(key, 0.0),
                            Helpers.isFiliere(key) ? "formation" : "metier",
                            fois,
                            cities,
                            stats,
                            eaee.explanations(),
                            eaee.examples()
                    )
            );
        }

        return result;

    }

    public static List<ExplanationGeo> getGeographicInterests(@Nullable List<String> flKeys, @Nullable Set<String> cities, int maxFormationsPerFiliere) {
        if(flKeys == null || cities == null || flKeys.isEmpty() || cities.isEmpty()) return Collections.emptyList();
        return cities.stream().flatMap(city ->
                        flKeys.stream()
                                .flatMap(key -> ServerData.reverseFlGroups.containsKey(key)
                                        ? getGeoExplanations(ServerData.reverseFlGroups.get(key), city, maxFormationsPerFiliere).stream()
                                        : getGeoExplanations(List.of(key), city, maxFormationsPerFiliere).stream()
                                )
                ).filter(Objects::nonNull)
                .sorted(Comparator.comparing(ExplanationGeo::distance))
                .toList();
    }


}