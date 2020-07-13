package io.cloudflight.ems.controller

import io.cloudflight.ems.api.ProjectStatusApi
import io.cloudflight.ems.api.dto.InputProjectStatus
import io.cloudflight.ems.api.dto.OutputProject
import io.cloudflight.ems.service.ProjectStatusService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class ProjectStatusController(
    private val projectStatusService: ProjectStatusService
) : ProjectStatusApi {

    @PreAuthorize("@projectStatusAuthorization.canChangeStatusTo(#id, #status.status)")
    override fun setProjectStatus(id: Long, status: InputProjectStatus): OutputProject {
        return projectStatusService.setProjectStatus(id, statusChange = status)
    }

}