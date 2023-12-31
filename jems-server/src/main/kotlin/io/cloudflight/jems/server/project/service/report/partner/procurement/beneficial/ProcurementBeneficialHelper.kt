package io.cloudflight.jems.server.project.service.report.partner.procurement.beneficial

import io.cloudflight.jems.server.project.service.report.model.partner.procurement.beneficial.ProjectPartnerReportProcurementBeneficialOwner

const val MAX_AMOUNT_OF_BENEFICIAL = 30

fun List<ProjectPartnerReportProcurementBeneficialOwner>.fillThisReportFlag(currentReportId: Long) = map {
    it.apply {
        createdInThisReport = reportId == currentReportId
    }
}
