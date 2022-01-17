package io.cloudflight.jems.server.project.service

import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeUnitCost
import io.cloudflight.jems.server.project.service.application.ApplicationStatus
import io.cloudflight.jems.server.project.service.model.ProjectFull
import io.cloudflight.jems.server.project.service.model.ProjectApplicantAndStatus
import io.cloudflight.jems.server.project.service.model.ProjectCallSettings
import io.cloudflight.jems.server.project.service.model.ProjectDetail
import io.cloudflight.jems.server.project.service.model.ProjectPeriod
import io.cloudflight.jems.server.project.service.model.ProjectSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectPersistence {

    fun getProject(projectId: Long, version: String? = null): ProjectFull

    fun throwIfNotExists(projectId: Long, version: String? = null)

    fun getApplicantAndStatusById(id: Long): ProjectApplicantAndStatus

    fun getProjectSummary(projectId: Long): ProjectSummary

    fun getProjectCallSettings(projectId: Long): ProjectCallSettings

    fun getCallIdOfProject(projectId: Long): Long

    fun getProjects(pageable: Pageable): Page<ProjectSummary>

    fun getProjectsOfUserPlusExtra(pageable: Pageable, extraProjectIds: Collection<Long>): Page<ProjectSummary>

    fun getProjectUnitCosts(projectId: Long): List<ProgrammeUnitCost>

    fun getProjectPeriods(projectId: Long, version: String? = null): List<ProjectPeriod>

    fun createProjectWithStatus(acronym: String, status: ApplicationStatus, userId: Long, callId: Long): ProjectDetail

    fun updateProjectCustomIdentifier(projectId: Long, customIdentification: String)

}
