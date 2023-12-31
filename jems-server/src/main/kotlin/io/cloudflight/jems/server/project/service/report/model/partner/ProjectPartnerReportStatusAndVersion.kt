package io.cloudflight.jems.server.project.service.report.model.partner

data class ProjectPartnerReportStatusAndVersion(
    val reportId: Long,
    val status: ReportStatus,
    val version: String,
)
