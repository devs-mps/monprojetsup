package fr.gouv.monprojetsup.recherche.application.dto

import fr.gouv.monprojetsup.recherche.domain.entity.AffiniteSpecialite
import fr.gouv.monprojetsup.recherche.domain.entity.CriteresAnalyseCandidature
import fr.gouv.monprojetsup.recherche.domain.entity.Domaine
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationGeographique
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationsSuggestionDetaillees
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.FicheFormationPourProfil.ExplicationAutoEvaluationMoyenne
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.FicheFormationPourProfil.ExplicationTypeBaccalaureat
import fr.gouv.monprojetsup.recherche.domain.entity.Formation
import fr.gouv.monprojetsup.recherche.domain.entity.InteretSousCategorie
import fr.gouv.monprojetsup.recherche.domain.entity.MetierDetaille
import fr.gouv.monprojetsup.recherche.domain.entity.StatistiquesDesAdmis.MoyenneGeneraleDesAdmis
import fr.gouv.monprojetsup.recherche.domain.entity.StatistiquesDesAdmis.MoyenneGeneraleDesAdmis.Centile
import fr.gouv.monprojetsup.recherche.domain.entity.StatistiquesDesAdmis.RepartitionAdmis
import fr.gouv.monprojetsup.recherche.domain.entity.StatistiquesDesAdmis.RepartitionAdmis.TotalAdmisPourUnBaccalaureat

