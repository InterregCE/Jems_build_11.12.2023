package io.cloudflight.ems.project.service

import io.cloudflight.ems.api.project.dto.InputProject
import io.cloudflight.ems.api.project.dto.InputProjectData
import io.cloudflight.ems.api.project.dto.OutputProject
import io.cloudflight.ems.api.project.dto.OutputProjectData
import io.cloudflight.ems.api.project.dto.OutputProjectSimple
import io.cloudflight.ems.call.entity.Call
import io.cloudflight.ems.project.entity.Project
import io.cloudflight.ems.project.entity.ProjectStatus
import io.cloudflight.ems.entity.User
import io.cloudflight.ems.call.service.toOutputCallSimple
import io.cloudflight.ems.project.entity.ProjectData
import io.cloudflight.ems.service.toOutputUser

fun InputProject.toEntity(call: Call, applicant: User, status: ProjectStatus) = Project(
    id = null,
    call = call,
    acronym = this.acronym!!,
    applicant = applicant,
    projectStatus = status
)

fun Project.toOutputProject() = OutputProject(
    id = id,
    call = call.toOutputCallSimple(),
    acronym = acronym,
    applicant = applicant.toOutputUser(),
    projectStatus = projectStatus.toOutputProjectStatus(),
    firstSubmission = firstSubmission?.toOutputProjectStatus(),
    lastResubmission = lastResubmission?.toOutputProjectStatus(),
    qualityAssessment = qualityAssessment?.toOutputProjectQualityAssessment(),
    eligibilityAssessment = eligibilityAssessment?.toOutputProjectEligibilityAssessment(),
    eligibilityDecision = eligibilityDecision?.toOutputProjectStatus(),
    fundingDecision = fundingDecision?.toOutputProjectStatus(),
    projectData = projectData?.toOutputProjectData()
)

fun Project.toOutputProjectSimple() = OutputProjectSimple(
    id = id,
    callName = call.name,
    acronym = acronym,
    projectStatus = projectStatus.status,
    firstSubmissionDate = firstSubmission?.updated,
    lastResubmissionDate = lastResubmission?.updated
)

fun InputProjectData.toEntity(project: Project) = ProjectData(
    projectId = project.id!!,
    project = project,
    title = title,
    duration = duration,
    intro = intro,
    introProgrammeLanguage = introProgrammeLanguage
)

fun ProjectData.toOutputProjectData() = OutputProjectData(
    title = title,
    duration = duration,
    intro = intro,
    introProgrammeLanguage = introProgrammeLanguage
)