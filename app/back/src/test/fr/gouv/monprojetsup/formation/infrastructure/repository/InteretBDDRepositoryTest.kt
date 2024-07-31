package fr.gouv.monprojetsup.formation.infrastructure.repository

import fr.gouv.monprojetsup.commun.infrastructure.repository.BDDRepositoryTest
import fr.gouv.monprojetsup.referentiel.domain.entity.Interet
import fr.gouv.monprojetsup.referentiel.domain.entity.InteretCategorie
import fr.gouv.monprojetsup.referentiel.domain.entity.InteretSousCategorie
import fr.gouv.monprojetsup.referentiel.infrastructure.repository.InteretBDDRepository
import fr.gouv.monprojetsup.referentiel.infrastructure.repository.InteretCategorieJPARepository
import fr.gouv.monprojetsup.referentiel.infrastructure.repository.InteretJPARepository
import fr.gouv.monprojetsup.referentiel.infrastructure.repository.InteretSousCategorieJPARepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql

class InteretBDDRepositoryTest : BDDRepositoryTest() {
    @Autowired
    lateinit var interetJPARepository: InteretJPARepository

    @Autowired
    lateinit var interetSousCategorieJPARepository: InteretSousCategorieJPARepository

    @Autowired
    lateinit var interetCategorieJPARepository: InteretCategorieJPARepository

    lateinit var interetBDDRepository: InteretBDDRepository

    @BeforeEach
    fun setup() {
        interetBDDRepository = InteretBDDRepository(interetJPARepository, interetSousCategorieJPARepository, interetCategorieJPARepository)
    }

    @Nested
    inner class RecupererLesSousCategoriesDInterets {
        @Test
        @Sql("classpath:interet.sql")
        fun `Doit retourner les sous catégories des intérêts reconnus et ignorer ceux inconnus`() {
            // Given
            val ids =
                listOf(
                    "decouvrir_monde",
                    "linguistique",
                    "voyage",
                    "multiculturel",
                    "T_ROME_1825212206",
                    "T_ROME_934089965",
                    "T_ROME_326548351",
                )

            // When
            val result = interetBDDRepository.recupererLesSousCategoriesDInterets(ids)

            // Then
            val attendu =
                mapOf(
                    "T_ROME_326548351" to InteretSousCategorie(id = "voyage", nom = "Voyager", emoji = "\uD83D\uDE85"),
                    "T_ROME_934089965" to InteretSousCategorie(id = "voyage", nom = "Voyager", emoji = "\uD83D\uDE85"),
                    "T_ROME_1825212206" to
                        InteretSousCategorie(
                            id = "linguistique",
                            nom = "Apprendre de nouvelles langues",
                            emoji = "\uD83C\uDDEC\uD83C\uDDE7",
                        ),
                )
            assertThat(result).isEqualTo(attendu)
        }

        @Test
        @Sql("classpath:interet.sql")
        fun `Si la liste est vide, doit retourner une liste vide`() {
            // Given
            val ids = emptyList<String>()

            // When
            val result = interetBDDRepository.recupererLesSousCategoriesDInterets(ids)

            // Then
            val attendu = emptyMap<String, InteretSousCategorie>()
            assertThat(result).isEqualTo(attendu)
        }
    }

    @Nested
    inner class RecupererLesInteretsDeSousCategories {
        @Test
        @Sql("classpath:interet.sql")
        fun `Doit retourner les intérêts des sous catégories reconnues et ignorer celles inconnues`() {
            // Given
            val ids =
                listOf(
                    "decouvrir_monde",
                    "linguistique",
                    "voyage",
                    "multiculturel",
                    "T_ROME_1825212206",
                    "T_ROME_934089965",
                    "T_ROME_326548351",
                )

            // When
            val result = interetBDDRepository.recupererLesInteretsDeSousCategories(ids)

            // Then
            val attendu =
                listOf(
                    Interet(id = "T_ROME_326548351", nom = "je veux conduire"),
                    Interet(id = "T_ROME_934089965", nom = "je veux voyager"),
                    Interet(id = "T_ROME_1825212206", nom = "je veux écrire ou lire"),
                )
            assertThat(result).isEqualTo(attendu)
        }

        @Test
        @Sql("classpath:interet.sql")
        fun `Si la liste est vide, doit retourner une liste vide`() {
            // Given
            val ids = emptyList<String>()

            // When
            val result = interetBDDRepository.recupererLesInteretsDeSousCategories(ids)

            // Then
            val attendu = emptyList<Interet>()
            assertThat(result).isEqualTo(attendu)
        }
    }

    @Nested
    inner class RecupererToutesLesCategoriesEtLeursSousCategoriesDInterets {
        @Test
        @Sql("classpath:interet.sql")
        fun `Doit retourner tous les catégories et sous categories d'intérêts`() {
            // When
            val result = interetBDDRepository.recupererToutesLesCategoriesEtLeursSousCategoriesDInterets()

            // Then
            val attendu =
                mapOf(
                    InteretCategorie(id = "decouvrir_monde", nom = "Découvrir le monde", emoji = "🌎") to
                        listOf(
                            InteretSousCategorie(id = "voyage", nom = "Voyager", emoji = "🚅"),
                            InteretSousCategorie(id = "linguistique", nom = "Apprendre de nouvelles langues", emoji = "🇬🇧"),
                        ),
                    InteretCategorie(id = "rechercher", nom = "Découvrir, enquêter et rechercher", emoji = "\uD83E\uDDD0") to emptyList(),
                )
            assertThat(result).isEqualTo(attendu)
        }
    }

    @Nested
    inner class VerifierCentresInteretsExistent {
        @Test
        @Sql("classpath:interet.sql")
        fun `si toutes les centres d'intérêt existent, renvoyer true`() {
            // Given
            val ids = listOf("linguistique", "voyage")

            // When
            val result = interetBDDRepository.verifierCentresInteretsExistent(ids)

            // Then
            assertThat(result).isTrue()
        }

        @Test
        @Sql("classpath:interet.sql")
        fun `si un des centres d'intérêt n'existe pas, renvoyer false`() {
            // Given
            val ids =
                listOf(
                    "decouvrir_monde",
                    "linguistique",
                    "voyage",
                    "T_ROME_1825212206",
                )

            // When
            val result = interetBDDRepository.verifierCentresInteretsExistent(ids)

            // Then
            assertThat(result).isFalse()
        }
    }
}
