package io.cloudflight.jems.api.project.dto.report.partner

import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRoleDTO
import io.cloudflight.jems.api.project.dto.report.partner.identification.ProjectPartnerReportPeriodDTO
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime

data class ProjectPartnerReportSummaryDTO(
    val id: Long,
    val projectId: Long,
    val partnerId: Long,
    val projectCustomIdentifier: String,
    val partnerRole: ProjectPartnerRoleDTO,
    val partnerNumber: Int,
    val partnerAbbreviation: String,
    val reportNumber: Int,
    val status: ReportStatusDTO,
    val linkedFormVersion: String,
    val firstSubmission: ZonedDateTime?,
    val lastReSubmission: ZonedDateTime?,
    val controlEnd: ZonedDateTime?,
    val createdAt: ZonedDateTime,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val periodDetail: ProjectPartnerReportPeriodDTO?,

    // if certificate linked to project report
    val projectReportId: Long?,
    val projectReportNumber: Int?,

    val totalEligibleAfterControl: BigDecimal?,
    val totalAfterSubmitted: BigDecimal?,
    val deletable: Boolean,
)
