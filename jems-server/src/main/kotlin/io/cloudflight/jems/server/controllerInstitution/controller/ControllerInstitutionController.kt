package io.cloudflight.jems.server.controllerInstitution.controller

import io.cloudflight.jems.api.common.dto.file.UserSimpleDTO
import io.cloudflight.jems.api.controllerInstitutions.ControllerInstitutionApi
import io.cloudflight.jems.api.controllerInstitutions.dto.ControllerInstitutionAssignmentDTO
import io.cloudflight.jems.api.controllerInstitutions.dto.ControllerInstitutionDTO
import io.cloudflight.jems.api.controllerInstitutions.dto.ControllerInstitutionListDTO
import io.cloudflight.jems.api.controllerInstitutions.dto.InstitutionPartnerAssignmentDTO
import io.cloudflight.jems.api.controllerInstitutions.dto.InstitutionPartnerDetailsDTO
import io.cloudflight.jems.api.controllerInstitutions.dto.InstitutionPartnerSearchRequestDTO
import io.cloudflight.jems.api.controllerInstitutions.dto.UpdateControllerInstitutionDTO
import io.cloudflight.jems.api.controllerInstitutions.dto.UserInstitutionAccessLevelDTO
import io.cloudflight.jems.api.nuts.dto.OutputNuts
import io.cloudflight.jems.server.controllerInstitution.service.assignInstitutionToPartner.AssignInstitutionToPartnerInteractor
import io.cloudflight.jems.server.controllerInstitution.service.createControllerInstitution.CreateControllerInteractor
import io.cloudflight.jems.server.controllerInstitution.service.getControllerInstitution.GetControllerInteractor
import io.cloudflight.jems.server.controllerInstitution.service.getControllerInstitutionNUTS.GetControllerInstitutionNUTSInteractor
import io.cloudflight.jems.server.controllerInstitution.service.getInstitutionPartnerAssignment.GetInstitutionPartnerAssignmentInteractor
import io.cloudflight.jems.server.controllerInstitution.service.getInstitutionUserAccessLevel.GetInstitutionUserAccessLevelInteractor
import io.cloudflight.jems.server.controllerInstitution.service.getInstitutionUsers.GetInstitutionUsersInteractor
import io.cloudflight.jems.server.controllerInstitution.service.updateControllerInstitution.UpdateControllerInteractor
import io.cloudflight.jems.server.project.controller.report.partner.toDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RestController

@RestController
class ControllerInstitutionController(
    private val getControllerInteractor: GetControllerInteractor,
    private val createControllerInteractor: CreateControllerInteractor,
    private val updateControllerInstitution: UpdateControllerInteractor,
    private val getInstitutionPartnerAssignment: GetInstitutionPartnerAssignmentInteractor,
    private val getInstitutionUserAccessLevel: GetInstitutionUserAccessLevelInteractor,
    private val assignInstitutionToPartnerInteractor: AssignInstitutionToPartnerInteractor,
    private val getInstitutionUsers: GetInstitutionUsersInteractor,
    private val getAvailableRegions: GetControllerInstitutionNUTSInteractor
): ControllerInstitutionApi {

    override fun getControllers(pageable: Pageable): Page<ControllerInstitutionListDTO> =
        getControllerInteractor.getControllers(pageable).toListDto()

    override fun getControllerInstitutionById(institutionId: Long): ControllerInstitutionDTO =
        getControllerInteractor.getControllerInstitutionById(institutionId).toDto()

    override fun createController(controllerData: UpdateControllerInstitutionDTO): ControllerInstitutionDTO =
        createControllerInteractor.createController(controllerData.toModel()).toDto()

    override fun updateController(
        institutionId: Long,
        controllerData: UpdateControllerInstitutionDTO
    ) =
        updateControllerInstitution.updateControllerInstitution(institutionId, controllerData.toModel()).toDto()

    override fun getInstitutionPartnerAssignments(pageable: Pageable, searchRequest: InstitutionPartnerSearchRequestDTO?): Page<InstitutionPartnerDetailsDTO> =
        getInstitutionPartnerAssignment.getInstitutionPartnerAssignments(pageable, searchRequest?.toModel() ?: emptySearch).toPageDto()

    override fun assignInstitutionToPartner(institutionPartnerAssignments: ControllerInstitutionAssignmentDTO): List<InstitutionPartnerAssignmentDTO>  =
        assignInstitutionToPartnerInteractor.assignInstitutionToPartner(institutionPartnerAssignments.toModel()).toDTOs()

    override fun getControllerUserAccessLevelForPartner(partnerId: Long): UserInstitutionAccessLevelDTO? =
        getInstitutionUserAccessLevel.getControllerUserAccessLevelForPartner(partnerId).toDto()

    override fun getUsersByControllerInstitutionId(partnerId: Long, institutionId: Long): List<UserSimpleDTO> =
        getInstitutionUsers.getInstitutionUsers(partnerId, institutionId).map { it.toDto() }

    override fun getAvailableRegions(): List<OutputNuts> =
        getAvailableRegions.getAvailableRegionsForCurrentUser()
}
