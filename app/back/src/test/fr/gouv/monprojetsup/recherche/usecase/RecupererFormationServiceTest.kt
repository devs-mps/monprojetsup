package fr.gouv.monprojetsup.recherche.usecase

import fr.gouv.monprojetsup.recherche.domain.entity.AffinitesPourProfil
import fr.gouv.monprojetsup.recherche.domain.entity.AffinitesPourProfil.FormationAvecSonAffinite
import fr.gouv.monprojetsup.recherche.domain.entity.Baccalaureat
import fr.gouv.monprojetsup.recherche.domain.entity.ChoixAlternance
import fr.gouv.monprojetsup.recherche.domain.entity.ChoixDureeEtudesPrevue
import fr.gouv.monprojetsup.recherche.domain.entity.ChoixNiveau
import fr.gouv.monprojetsup.recherche.domain.entity.CriteresAnalyseCandidature
import fr.gouv.monprojetsup.recherche.domain.entity.Domaine
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationsSuggestion
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationsSuggestion.AffiniteSpecialite
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationsSuggestion.AutoEvaluationMoyenne
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationsSuggestion.ExplicationGeographique
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationsSuggestion.TypeBaccalaureat
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.FicheFormationPourProfil.ExplicationAutoEvaluationMoyenne
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.FicheFormationPourProfil.ExplicationTypeBaccalaureat
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.FicheFormationPourProfil.MoyenneGeneraleDesAdmis
import fr.gouv.monprojetsup.recherche.domain.entity.Formation
import fr.gouv.monprojetsup.recherche.domain.entity.FormationDetaillee
import fr.gouv.monprojetsup.recherche.domain.entity.InteretSousCategorie
import fr.gouv.monprojetsup.recherche.domain.entity.MetierDetaille
import fr.gouv.monprojetsup.recherche.domain.entity.ProfilEleve
import fr.gouv.monprojetsup.recherche.domain.entity.TripletAffectation
import fr.gouv.monprojetsup.recherche.domain.port.BaccalaureatRepository
import fr.gouv.monprojetsup.recherche.domain.port.CriteresAnalyseCandidatureRepository
import fr.gouv.monprojetsup.recherche.domain.port.DomaineRepository
import fr.gouv.monprojetsup.recherche.domain.port.FormationRepository
import fr.gouv.monprojetsup.recherche.domain.port.InteretRepository
import fr.gouv.monprojetsup.recherche.domain.port.SuggestionHttpClient
import fr.gouv.monprojetsup.recherche.domain.port.TripletAffectationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RecupererFormationServiceTest {
    @Mock
    lateinit var suggestionHttpClient: SuggestionHttpClient

    @Mock
    lateinit var formationRepository: FormationRepository

    @Mock
    lateinit var tripletAffectationRepository: TripletAffectationRepository

    @Mock
    lateinit var baccalaureatRepository: BaccalaureatRepository

    @Mock
    lateinit var interetRepository: InteretRepository

    @Mock
    lateinit var domaineRepository: DomaineRepository

    @Mock
    lateinit var moyenneGeneraleDesAdmisService: MoyenneGeneraleDesAdmisService

    @Mock
    lateinit var criteresAnalyseCandidatureRepository: CriteresAnalyseCandidatureRepository

    @InjectMocks
    lateinit var recupererFormationService: RecupererFormationService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    private val formationDetaillee =
        FormationDetaillee(
            id = "fl0001",
            nom = "CAP Fleuriste",
            descriptifGeneral = "Le CAP Fleuriste est un diplôme de niveau 3 qui permet d acquérir les ...",
            descriptifAttendus = "Il est attendu des candidats de démontrer une solide compréhension des techniques ...",
            descriptifDiplome = "Le Certificat d'Aptitude Professionnelle (CAP) est un diplôme national de niveau 3 du ..",
            descriptifConseils = "Nous vous conseillons de développer une sensibilité artistique et de rester informé ...",
            formationsAssociees = listOf("fl0010", "fl0012"),
            liens = listOf("https://www.onisep.fr/ressources/univers-formation/formations/cap-fleuriste"),
            metiers =
                listOf(
                    MetierDetaille(
                        id = "MET_001",
                        nom = "Fleuriste",
                        descriptif = "Le fleuriste est un artisan qui confectionne et vend des bouquets, des ...",
                        liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                    ),
                    MetierDetaille(
                        id = "MET_002",
                        nom = "Fleuriste événementiel",
                        descriptif = "Le fleuriste événementiel est un artisan qui confectionne et vend des bouquets ...",
                        liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                    ),
                ),
            valeurCriteresAnalyseCandidature = listOf(10, 0, 18, 42, 30),
        )

    private val tripletsAffectations =
        listOf(
            TripletAffectation(
                id = "ta0001",
                commune = "Paris",
            ),
            TripletAffectation(
                id = "ta0002",
                commune = "Marseille",
            ),
            TripletAffectation(
                id = "ta0003",
                commune = "Caen",
            ),
        )

    private val criteresAnalyseCandidature =
        listOf(
            CriteresAnalyseCandidature(nom = "Compétences académiques", pourcentage = 10),
            CriteresAnalyseCandidature(
                nom = "Engagements, activités et centres d’intérêt, réalisations péri ou extra-scolaires",
                pourcentage = 0,
            ),
            CriteresAnalyseCandidature(nom = "Résultats académiques", pourcentage = 18),
            CriteresAnalyseCandidature(nom = "Savoir-être", pourcentage = 42),
            CriteresAnalyseCandidature(nom = "Motivation, connaissance", pourcentage = 30),
        )

    @Nested
    inner class ProfilNull {
        @Test
        fun `doit retourner une fiche formation sans le profil avec les criteres filtrant ceux à 0`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation(idFormation = "fl0001")).willReturn(
                tripletsAffectations,
            )

            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = null, idFormation = "fl0001")

            // Then
            assertThat(resultat).usingRecursiveComparison().isEqualTo(
                FicheFormation.FicheFormationSansProfil(
                    id = "fl0001",
                    nom = "CAP Fleuriste",
                    formationsAssociees = listOf("fl0010", "fl0012"),
                    descriptifGeneral = "Le CAP Fleuriste est un diplôme de niveau 3 qui permet d acquérir les ...",
                    descriptifAttendus = "Il est attendu des candidats de démontrer une solide compréhension des techniques ...",
                    descriptifDiplome = "Le Certificat d'Aptitude Professionnelle (CAP) est un diplôme national de niveau 3 du ..",
                    descriptifConseils = "Nous vous conseillons de développer une sensibilité artistique et de rester informé ...",
                    liens = listOf("https://www.onisep.fr/ressources/univers-formation/formations/cap-fleuriste"),
                    metiers =
                        listOf(
                            MetierDetaille(
                                id = "MET_001",
                                nom = "Fleuriste",
                                descriptif = "Le fleuriste est un artisan qui confectionne et vend des bouquets, des ...",
                                liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                            ),
                            MetierDetaille(
                                id = "MET_002",
                                nom = "Fleuriste événementiel",
                                descriptif = "Le fleuriste événementiel est un artisan qui confectionne et vend des bouquets ...",
                                liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                            ),
                        ),
                    communes = listOf("Paris", "Marseille", "Caen"),
                    criteresAnalyseCandidature =
                        listOf(
                            CriteresAnalyseCandidature(nom = "Compétences académiques", pourcentage = 10),
                            CriteresAnalyseCandidature(nom = "Résultats académiques", pourcentage = 18),
                            CriteresAnalyseCandidature(nom = "Savoir-être", pourcentage = 42),
                            CriteresAnalyseCandidature(nom = "Motivation, connaissance", pourcentage = 30),
                        ),
                ),
            )
        }

        @Test
        fun `ne doit pas appeler le suggestionHttpClient`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0001")).willReturn(
                tripletsAffectations,
            )

            // When
            recupererFormationService.recupererFormation(profilEleve = null, idFormation = "fl0001")

            // Then
            then(suggestionHttpClient).shouldHaveNoInteractions()
        }
    }

    @Nested
    inner class ProfilNonNull {
        private val profil =
            ProfilEleve(
                id = "adcf627c-36dd-4df5-897b-159443a6d49c",
                classe = ChoixNiveau.TERMINALE,
                bac = "Générale",
                dureeEtudesPrevue = ChoixDureeEtudesPrevue.OPTIONS_OUVERTES,
                alternance = ChoixAlternance.PAS_INTERESSE,
                villesPreferees = listOf("Caen"),
                specialites = listOf("1001", "1049"),
                centresInterets = listOf("T_ROME_2092381917", "T_IDEO2_4812"),
                moyenneGenerale = 14f,
                metiersChoisis = listOf("MET_123", "MET_456"),
                formationsChoisies = listOf("fl1234", "fl5678"),
                domainesInterets = listOf("T_ITM_1054", "T_ITM_1534", "T_ITM_1248", "T_ITM_1351"),
            )

        private val affinitesPourProfil =
            AffinitesPourProfil(
                metiersTriesParAffinites = listOf("MET_123", "MET_002", "MET_456", "MET_001"),
                formations =
                    listOf(
                        FormationAvecSonAffinite(idFormation = "fl00012", tauxAffinite = 0.9f),
                        FormationAvecSonAffinite(idFormation = "fl0001", tauxAffinite = 0.7f),
                        FormationAvecSonAffinite(idFormation = "fl00014", tauxAffinite = 0.6648471f),
                        FormationAvecSonAffinite(idFormation = "fl0002", tauxAffinite = 0.1250544f),
                    ),
            )

        @Test
        fun `doit retourner une fiche formation avec les metiers et villes triés, les explications et les criteres filtrant ceux à 0`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0001")).willReturn(
                tripletsAffectations,
            )
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat, "fl0001", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            val explications =
                ExplicationsSuggestion(
                    dureeEtudesPrevue = ChoixDureeEtudesPrevue.LONGUE,
                    alternance = ChoixAlternance.TRES_INTERESSE,
                    specialitesChoisies =
                        listOf(
                            AffiniteSpecialite(nomSpecialite = "specialiteA", pourcentage = 12),
                            AffiniteSpecialite(nomSpecialite = "specialiteB", pourcentage = 1),
                            AffiniteSpecialite(nomSpecialite = "specialiteC", pourcentage = 89),
                        ),
                )
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl0001",
                ),
            ).willReturn(explications)

            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl0001")

            // Then
            assertThat(resultat).usingRecursiveComparison().isEqualTo(
                FicheFormation.FicheFormationPourProfil(
                    id = "fl0001",
                    nom = "CAP Fleuriste",
                    formationsAssociees = listOf("fl0010", "fl0012"),
                    descriptifGeneral = "Le CAP Fleuriste est un diplôme de niveau 3 qui permet d acquérir les ...",
                    descriptifAttendus = "Il est attendu des candidats de démontrer une solide compréhension des techniques ...",
                    descriptifDiplome = "Le Certificat d'Aptitude Professionnelle (CAP) est un diplôme national de niveau 3 du ..",
                    descriptifConseils = "Nous vous conseillons de développer une sensibilité artistique et de rester informé ...",
                    liens = listOf("https://www.onisep.fr/ressources/univers-formation/formations/cap-fleuriste"),
                    metiersTriesParAffinites =
                        listOf(
                            MetierDetaille(
                                id = "MET_002",
                                nom = "Fleuriste événementiel",
                                descriptif = "Le fleuriste événementiel est un artisan qui confectionne et vend des bouquets ...",
                                liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                            ),
                            MetierDetaille(
                                id = "MET_001",
                                nom = "Fleuriste",
                                descriptif = "Le fleuriste est un artisan qui confectionne et vend des bouquets, des ...",
                                liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                            ),
                        ),
                    communesTrieesParAffinites = listOf("Caen", "Paris", "Marseille"),
                    tauxAffinite = 70,
                    formationsSimilaires = null,
                    explicationAutoEvaluationMoyenne = null,
                    domaines = null,
                    interets = null,
                    explications = explications,
                    explicationTypeBaccalaureat = null,
                    moyenneGeneraleDesAdmis = moyenneGeneraleDesAdmis,
                    criteresAnalyseCandidature =
                        listOf(
                            CriteresAnalyseCandidature(nom = "Compétences académiques", pourcentage = 10),
                            CriteresAnalyseCandidature(nom = "Résultats académiques", pourcentage = 18),
                            CriteresAnalyseCandidature(nom = "Savoir-être", pourcentage = 42),
                            CriteresAnalyseCandidature(nom = "Motivation, connaissance", pourcentage = 30),
                        ),
                ),
            )
        }

        @Test
        fun `si la formation est une formation enfante, doit retourner la fiche formation de son parent`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            val idFormationParente = "fl0001"
            val idFormationEnfant = "fl00012"
            given(
                formationRepository.recupererUneFormationAvecSesMetiers(idFormationEnfant),
            ).willReturn(formationDetaillee.copy(id = idFormationParente))
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation(idFormationParente)).willReturn(
                tripletsAffectations,
            )
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat, "fl0001", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val explications =
                ExplicationsSuggestion(
                    dureeEtudesPrevue = ChoixDureeEtudesPrevue.LONGUE,
                    alternance = ChoixAlternance.TRES_INTERESSE,
                    specialitesChoisies =
                        listOf(
                            AffiniteSpecialite(nomSpecialite = "specialiteA", pourcentage = 12),
                            AffiniteSpecialite(nomSpecialite = "specialiteB", pourcentage = 1),
                            AffiniteSpecialite(nomSpecialite = "specialiteC", pourcentage = 89),
                        ),
                )
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = idFormationParente,
                ),
            ).willReturn(explications)

            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = idFormationEnfant)

            // Then
            assertThat(resultat).usingRecursiveComparison().isEqualTo(
                FicheFormation.FicheFormationPourProfil(
                    id = idFormationParente,
                    nom = "CAP Fleuriste",
                    formationsAssociees = listOf("fl0010", "fl0012"),
                    descriptifGeneral = "Le CAP Fleuriste est un diplôme de niveau 3 qui permet d acquérir les ...",
                    descriptifAttendus = "Il est attendu des candidats de démontrer une solide compréhension des techniques ...",
                    descriptifDiplome = "Le Certificat d'Aptitude Professionnelle (CAP) est un diplôme national de niveau 3 du ..",
                    descriptifConseils = "Nous vous conseillons de développer une sensibilité artistique et de rester informé ...",
                    liens = listOf("https://www.onisep.fr/ressources/univers-formation/formations/cap-fleuriste"),
                    metiersTriesParAffinites =
                        listOf(
                            MetierDetaille(
                                id = "MET_002",
                                nom = "Fleuriste événementiel",
                                descriptif = "Le fleuriste événementiel est un artisan qui confectionne et vend des bouquets ...",
                                liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                            ),
                            MetierDetaille(
                                id = "MET_001",
                                nom = "Fleuriste",
                                descriptif = "Le fleuriste est un artisan qui confectionne et vend des bouquets, des ...",
                                liens = listOf("https://www.onisep.fr/ressources/univers-metier/metiers/fleuriste"),
                            ),
                        ),
                    communesTrieesParAffinites = listOf("Caen", "Paris", "Marseille"),
                    tauxAffinite = 70,
                    formationsSimilaires = null,
                    explicationAutoEvaluationMoyenne = null,
                    domaines = null,
                    interets = null,
                    explications = explications,
                    explicationTypeBaccalaureat = null,
                    moyenneGeneraleDesAdmis = moyenneGeneraleDesAdmis,
                    criteresAnalyseCandidature =
                        listOf(
                            CriteresAnalyseCandidature(nom = "Compétences académiques", pourcentage = 10),
                            CriteresAnalyseCandidature(nom = "Résultats académiques", pourcentage = 18),
                            CriteresAnalyseCandidature(nom = "Savoir-être", pourcentage = 42),
                            CriteresAnalyseCandidature(nom = "Motivation, connaissance", pourcentage = 30),
                        ),
                ),
            )
        }

        @Test
        fun `doit trier et filtrer les explications géographiques`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0001")).willReturn(
                tripletsAffectations,
            )
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat, "fl0001", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val explications =
                ExplicationsSuggestion(
                    geographique =
                        listOf(
                            ExplicationGeographique(
                                ville = "Nantes",
                                distanceKm = 10,
                            ),
                            ExplicationGeographique(
                                ville = "Nantes",
                                distanceKm = 85,
                            ),
                            ExplicationGeographique(
                                ville = "Paris",
                                distanceKm = 2,
                            ),
                            ExplicationGeographique(
                                ville = "Paris",
                                distanceKm = 1,
                            ),
                            ExplicationGeographique(
                                ville = "Melun",
                                distanceKm = 12,
                            ),
                        ),
                )
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl0001",
                ),
            ).willReturn(explications)

            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl0001")

            // Then
            assertThat((resultat as FicheFormation.FicheFormationPourProfil).explications?.geographique).usingRecursiveComparison()
                .isEqualTo(
                    listOf(
                        ExplicationGeographique(
                            ville = "Paris",
                            distanceKm = 1,
                        ),
                        ExplicationGeographique(
                            ville = "Nantes",
                            distanceKm = 10,
                        ),
                        ExplicationGeographique(
                            ville = "Melun",
                            distanceKm = 12,
                        ),
                    ),
                )
        }

        @Test
        fun `si le baccalaureatRepository réussi, doit retourner l'explication de l'auto évaluation et du type de bac`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0001")).willReturn(
                tripletsAffectations,
            )
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat, "fl0001", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            val explications =
                ExplicationsSuggestion(
                    autoEvaluationMoyenne =
                        AutoEvaluationMoyenne(
                            moyenneAutoEvalue = 14.5f,
                            rangs =
                                ExplicationsSuggestion.RangsEchellons(
                                    rangEch25 = 12,
                                    rangEch50 = 14,
                                    rangEch75 = 16,
                                    rangEch10 = 10,
                                    rangEch90 = 17,
                                ),
                            bacUtilise = "Général",
                        ),
                    typeBaccalaureat =
                        TypeBaccalaureat(
                            nomBaccalaureat = "Général",
                            pourcentage = 18,
                        ),
                )
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl0001",
                ),
            ).willReturn(explications)
            given(baccalaureatRepository.recupererUnBaccalaureatParIdExterne("Général")).willReturn(
                Baccalaureat(id = "Générale", idExterne = "Général", nom = "Série Générale"),
            )

            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl0001")

            // Then
            resultat as FicheFormation.FicheFormationPourProfil
            assertThat(resultat.explicationAutoEvaluationMoyenne).usingRecursiveComparison().isEqualTo(
                ExplicationAutoEvaluationMoyenne(
                    moyenneAutoEvalue = 14.5f,
                    hautIntervalleNotes = 8f,
                    basIntervalleNotes = 6f,
                    baccalaureat = Baccalaureat(id = "Générale", idExterne = "Général", nom = "Série Générale"),
                ),
            )
            assertThat(resultat.explicationTypeBaccalaureat).usingRecursiveComparison().isEqualTo(
                ExplicationTypeBaccalaureat(
                    baccalaureat = Baccalaureat(id = "Générale", idExterne = "Général", nom = "Série Générale"),
                    pourcentage = 18,
                ),
            )
        }

        @Test
        fun `si le baccalaureatRepository échoue, doit retourner l'explication de l'auto évaluation avec le nom renvoyer`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0001")).willReturn(
                tripletsAffectations,
            )
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(null)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(null, "fl0001", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            val explications =
                ExplicationsSuggestion(
                    autoEvaluationMoyenne =
                        AutoEvaluationMoyenne(
                            moyenneAutoEvalue = 14.5f,
                            rangs =
                                ExplicationsSuggestion.RangsEchellons(
                                    rangEch25 = 12,
                                    rangEch50 = 14,
                                    rangEch75 = 16,
                                    rangEch10 = 10,
                                    rangEch90 = 17,
                                ),
                            bacUtilise = "Général",
                        ),
                    typeBaccalaureat =
                        TypeBaccalaureat(
                            nomBaccalaureat = "Général",
                            pourcentage = 18,
                        ),
                )
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl0001",
                ),
            ).willReturn(explications)

            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl0001")

            // Then
            resultat as FicheFormation.FicheFormationPourProfil
            assertThat(resultat.explicationAutoEvaluationMoyenne).usingRecursiveComparison().isEqualTo(
                ExplicationAutoEvaluationMoyenne(
                    moyenneAutoEvalue = 14.5f,
                    hautIntervalleNotes = 8f,
                    basIntervalleNotes = 6f,
                    baccalaureat = Baccalaureat(id = "Général", idExterne = "Général", nom = "Général"),
                ),
            )
            assertThat(resultat.explicationTypeBaccalaureat).usingRecursiveComparison().isEqualTo(
                ExplicationTypeBaccalaureat(
                    baccalaureat = Baccalaureat(id = "Général", idExterne = "Général", nom = "Général"),
                    pourcentage = 18,
                ),
            )
        }

        @Test
        fun `doit retourner les domaines et interets`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0001")).willReturn(
                tripletsAffectations,
            )
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat, "fl0001", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val interetsEtDomainesChoisis =
                listOf(
                    "T_ROME_731379930",
                    "T_ITM_1169",
                    "T_ROME_1959553899",
                )
            val explications = ExplicationsSuggestion(interetsEtDomainesChoisis = interetsEtDomainesChoisis)
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl0001",
                ),
            ).willReturn(explications)
            val domaines = listOf(Domaine(id = "T_ITM_1169", nom = "défense nationale"))
            val interets =
                listOf(
                    InteretSousCategorie(id = "aider_autres", nom = "Aider les autres"),
                    InteretSousCategorie(id = "creer", nom = "Créer quelque chose de mes mains"),
                )
            given(domaineRepository.recupererLesDomaines(interetsEtDomainesChoisis)).willReturn(domaines)
            given(interetRepository.recupererLesSousCategoriesDInterets(interetsEtDomainesChoisis)).willReturn(interets)

            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl0001")

            // Then
            resultat as FicheFormation.FicheFormationPourProfil
            assertThat(resultat.interets).usingRecursiveComparison().isEqualTo(interets)
            assertThat(resultat.domaines).usingRecursiveComparison().isEqualTo(domaines)
        }

        @Test
        fun `doit retourner les formations similaires`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0001")).willReturn(formationDetaillee)
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0001")).willReturn(
                tripletsAffectations,
            )
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat, "fl0001", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            val explications = ExplicationsSuggestion(formationsSimilaires = listOf("fl1", "fl7"))
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl0001",
                ),
            ).willReturn(explications)
            val formations =
                listOf(
                    Formation(id = "fl1", nom = "Classe préparatoire aux études supérieures - Cinéma audiovisuel"),
                    Formation(id = "fl7", nom = "Classe préparatoire aux études supérieures - Littéraire"),
                )
            given(formationRepository.recupererLesNomsDesFormations(listOf("fl1", "fl7"))).willReturn(formations)
            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl0001")

            // Then
            resultat as FicheFormation.FicheFormationPourProfil
            assertThat(resultat.formationsSimilaires).usingRecursiveComparison().isEqualTo(formations)
        }

        @Test
        fun `si la formation n'est pas présente dans la liste retournée par l'api suggestion, doit retourner 0 en taux d'affinité`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl00011")).willReturn(formationDetaillee.copy(id = "fl00011"))
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl00011")).willReturn(
                tripletsAffectations,
            )
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(
                    baccalaureat,
                    "fl00011",
                    ChoixNiveau.TERMINALE,
                ),
            ).willReturn(moyenneGeneraleDesAdmis)
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val explications = ExplicationsSuggestion()
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl00011",
                ),
            ).willReturn(explications)
            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl00011")

            // Then
            resultat as FicheFormation.FicheFormationPourProfil
            assertThat(resultat.tauxAffinite).isEqualTo(0)
        }

        @Test
        fun `si la valeur du taux d'affinité est strictement inférieur à x,xx5, doit l'arrondir à l'inférieur`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl0002")).willReturn(formationDetaillee.copy(id = "fl0002"))
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl0002")).willReturn(
                tripletsAffectations,
            )
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat, "fl0002", ChoixNiveau.TERMINALE),
            ).willReturn(moyenneGeneraleDesAdmis)
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val explications = ExplicationsSuggestion()
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl0002",
                ),
            ).willReturn(explications)
            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl0002")

            // Then
            resultat as FicheFormation.FicheFormationPourProfil
            assertThat(resultat.tauxAffinite).isEqualTo(13)
        }

        @Test
        fun `si la valeur du taux d'affinité est supérieur ou égale à x,xx5, doit l'arrondir au supérieur`() {
            // Given
            given(criteresAnalyseCandidatureRepository.recupererLesCriteresDUneFormation(listOf(10, 0, 18, 42, 30))).willReturn(
                criteresAnalyseCandidature,
            )
            given(formationRepository.recupererUneFormationAvecSesMetiers("fl00014")).willReturn(formationDetaillee.copy(id = "fl00014"))
            given(tripletAffectationRepository.recupererLesTripletsAffectationDUneFormation("fl00014")).willReturn(
                tripletsAffectations,
            )
            val baccalaureat = mock(Baccalaureat::class.java)
            given(baccalaureatRepository.recupererUnBaccalaureat("Générale")).willReturn(baccalaureat)
            val moyenneGeneraleDesAdmis = mock(MoyenneGeneraleDesAdmis::class.java)
            given(
                moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(
                    baccalaureat,
                    "fl00014",
                    ChoixNiveau.TERMINALE,
                ),
            ).willReturn(moyenneGeneraleDesAdmis)
            given(suggestionHttpClient.recupererLesAffinitees(profilEleve = profil)).willReturn(affinitesPourProfil)
            val explications = ExplicationsSuggestion()
            given(
                suggestionHttpClient.recupererLesExplications(
                    profilEleve = profil,
                    idFormation = "fl00014",
                ),
            ).willReturn(explications)
            // When
            val resultat = recupererFormationService.recupererFormation(profilEleve = profil, idFormation = "fl00014")

            // Then
            resultat as FicheFormation.FicheFormationPourProfil
            assertThat(resultat.tauxAffinite).isEqualTo(66)
        }
    }
}
