package io.cloudflight.jems.server.project.service.auditAndControl.model.correction

import io.cloudflight.jems.server.project.service.auditAndControl.model.AuditControlStatus
import java.time.LocalDate

data class AuditControlCorrectionDetail(
    val id: Long,
    val orderNr: Int,
    val status: AuditControlStatus,
    val type: AuditControlCorrectionType,

    val auditControlId: Long,
    val auditControlNr: Int,

    val followUpOfCorrectionId: Long?,
    val correctionFollowUpType: CorrectionFollowUpType,
    val repaymentFrom: LocalDate?,
    val lateRepaymentTo: LocalDate?,
    val partnerId: Long?,
    val partnerReportId: Long?,
    val programmeFundId: Long?,
)
