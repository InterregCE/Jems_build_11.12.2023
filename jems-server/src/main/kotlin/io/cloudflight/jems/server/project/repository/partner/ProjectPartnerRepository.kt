package io.cloudflight.jems.server.project.repository.partner

import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRole
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProjectPartnerRepository : PagingAndSortingRepository<ProjectPartnerEntity, Long> {

    fun findFirstByProjectIdAndId(projectId: Long, id: Long): Optional<ProjectPartnerEntity>

    fun findAllByProjectId(projectId: Long, pageable: Pageable): Page<ProjectPartnerEntity>

    fun findTop30ByProjectId(projectId: Long, sort: Sort): Iterable<ProjectPartnerEntity>

    fun findFirstByProjectIdAndRole(projectId: Long, role: ProjectPartnerRole): Optional<ProjectPartnerEntity>

    fun countByProjectId(projectId: Long): Long

}
