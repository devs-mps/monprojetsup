package fr.gouv.monprojetsup.recherche.application.controller

import fr.gouv.monprojetsup.recherche.application.dto.CriteresAdmissionDTO
import fr.gouv.monprojetsup.recherche.application.dto.ExplicationGeographiqueDTO
import fr.gouv.monprojetsup.recherche.application.dto.ExplicationsDTO
import fr.gouv.monprojetsup.recherche.application.dto.FormationDTO
import fr.gouv.monprojetsup.recherche.application.dto.FormationDetailleDTO
import fr.gouv.monprojetsup.recherche.application.dto.MetierDetailleDTO
import fr.gouv.monprojetsup.recherche.application.dto.MoyenneGeneraleDTO
import fr.gouv.monprojetsup.recherche.application.dto.RechercheFormationReponseDTO
import fr.gouv.monprojetsup.recherche.application.dto.RechercheFormationRequeteDTO
import fr.gouv.monprojetsup.recherche.application.dto.RecupererFormationReponseDTO
import fr.gouv.monprojetsup.recherche.application.dto.RecupererFormationRequeteDTO
import fr.gouv.monprojetsup.recherche.usecase.RecupererFormationService
import fr.gouv.monprojetsup.recherche.usecase.SuggestionsFormationsService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("api/v1/formations")
@RestController
class RechercheController(
    val suggestionsFormationsService: SuggestionsFormationsService,
    val recupererFormationService: RecupererFormationService,
) {
    @PostMapping("/recherche")
    fun postRecherche(
        @RequestBody rechercheFormationRequeteDTO: RechercheFormationRequeteDTO,
    ): RechercheFormationReponseDTO {
        val formationsPourProfil =
            suggestionsFormationsService.suggererFormations(
                profilEleve = rechercheFormationRequeteDTO.profil.toProfil(),
                deLIndex = 0,
                aLIndex = NOMBRE_FORMATIONS_SUGGEREES,
            )
        return RechercheFormationReponseDTO(
            formations =
                formationsPourProfil.map { formationPourProfil ->
                    FormationDTO(
                        id = formationPourProfil.id,
                        nom = formationPourProfil.nom,
                        tauxAffinite = formationPourProfil.tauxAffinite,
                        villes = formationPourProfil.communesTrieesParAffinites,
                        metiers = formationPourProfil.metiersTriesParAffinites,
                    )
                },
        )
    }

    @PostMapping("/{idformation}")
    fun postFormation(
        @PathVariable("idformation") idFormation: String,
        @RequestBody recupererFormationRequeteDTO: RecupererFormationRequeteDTO,
    ): RecupererFormationReponseDTO {
        val formation =
            recupererFormationService.recupererFormation(
                profilEleve = recupererFormationRequeteDTO.profil?.toProfil(),
                idFormation = idFormation,
            )
        return RecupererFormationReponseDTO(
            formation =
                FormationDetailleDTO(
                    id = formation.id,
                    nom = formation.nom,
                    formationsAssociees = formation.formationsAssociees?.takeUnless { it.isEmpty() },
                    descriptifFormation = formation.descriptifFormation,
                    descriptifDiplome = formation.descriptifDiplome,
                    descriptifAttendus = formation.descriptifAttendus,
                    criteresAdmission =
                        formation.criteresAdmission?.let { criteresAdmission ->
                            CriteresAdmissionDTO(
                                principauxPoints = criteresAdmission.principauxPoints?.takeUnless { it.isEmpty() },
                                moyenneGenerale =
                                    criteresAdmission.moyenneGenerale?.let { moyenneGenerale ->
                                        MoyenneGeneraleDTO(
                                            moyenneGenerale.centille5eme,
                                            moyenneGenerale.centille25eme,
                                            moyenneGenerale.centille75eme,
                                            moyenneGenerale.centille95eme,
                                        )
                                    },
                            )
                        },
                    descriptifConseils = formation.descriptifConseils,
                    liens = formation.liens?.takeUnless { it.isEmpty() },
                    villes = formation.communes.takeUnless { it.isEmpty() },
                    metiers =
                        formation.metiers.map { metier ->
                            MetierDetailleDTO(
                                id = metier.id,
                                nom = metier.nom,
                                descriptif = metier.descriptif,
                                liens = metier.liens?.takeUnless { it.isEmpty() },
                            )
                        }.takeUnless { it.isEmpty() },
                    tauxAffinite = formation.tauxAffinite,
                ),
            explications =
                formation.explications?.let { explications ->
                    ExplicationsDTO(
                        geographique =
                            explications.geographique?.map {
                                ExplicationGeographiqueDTO(nom = it.ville, distanceKm = it.distanceKm)
                            }?.takeUnless { it.isEmpty() },
                        similaires = explications.similaires?.takeUnless { it.isEmpty() },
                        dureeEtudesPrevue = explications.dureeEtudesPrevue,
                        alternance = explications.alternance,
                        interets = explications.interets,
                    )
                },
        )
    }

    companion object {
        private const val NOMBRE_FORMATIONS_SUGGEREES = 50
    }
}
