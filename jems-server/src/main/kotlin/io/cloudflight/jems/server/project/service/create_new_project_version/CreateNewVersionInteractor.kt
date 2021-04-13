package io.cloudflight.jems.server.project.service.create_new_project_version

import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.model.ProjectVersion

interface CreateNewProjectVersionInteractor {
    fun create(projectId: Long, status: ApplicationStatus): ProjectVersion
}
