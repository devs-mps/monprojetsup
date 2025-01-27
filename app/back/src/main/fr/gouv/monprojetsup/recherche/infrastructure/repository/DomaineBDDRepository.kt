package fr.gouv.monprojetsup.recherche.infrastructure.repository

import fr.gouv.monprojetsup.recherche.domain.entity.Domaine
import fr.gouv.monprojetsup.recherche.domain.port.DomaineRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class DomaineBDDRepository(
    val domaineJPARepository: DomaineJPARepository,
) : DomaineRepository {
    @Transactional(readOnly = true)
    override fun recupererLesDomaines(ids: List<String>): List<Domaine> {
        return domaineJPARepository.findAllByIdIn(ids).map { it.toDomaine() }
    }
}
