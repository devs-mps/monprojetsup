package fr.gouv.monprojetsup.data;

import fr.gouv.monprojetsup.data.config.DataServerConfig;
import fr.gouv.monprojetsup.data.distances.Distances;
import fr.gouv.monprojetsup.data.model.cities.CitiesBack;
import fr.gouv.monprojetsup.data.model.formations.Formation;
import fr.gouv.monprojetsup.data.model.specialites.Specialites;
import fr.gouv.monprojetsup.data.model.specialites.SpecialitesLoader;
import fr.gouv.monprojetsup.data.model.stats.PsupStatistiques;
import fr.gouv.monprojetsup.data.model.stats.StatsContainers;
import fr.gouv.monprojetsup.data.model.tags.TagsSources;
import fr.gouv.monprojetsup.data.update.BackEndData;
import fr.gouv.monprojetsup.data.update.onisep.DomainePro;
import fr.gouv.monprojetsup.data.update.onisep.OnisepData;
import fr.gouv.monprojetsup.data.update.psup.PsupData;
import fr.gouv.monprojetsup.data.tools.Serialisation;
import fr.gouv.parcoursup.carte.modele.modele.JsonCarte;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static fr.gouv.monprojetsup.data.Constants.FORMATION_PREFIX;
import static fr.gouv.monprojetsup.data.Helpers.isFiliere;
import static fr.parcoursup.carte.algos.Filiere.LAS_CONSTANT;


@Slf4j
public class ServerData {


    /***************************************************************************
     ******************* DATAS ***********************************
     ****************************************************************************/

    public static PsupData backPsupData;
    public static PsupStatistiques statistiques;
    public static OnisepData onisepData;
    public static JsonCarte carte;
    public static TagsSources tagsSources;

    public static Map<String, Set<String>> liensSecteursMetiers;
    public static Map<DomainePro, Set<String>> liensDomainesMetiers;


    public static final Map<String, Set<String>> reverseFlGroups = new HashMap<>();

    public static Specialites specialites;
    public static Map<String, Integer> codesSpecialites = new HashMap<>();

    //regroupement des filieres
    public static Map<String, String> flGroups = null;

    protected static final Map<String, List<Formation>> filToFormations = new HashMap<>();

    public static CitiesBack cities = null;
    /*
    ************************************************************************
    **************************** LOADERS ***********************************
    ************************************************************************
     */

    private static boolean dataLoaded = false;


    /**
     * Load data into server
     * @throws IOException unlucky
     */
    public static synchronized void load() throws IOException {

        if(dataLoaded) return;

        DataServerConfig.load();

        log.info("Loading server data...");

        loadBackEndData();

        flGroups = new HashMap<>(backPsupData.getCorrespondances());
        flGroups.forEach((s, s2) -> reverseFlGroups.computeIfAbsent(s2, z -> new HashSet<>()).add(s));

        ServerData.specialites = SpecialitesLoader.load();
        ServerData.specialites.specialites().forEach((iMtCod, s) -> ServerData.codesSpecialites.put(s, iMtCod));

        ServerData.statistiques = Serialisation.fromZippedJson(DataSources.getSourceDataFilePath(DataSources.STATS_BACK_SRC_FILENAME), PsupStatistiques.class);
        ServerData.statistiques.removeSmallPopulations();

        ServerData.statistiques =
                Serialisation.fromZippedJson(
                        DataSources.getSourceDataFilePath(DataSources.STATS_BACK_SRC_FILENAME),
                        PsupStatistiques.class
                );
        /* can be deleted afte rnext data update */
        ServerData.statistiques.rebuildMiddle50();
        ServerData.statistiques.createGroupAdmisStatistique(reverseFlGroups);
        ServerData.statistiques.createGroupAdmisStatistique(getLasToGtaMapping());

        ServerData.updateLabelsForDebug();

        liensSecteursMetiers  = OnisepData.getSecteursVersMetiers(
                onisepData.fichesMetiers(),
                onisepData.formations().getFormationsDuSup()
        );
        liensDomainesMetiers  = OnisepData.getDomainesVersMetiers(onisepData.metiers());

        Distances.init();

        tagsSources = TagsSources.load(backPsupData.getCorrespondances());

        dataLoaded = true;
    }

    private static void loadBackEndData() throws IOException {

        BackEndData backendData = Serialisation.fromZippedJson(DataSources.getBackDataFilePath(), BackEndData.class);

        ServerData.onisepData = backendData.onisepData();
        ServerData.carte = backendData.carte();

        backPsupData = backendData.psupData();
        backPsupData.cleanup();//should be useless but it does not harm...

        val groupes = backPsupData.getCorrespondances();

        backPsupData.formations().formations.values()
                .forEach(f -> {
                    int gFlCod = (f.isLAS() && f.gFlCod < LAS_CONSTANT) ? f.gFlCod + LAS_CONSTANT: f.gFlCod;
                    String key = Constants.gFlCodToFrontId(gFlCod);
                    filToFormations
                            .computeIfAbsent(key, z -> new ArrayList<>())
                            .add(f);
                    if(groupes.containsKey(key)) {
                        filToFormations
                                .computeIfAbsent(groupes.get(key), z -> new ArrayList<>())
                                .add(f);

                    }
                });

        ServerData.cities = new CitiesBack(backendData.cities().cities());

    }

