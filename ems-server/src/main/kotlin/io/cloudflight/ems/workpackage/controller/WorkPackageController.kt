package io.cloudflight.ems.workpackage.controller

import io.cloudflight.ems.api.workpackage.WorkPackageApi
import io.cloudflight.ems.api.workpackage.dto.InputWorkPackageCreate
import io.cloudflight.ems.api.workpackage.dto.InputWorkPackageUpdate
import io.cloudflight.ems.api.workpackage.dto.OutputWorkPackage
import io.cloudflight.ems.api.workpackage.dto.OutputWorkPackageSimple
import io.cloudflight.ems.workpackage.service.WorkPackageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkPackageController(
    private val workPackageService: WorkPackageService
) : WorkPackageApi {

    @PreAuthorize("@projectAuthorization.canReadProject(#projectId)")
    override fun getWorkPackageById(projectId: Long, id: Long): OutputWorkPackage {
        return workPackageService.getWorkPackageById(id)
    }

    @PreAuthorize("@projectAuthorization.canReadProject(#projectId)")
    override fun getWorkPackagesByProjectId(projectId: Long, pageable: Pageable): Page<OutputWorkPackageSimple> {
        return workPackageService.getWorkPackagesByProjectId(projectId, pageable)
    }

    @PreAuthorize("@projectAuthorization.canUpdateProject(#inputWorkPackageCreate.projectId)")
    override fun createWorkPackage(projectId: Long, inputWorkPackageCreate: InputWorkPackageCreate): OutputWorkPackage {
        return workPackageService.createWorkPackage(inputWorkPackageCreate)
    }

    @PreAuthorize("@projectAuthorization.canUpdateProject(#projectId)")
    override fun updateWorkPackage(projectId: Long, inputWorkPackageUpdate: InputWorkPackageUpdate): OutputWorkPackage {
        return workPackageService.updateWorkPackage(inputWorkPackageUpdate)
    }

}