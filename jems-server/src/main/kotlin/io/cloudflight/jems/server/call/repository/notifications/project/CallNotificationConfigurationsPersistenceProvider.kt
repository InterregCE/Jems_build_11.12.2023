package io.cloudflight.jems.server.call.repository.notifications.project

import io.cloudflight.jems.server.call.entity.ProjectNotificationConfigurationId
import io.cloudflight.jems.server.call.repository.CallRepository
import io.cloudflight.jems.server.call.repository.toModel
import io.cloudflight.jems.server.call.repository.toNotificationEntities
import io.cloudflight.jems.server.call.repository.toNotificationModel
import io.cloudflight.jems.server.call.service.model.ProjectNotificationConfiguration
import io.cloudflight.jems.server.call.service.notificationConfigurations.CallNotificationConfigurationsPersistence
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CallNotificationConfigurationsPersistenceProvider(
   private val projectNotificationConfigurationRepository: ProjectNotificationConfigurationRepository,
   private val callRepository: CallRepository,
): CallNotificationConfigurationsPersistence {

    @Transactional(readOnly = true)
    override fun getProjectNotificationConfigurations(callId: Long): List<ProjectNotificationConfiguration> =
        projectNotificationConfigurationRepository.findByIdCallEntityId(callId).toNotificationModel()


    @Transactional
    override fun saveProjectNotificationConfigurations(
        callId: Long,
        projectNotificationConfigurations: List<ProjectNotificationConfiguration>
    ): List<ProjectNotificationConfiguration> {
        val callEntity = callRepository.getById(callId)

        val configurations =
            projectNotificationConfigurationRepository.saveAll(projectNotificationConfigurations.toNotificationEntities(callEntity))
                .toMutableSet()
        return configurations.toNotificationModel()
    }

    @Transactional
    override fun getActiveNotificationOfType(
        callId: Long,
        status: ApplicationStatus
    ): ProjectNotificationConfiguration? {
        val callEntity = callRepository.getById(callId)
        return projectNotificationConfigurationRepository.findByActiveTrueAndId(
            ProjectNotificationConfigurationId(
                status,
                callEntity
            )
        )?.toModel()
    }

}