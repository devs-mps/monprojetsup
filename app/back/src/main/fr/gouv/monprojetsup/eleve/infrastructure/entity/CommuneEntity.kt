package fr.gouv.monprojetsup.eleve.infrastructure.entity

import fr.gouv.monprojetsup.eleve.domain.entity.Commune
import java.io.Serializable

data class CommuneEntity(
    val codeInsee: String,
    val nom: String,
    val latitude: Float,
    val longitude: Float,
) : Serializable {
    fun toCommune() =
        Commune(
            codeInsee = codeInsee,
            nom = nom,
            latitude = latitude,
            longitude = longitude,
        )
}
