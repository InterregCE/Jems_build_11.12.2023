package io.cloudflight.jems.server.project.repository.report.expenditure

import io.cloudflight.jems.server.common.entity.extractField
import io.cloudflight.jems.server.project.entity.report.expenditure.PartnerReportInvestmentEntity
import io.cloudflight.jems.server.project.service.report.model.expenditure.ProjectPartnerReportInvestment

fun List<PartnerReportInvestmentEntity>.toModel() = map {
    ProjectPartnerReportInvestment(
        id = it.id,
        investmentId = it.investmentId,
        workPackageNumber = it.workPackageNumber,
        title = it.translatedValues.extractField { it.title },
        investmentNumber = it.investmentNumber,
        total = it.total,
    )
}