data class RecupererFormationReponseDTO(
    val formation: FormationDetailleDTO,
    val explications: ExplicationsDTO?,
) {
    data class FormationDetailleDTO(
        val id: String,
        val nom: String,
        val idsFormationsAssociees: List<String>,
        val descriptifFormation: String?,
        val descriptifDiplome: String?,
        val descriptifConseils: String?,
        val descriptifAttendus: String?,
        val moyenneGeneraleDesAdmis: MoyenneGeneraleDesAdmisDTO?,
        val criteresAnalyseCandidature: List<CriteresAnalyseCandidatureDTO>,
        val repartitionAdmisAnneePrecedente: RepartitionAdmisAnneePrecedenteDTO?,
        val liens: List<LiensDTO>,
        val villes: List<String>,
        val metiers: List<MetierDetailleDTO>,
        val tauxAffinite: Int?,
    ) {
        constructor(ficheFormation: FicheFormation) : this(
            id = ficheFormation.id,
            nom = ficheFormation.nom,
            idsFormationsAssociees = ficheFormation.formationsAssociees ?: emptyList(),
            descriptifFormation = ficheFormation.descriptifGeneral,
            descriptifDiplome = ficheFormation.descriptifDiplome,
            descriptifAttendus = ficheFormation.descriptifAttendus,
            moyenneGeneraleDesAdmis =
                when (ficheFormation) {
                    is FicheFormation.FicheFormationPourProfil ->
                        ficheFormation.statistiquesDesAdmis.moyenneGeneraleDesAdmis?.let {
                            MoyenneGeneraleDesAdmisDTO(it)
                        }

                    is FicheFormation.FicheFormationSansProfil -> null
                },
            descriptifConseils = ficheFormation.descriptifConseils,
            liens = emptyList(), // TODO #66
            villes = ficheFormation.communes,
            metiers =
                ficheFormation.metiers.map { metier ->
                    MetierDetailleDTO(metier)
                },
            tauxAffinite =
                when (ficheFormation) {
                    is FicheFormation.FicheFormationPourProfil -> ficheFormation.tauxAffinite
                    is FicheFormation.FicheFormationSansProfil -> null
                },
            repartitionAdmisAnneePrecedente =
                RepartitionAdmisAnneePrecedenteDTO(
                    ficheFormation.statistiquesDesAdmis.repartitionAdmis,
                ),
            criteresAnalyseCandidature =
                ficheFormation.criteresAnalyseCandidature.map {
                    CriteresAnalyseCandidatureDTO(it)
                },
        )

        data class LiensDTO(
            val nom: String,
            val url: String,
        )

        data class CriteresAnalyseCandidatureDTO(
            val nom: String,
            val pourcentage: Int,
        ) {
            constructor(criteresAnalyseCandidature: CriteresAnalyseCandidature) :
                this(
                    criteresAnalyseCandidature.nom,
                    criteresAnalyseCandidature.pourcentage,
                )
        }

        data class RepartitionAdmisAnneePrecedenteDTO(
            val total: Int,
            val parBaccalaureat: List<TotalAdmisPourUnBaccalaureatDTO>,
        ) {
            constructor(repartitionAdmis: RepartitionAdmis) : this(
                repartitionAdmis.total,
                repartitionAdmis.parBaccalaureat.map {
                    TotalAdmisPourUnBaccalaureatDTO(it)
                },
            )

            data class TotalAdmisPourUnBaccalaureatDTO(
                val baccalaureat: BaccalaureatDTO,
                val nombreAdmis: Int,
            ) {
                constructor(totalAdmisPourUnBaccalaureat: TotalAdmisPourUnBaccalaureat) :
                    this(
                        baccalaureat = BaccalaureatDTO(totalAdmisPourUnBaccalaureat.baccalaureat),
                        nombreAdmis = totalAdmisPourUnBaccalaureat.nombreAdmis,
                    )
            }
        }

        data class MetierDetailleDTO(
            val id: String,
            val nom: String,
            val descriptif: String?,
            val liens: List<LiensDTO>,
        ) {
            constructor(metier: MetierDetaille) : this(
                id = metier.id,
                nom = metier.nom,
                descriptif = metier.descriptif,
                liens = emptyList(), // TODO #66
            )
        }

        data class MoyenneGeneraleDesAdmisDTO(
            val baccalaureat: BaccalaureatDTO?,
            val centiles: List<CentileDTO>,
        ) {
            constructor(moyenneGeneraleDesAdmis: MoyenneGeneraleDesAdmis) :
                this(
                    baccalaureat = moyenneGeneraleDesAdmis.baccalaureat?.let { BaccalaureatDTO(it) },
                    centiles = moyenneGeneraleDesAdmis.centiles.map { CentileDTO(it) },
                )

            data class CentileDTO(
                val centile: Int,
                val note: Float,
            ) {
                constructor(centile: Centile) :
                    this(
                        centile = centile.centile,
                        note = centile.note,
                    )
            }
        }
    }

    data class ExplicationsDTO(
        val geographique: List<ExplicationGeographiqueDTO>,
        val formationsSimilaires: List<FormationSimilaireDTO>,
        val dureeEtudesPrevue: String?,
        val alternance: String?,
        val interetsEtDomainesChoisis: InteretsEtDomainesDTO?,
        val specialitesChoisies: List<AffiniteSpecialiteDTO>,
        val typeBaccalaureat: TypeBaccalaureatDTO?,
        val autoEvaluationMoyenne: AutoEvaluationMoyenneDTO?,
    ) {
        constructor(explications: ExplicationsSuggestionDetaillees) : this(
            geographique = explications.geographique.map { ExplicationGeographiqueDTO(it) },
            formationsSimilaires = explications.formationsSimilaires.map { FormationSimilaireDTO(it) },
            dureeEtudesPrevue = explications.dureeEtudesPrevue?.jsonValeur,
            alternance = explications.alternance?.jsonValeur,
            interetsEtDomainesChoisis =
                InteretsEtDomainesDTO(
                    interets = explications.interets.map { InteretDTO(it) },
                    domaines = explications.domaines.map { DomaineDTO(it) },
                ),
            specialitesChoisies =
                explications.specialitesChoisies.map {
                    AffiniteSpecialiteDTO(
                        it,
                    )
                },
            typeBaccalaureat =
                explications.explicationTypeBaccalaureat?.let {
                    TypeBaccalaureatDTO(it)
                },
            autoEvaluationMoyenne =
                explications.explicationAutoEvaluationMoyenne?.let {
                    AutoEvaluationMoyenneDTO(it)
                },
        )
    }

    data class InteretsEtDomainesDTO(
        val interets: List<InteretDTO>,
        val domaines: List<DomaineDTO>,
    )

    data class InteretDTO(
        val id: String,
        val nom: String,
    ) {
        constructor(interet: InteretSousCategorie) : this(id = interet.id, nom = interet.nom)
    }

    data class DomaineDTO(
        val id: String,
        val nom: String,
    ) {
        constructor(domaine: Domaine) : this(id = domaine.id, nom = domaine.nom)
    }

    data class FormationSimilaireDTO(
        val id: String,
        val nom: String,
    ) {
        constructor(formation: Formation) : this(id = formation.id, nom = formation.nom)
    }

    data class AffiniteSpecialiteDTO(
        val nomSpecialite: String,
        val pourcentage: Int,
    ) {
        constructor(affiniteSpecialite: AffiniteSpecialite) : this(
            nomSpecialite = affiniteSpecialite.nomSpecialite,
            pourcentage = affiniteSpecialite.pourcentage,
        )
    }

    data class ExplicationGeographiqueDTO(
        val nomVille: String,
        val distanceKm: Int,
    ) {
        constructor(explicationGeographique: ExplicationGeographique) : this(
            nomVille = explicationGeographique.ville,
            distanceKm = explicationGeographique.distanceKm,
        )
    }

    data class AutoEvaluationMoyenneDTO(
        val moyenne: Float,
        val basIntervalleNotes: Float,
        val hautIntervalleNotes: Float,
        val baccalaureatUtilise: BaccalaureatDTO,
    ) {
        constructor(autoEvaluationMoyenne: ExplicationAutoEvaluationMoyenne) : this(
            moyenne = autoEvaluationMoyenne.moyenneAutoEvalue,
            basIntervalleNotes = autoEvaluationMoyenne.basIntervalleNotes,
            hautIntervalleNotes = autoEvaluationMoyenne.hautIntervalleNotes,
            baccalaureatUtilise = BaccalaureatDTO(autoEvaluationMoyenne.baccalaureatUtilise),
        )
    }

    data class TypeBaccalaureatDTO(
        val baccalaureat: BaccalaureatDTO,
        val pourcentage: Int,
    ) {
        constructor(typeBaccalaureat: ExplicationTypeBaccalaureat) : this(
            baccalaureat = BaccalaureatDTO(typeBaccalaureat.baccalaureat),
            pourcentage = typeBaccalaureat.pourcentage,
        )
    }
}
