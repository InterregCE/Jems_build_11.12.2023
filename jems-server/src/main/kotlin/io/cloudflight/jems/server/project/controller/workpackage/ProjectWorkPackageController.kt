package io.cloudflight.jems.server.project.controller.workpackage

import io.cloudflight.jems.api.project.workpackage.ProjectWorkPackageApi
import io.cloudflight.jems.api.project.dto.workpackage.InputWorkPackageCreate
import io.cloudflight.jems.api.project.dto.workpackage.InputWorkPackageUpdate
import io.cloudflight.jems.api.project.dto.workpackage.OutputWorkPackage
import io.cloudflight.jems.api.project.dto.workpackage.OutputWorkPackageSimple
import io.cloudflight.jems.server.project.service.workpackage.WorkPackageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RestController

@RestController
class ProjectWorkPackageController(
    private val workPackageService: WorkPackageService,
) : ProjectWorkPackageApi {

    override fun getWorkPackageById(workPackageId: Long): OutputWorkPackage =
        workPackageService.getWorkPackageById(workPackageId)

    override fun getWorkPackagesByProjectId(projectId: Long, pageable: Pageable): Page<OutputWorkPackageSimple> =
        workPackageService.getWorkPackagesByProjectId(projectId, pageable)

    override fun createWorkPackage(projectId: Long, inputWorkPackageCreate: InputWorkPackageCreate): OutputWorkPackage =
        workPackageService.createWorkPackage(projectId, inputWorkPackageCreate)

    override fun updateWorkPackage(inputWorkPackageUpdate: InputWorkPackageUpdate): OutputWorkPackage =
        workPackageService.updateWorkPackage(inputWorkPackageUpdate)

    override fun deleteWorkPackage(workPackageId: Long) {
        return workPackageService.deleteWorkPackage(workPackageId)
    }

}