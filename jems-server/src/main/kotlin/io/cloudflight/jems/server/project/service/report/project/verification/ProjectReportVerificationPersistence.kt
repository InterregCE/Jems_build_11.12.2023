package io.cloudflight.jems.server.project.service.report.project.verification

import io.cloudflight.jems.server.project.service.report.model.project.verification.ProjectReportVerificationClarification
import io.cloudflight.jems.server.project.service.report.model.project.verification.ProjectReportVerificationConclusion

interface ProjectReportVerificationPersistence {

    fun getVerificationConclusion(
        projectId: Long,
        reportId: Long,
    ): ProjectReportVerificationConclusion

    fun updateVerificationConclusion(
        projectId: Long,
        reportId: Long,
        projectReportVerificationConclusion: ProjectReportVerificationConclusion
    ): ProjectReportVerificationConclusion

    fun getVerificationClarifications(reportId: Long): List<ProjectReportVerificationClarification>

    fun updateVerificationClarifications(
        projectId: Long,
        reportId: Long,
        clarifications: List<ProjectReportVerificationClarification>,
    ): List<ProjectReportVerificationClarification>

}
