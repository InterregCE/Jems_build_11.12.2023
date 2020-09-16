package io.cloudflight.ems.project.service

import io.cloudflight.ems.api.project.dto.InputProjectPartnerContact
import io.cloudflight.ems.api.project.dto.InputProjectPartnerCreate
import io.cloudflight.ems.api.project.dto.InputProjectPartnerUpdate
import io.cloudflight.ems.api.project.dto.OutputProjectPartner
import io.cloudflight.ems.api.project.dto.OutputProjectPartnerDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectPartnerService {

    fun getById(id: Long): OutputProjectPartnerDetail

    fun findAllByProjectId(projectId: Long, page: Pageable): Page<OutputProjectPartner>

    fun create(projectId: Long, projectPartner: InputProjectPartnerCreate): OutputProjectPartnerDetail

    fun update(projectId: Long, projectPartner: InputProjectPartnerUpdate): OutputProjectPartnerDetail

    fun updateSortByRole(projectId: Long)

    fun updatePartnerContact(partnerId: Long, projectPartnerContact: Set<InputProjectPartnerContact>): OutputProjectPartnerDetail

}
