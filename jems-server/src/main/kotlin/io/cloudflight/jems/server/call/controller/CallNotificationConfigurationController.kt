package io.cloudflight.jems.server.call.controller

import io.cloudflight.jems.api.call.CallNotificationConfigurationApi
import io.cloudflight.jems.api.call.dto.notificationConfiguration.ProjectNotificationConfigurationDTO
import io.cloudflight.jems.server.call.service.notificationConfigurations.getProjectNotificationConfiguration.GetProjectNotificationConfigurationsInteractor
import io.cloudflight.jems.server.call.service.notificationConfigurations.updateProjectNotificationConfigurations.UpdateProjectNotificationConfigurationsInteractor
import org.springframework.web.bind.annotation.RestController

@RestController
class CallNotificationConfigurationController(
    private val getProjectNotificationConfigurations: GetProjectNotificationConfigurationsInteractor,
    private val updateProjectNotificationConfigurations: UpdateProjectNotificationConfigurationsInteractor
) : CallNotificationConfigurationApi {
    override fun getProjectNotificationsByCallId(callId: Long): List<ProjectNotificationConfigurationDTO> =
        getProjectNotificationConfigurations.get(callId).toDto()

    override fun updateProjectNotifications(
        callId: Long,
        projectNotificationConfigurations: List<ProjectNotificationConfigurationDTO>
    ): List<ProjectNotificationConfigurationDTO> =
        updateProjectNotificationConfigurations.update(callId, projectNotificationConfigurations.toNotificationModel()).map { it.toDto() }
}