    private static Map<String, Set<String>> getLasToGtaMapping() {
        //fl1002033
        Set<String> lasCodes = ServerData.statistiques.getLASCorrespondance().lasToGeneric().keySet();
        return
                lasCodes
                        .stream()
                        .collect(Collectors.toMap(
                                        las -> las,
                                        las -> filToFormations.getOrDefault(las, List.of())
                                                .stream()
                                                .map(f ->  FORMATION_PREFIX + f.gTaCod)
                                                .collect(Collectors.toSet())
                                )
                        );
    }

    public static String getGroupOfFiliere(String fl) {
        return flGroups.getOrDefault(fl,fl);
    }
    public static Set<String> getFilieresOfGroup(String fl) {
        return  reverseFlGroups.getOrDefault(fl, Set.of(fl));
    }

    private static void updateLabelsForDebug() {
        statistiques.updateLabels(onisepData, backPsupData, statistiques.getLASCorrespondance().lasToGeneric());
        Map<String, String> suffixes =
                reverseFlGroups.entrySet().stream()
                        .filter(e -> !e.getValue().isEmpty())
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        s -> s.getValue().toString())
                                );
        suffixes.forEach((key, suffix) -> statistiques.labels.put(key, statistiques.labels.get(key) + " groupe " + suffix) );
        try {
            Serialisation.toJsonFile("labelsDebug.json", statistiques.labels, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /* ************************************************************************
    ************************* HELPERS to get labels associated with a key ***********************
     */
    public static String getLabel(String key) {
        return ServerData.statistiques.labels.getOrDefault(
                key,
                ServerData.statistiques.nomsFilieres.get(key)
        );
    }

    public static String getDebugLabel(String key) {
        return getLabel(key) + " (" + key  + ")";
    }

    public static String getLabel(String key, String defaultValue) {
        return ServerData.statistiques.labels.getOrDefault(key, defaultValue);
    }


    /*
    ***********************************************
    ************* STATS HELPERS *******************
    ************************************************/

    /**
     * utilisé pour l'envoi des stats aux profs
     * @param bac le bac
     * @param groups les groupes
     * @return les stats
     */
    public static Map<String, StatsContainers.DetailFiliere> getGroupStats(@Nullable String bac, @Nullable Collection<String> groups) {
        if(groups == null) return Collections.emptyMap();
        if(bac == null) bac = PsupStatistiques.TOUS_BACS_CODE;
        @Nullable String finalBac = bac;
        return groups.stream().collect(Collectors.toMap(
                g -> g,
                g -> getDetailedGroupStats(finalBac, g)
        ));
    }

    /**
     * utilisé pour l'envoi des stats aux élèves
     *
     * @param bac le bac
     * @param g le groupe
     * @return les détails
     */
    public static @NotNull StatsContainers.SimpleStatGroupParBac getSimpleGroupStats(@Nullable String bac, String g) {
        if(bac == null) bac = PsupStatistiques.TOUS_BACS_CODE;
        return getDetailedGroupStats(bac, g, false).stat();
    }

    /**
     * utilisé pour l'envoi des stats aux profs
     * @param bac le bac
     * @param g le groupe
     * @return les stats
     */
    private static StatsContainers.DetailFiliere getDetailedGroupStats(@NotNull String bac, String g) {
        return getDetailedGroupStats(bac, g, true);
    }
    private static StatsContainers.DetailFiliere getDetailedGroupStats(@NotNull String bac, String g, boolean includeProfDetails) {
        StatsContainers.SimpleStatGroupParBac statFil
                = new StatsContainers.SimpleStatGroupParBac(
                        statistiques.getGroupStats(
                                g,
                                bac,
                                !includeProfDetails
                        )
        );
        //statFil.stats().entrySet().removeIf(e -> e.getValue().nbAdmis() == null);

        if(includeProfDetails) {
            Map<String, StatsContainers.DetailFormation> statsFormations = new HashMap<>();
            try {
                List<Formation> fors = getFormationsFromFil(g);
                fors.forEach(f -> {
                    try {
                        String fr = FORMATION_PREFIX + f.gTaCod;
                        StatsContainers.SimpleStatGroupParBac statFor = new StatsContainers.SimpleStatGroupParBac(
                                statistiques.getGroupStats(
                                        fr,
                                        bac,
                                        !includeProfDetails)
                        );
                        statFor.stats().entrySet().removeIf(e -> e.getValue().statsScol().isEmpty());
                        statsFormations.put(fr, new StatsContainers.DetailFormation(
                                f.libelle,
                                fr,
                                statFor
                        ));
                    } catch (Exception ignored) {
                        //ignored
                    }
                });
            } catch (Exception ignored) {
                //ignore
            }
            return new StatsContainers.DetailFiliere(
                    g,
                    statFil,
                    statsFormations
            );
        } else {
            return new StatsContainers.DetailFiliere(
                    g,
                    statFil,
                    null
            );
        }
    }

    /**
     * Get the list of formations for a filiere. Used by the suggestion service to compute foi (formations of interest).
     * @param fl the filiere
     * @return the list of formations
     */
    public static List<Formation> getFormationsFromFil(String fl) {
        return filToFormations
                .getOrDefault(fl, Collections.emptyList());
    }

    private ServerData() {

    }

    public static Set<String> search(String searchString) {

        List<String> tags = Arrays.stream(
                searchString.replaceAll("[-,_()]", " ").split(" ")
        ).filter(s -> !s.isBlank()).toList();

        if(tags.isEmpty()) {
            return tagsSources.sources().values().stream().flatMap(Set::stream).filter(s -> isFiliere(s)).collect(Collectors.toSet());
        }
        boolean allformationsandNoMetier = tags.isEmpty();


        return tagsSources.getPrefixMatches(tags);
    }
}
