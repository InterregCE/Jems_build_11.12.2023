package io.cloudflight.jems.server.project.service.report.partner.base.finalizeControlPartnerReport

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanEditPartnerControlReport
import io.cloudflight.jems.server.project.service.budget.model.BudgetCostsCalculationResultFull
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.partner.contribution.ProjectPartnerReportContributionPersistence
import io.cloudflight.jems.server.project.service.report.partner.contribution.extractOverview
import io.cloudflight.jems.server.project.service.report.partner.control.expenditure.ProjectPartnerReportExpenditureVerificationPersistence
import io.cloudflight.jems.server.project.service.report.partner.control.overview.getReportControlWorkOverview.calculateCertified
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportExpenditureCoFinancingPersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportExpenditureCostCategoryPersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportInvestmentPersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportLumpSumPersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportUnitCostPersistence
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportCoFinancingBreakdown.generateCoFinCalculationInputData
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportCoFinancingBreakdown.getCurrentFrom
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportExpenditureInvestementsBreakdown.getAfterControlForInvestments
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportExpenditureLumpSumBreakdown.getAfterControlForLumpSums
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.getReportExpenditureUnitCostBreakdown.getAfterControlForUnitCosts
import io.cloudflight.jems.server.project.service.report.partner.partnerReportControlFinalized
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZonedDateTime

@Service
class FinalizeControlPartnerReport(
    private val reportPersistence: ProjectPartnerReportPersistence,
    private val partnerPersistence: PartnerPersistence,
    private val reportControlExpenditurePersistence: ProjectPartnerReportExpenditureVerificationPersistence,
    private val reportExpenditureCostCategoryPersistence: ProjectPartnerReportExpenditureCostCategoryPersistence,
    private val reportExpenditureCoFinancingPersistence: ProjectPartnerReportExpenditureCoFinancingPersistence,
    private val reportContributionPersistence: ProjectPartnerReportContributionPersistence,
    private val reportLumpSumPersistence: ProjectPartnerReportLumpSumPersistence,
    private val reportUnitCostPersistence: ProjectPartnerReportUnitCostPersistence,
    private val reportInvestmentPersistence: ProjectPartnerReportInvestmentPersistence,
    private val auditPublisher: ApplicationEventPublisher,
) : FinalizeControlPartnerReportInteractor {

    @CanEditPartnerControlReport
    @Transactional
    @ExceptionWrapper(FinalizeControlPartnerReportException::class)
    override fun finalizeControl(partnerId: Long, reportId: Long): ReportStatus {
        val report = reportPersistence.getPartnerReportById(partnerId = partnerId, reportId = reportId)
        validateReportIsInControl(report)

        val expenditures = reportControlExpenditurePersistence
            .getPartnerControlReportExpenditureVerification(partnerId, reportId = reportId)
        val costCategories = reportExpenditureCostCategoryPersistence.getCostCategories(partnerId, reportId = reportId)
        val afterControlCostCategories = expenditures.calculateCertified(options = costCategories.options)

        saveAfterControlCostCategories(afterControlCostCategories, partnerId = partnerId, reportId)
        saveAfterControlCoFinancing(
            afterControlExpenditure = afterControlCostCategories.sum,
            totalEligibleBudget = costCategories.totalsFromAF.sum,
            report = report, partnerId = partnerId,
        )
        saveAfterControlLumpSums(expenditures.getAfterControlForLumpSums(), partnerId = partnerId, reportId)
        saveAfterControlUnitCosts(expenditures.getAfterControlForUnitCosts(), partnerId = partnerId, reportId)
        saveAfterControlInvestments(expenditures.getAfterControlForInvestments(), partnerId = partnerId, reportId)

        return reportPersistence.finalizeControlOnReportById(
            partnerId = partnerId,
            reportId = reportId,
            controlEnd = ZonedDateTime.now(),
        ).also {
            val projectId = partnerPersistence.getProjectIdForPartnerId(id = partnerId, it.version)
            auditPublisher.publishEvent(
                partnerReportControlFinalized(context = this, projectId = projectId, report = it)
            )
        }.status
    }

    private fun validateReportIsInControl(report: ProjectPartnerReport) {
        if (report.status.controlNotOpenAnymore())
            throw ReportNotInControl()
    }

    private fun saveAfterControlCostCategories(afterControlCostCategories: BudgetCostsCalculationResultFull, partnerId: Long, reportId: Long) {
        reportExpenditureCostCategoryPersistence.updateAfterControlValues(
            partnerId = partnerId,
            reportId = reportId,
            afterControl = afterControlCostCategories,
        )
    }

    private fun saveAfterControlCoFinancing(
        afterControlExpenditure: BigDecimal,
        totalEligibleBudget: BigDecimal,
        report: ProjectPartnerReport,
        partnerId: Long,
    ) {
        val contributions = reportContributionPersistence
            .getPartnerReportContribution(partnerId, reportId = report.id).extractOverview()

        reportExpenditureCoFinancingPersistence.updateAfterControlValues(
            partnerId = partnerId,
            reportId = report.id,
            afterControl = getCurrentFrom(
                contributions.generateCoFinCalculationInputData(
                    totalEligibleBudget = totalEligibleBudget,
                    currentValueToSplit = afterControlExpenditure,
                    funds = report.identification.coFinancing,
                )
            ),
        )
    }

    private fun saveAfterControlLumpSums(afterControlLumpSums: Map<Long, BigDecimal>, partnerId: Long, reportId: Long) {
        reportLumpSumPersistence.updateAfterControlValues(
            partnerId = partnerId,
            reportId = reportId,
            afterControl = afterControlLumpSums,
        )
    }

    private fun saveAfterControlUnitCosts(afterControlUnitCosts: Map<Long, BigDecimal>, partnerId: Long, reportId: Long) {
        reportUnitCostPersistence.updateAfterControlValues(
            partnerId = partnerId,
            reportId = reportId,
            afterControl = afterControlUnitCosts,
        )
    }

    private fun saveAfterControlInvestments(afterControlInvestments: Map<Long, BigDecimal>, partnerId: Long, reportId: Long) {
        reportInvestmentPersistence.updateAfterControlValues(
            partnerId = partnerId,
            reportId = reportId,
            afterControl = afterControlInvestments,
        )
    }
}
