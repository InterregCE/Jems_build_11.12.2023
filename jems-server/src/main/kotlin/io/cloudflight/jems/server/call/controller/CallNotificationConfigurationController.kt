package io.cloudflight.jems.server.call.controller

import io.cloudflight.jems.api.call.CallNotificationConfigurationApi
import io.cloudflight.jems.api.call.dto.notificationConfiguration.ProjectNotificationConfigurationDTO
import io.cloudflight.jems.server.call.service.notificationConfigurations.getPartnerReportNotificationConfiguration.GetPartnerReportNotificationConfigurationsInteractor
import io.cloudflight.jems.server.call.service.notificationConfigurations.getProjectNotificationConfiguration.GetProjectNotificationConfigurationsInteractor
import io.cloudflight.jems.server.call.service.notificationConfigurations.getProjectReportNotificationConfiguration.GetProjectReportNotificationConfigurationsInteractor
import io.cloudflight.jems.server.call.service.notificationConfigurations.updatePartnerReportNotificationConfiguration.UpdatePartnerReportNotificationConfigurationsInteractor
import io.cloudflight.jems.server.call.service.notificationConfigurations.updateProjectNotificationConfiguration.UpdateProjectNotificationConfigurationsInteractor
import io.cloudflight.jems.server.call.service.notificationConfigurations.updateProjectReportNotificationConfiguration.UpdateProjectReportNotificationConfigurationsInteractor
import org.springframework.web.bind.annotation.RestController

@RestController
class CallNotificationConfigurationController(
    private val getProjectNotificationConfigurations: GetProjectNotificationConfigurationsInteractor,
    private val updateProjectNotificationConfigurations: UpdateProjectNotificationConfigurationsInteractor,
    private val getPartnerReportNotificationConfigurations: GetPartnerReportNotificationConfigurationsInteractor,
    private val updatePartnerReportNotificationConfigurations: UpdatePartnerReportNotificationConfigurationsInteractor,
    private val getProjectReportNotificationConfigurations: GetProjectReportNotificationConfigurationsInteractor,
    private val updateProjectReportNotificationConfigurations: UpdateProjectReportNotificationConfigurationsInteractor,
) : CallNotificationConfigurationApi {

    override fun getProjectNotificationsByCallId(callId: Long): List<ProjectNotificationConfigurationDTO> =
        getProjectNotificationConfigurations.get(callId).toDto()

    override fun updateProjectNotifications(
        callId: Long,
        projectNotificationConfigurations: List<ProjectNotificationConfigurationDTO>
    ): List<ProjectNotificationConfigurationDTO> =
        updateProjectNotificationConfigurations.update(callId, projectNotificationConfigurations.toNotificationModel()).map { it.toDto() }

    override fun getPartnerReportNotificationsByCallId(callId: Long): List<ProjectNotificationConfigurationDTO> =
        getPartnerReportNotificationConfigurations.get(callId).toDto()

    override fun updatePartnerReportNotifications(
        callId: Long,
        projectNotificationConfigurations: List<ProjectNotificationConfigurationDTO>
    ): List<ProjectNotificationConfigurationDTO> =
        updatePartnerReportNotificationConfigurations.update(callId, projectNotificationConfigurations.toNotificationModel()).map { it.toDto() }

    override fun getProjectReportNotificationsByCallId(callId: Long): List<ProjectNotificationConfigurationDTO> =
        getProjectReportNotificationConfigurations.get(callId).toDto()

    override fun updateProjectReportNotifications(
        callId: Long,
        projectNotificationConfigurations: List<ProjectNotificationConfigurationDTO>
    ): List<ProjectNotificationConfigurationDTO> =
        updateProjectReportNotificationConfigurations.update(callId, projectNotificationConfigurations.toNotificationModel()).map { it.toDto() }
}
