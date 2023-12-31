package io.cloudflight.jems.server.project.service.report.project.financialOverview.getReportCostCategoryBreakdown

import io.cloudflight.jems.server.project.service.report.model.project.financialOverview.costCategory.CertificateCostCategoryBreakdown
import io.cloudflight.jems.server.project.service.report.partner.financialOverview.ProjectPartnerReportExpenditureCostCategoryPersistence
import io.cloudflight.jems.server.project.service.report.project.base.ProjectReportPersistence
import io.cloudflight.jems.server.project.service.report.project.certificate.ProjectReportCertificatePersistence
import io.cloudflight.jems.server.project.service.report.project.financialOverview.ProjectReportCertificateCostCategoryPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetReportCertificateCostCategoryBreakdownCalculator(
    private val reportPersistence: ProjectReportPersistence,
    private val reportCertificateCostCategoryPersistence: ProjectReportCertificateCostCategoryPersistence,
    private val reportCertificatePersistence: ProjectReportCertificatePersistence,
    private val reportExpenditureCostCategoryPersistence: ProjectPartnerReportExpenditureCostCategoryPersistence
) {

    /**
     * This fun will either
     *  - just fetch reported certificate cost category breakdown for submitted report
     *  - or calculate currently reported values from actual certificate cost category
     */
    @Transactional(readOnly = true)
    fun getSubmittedOrCalculateCurrent(projectId: Long, reportId: Long): CertificateCostCategoryBreakdown {
        val reportSatus = reportPersistence.getReportById(projectId = projectId, reportId).status
        val data = reportCertificateCostCategoryPersistence.getCostCategories(projectId = projectId, reportId = reportId)

        val costCategories = data.toLinesModel()

        if (reportSatus.isOpen()) {
            val partnerReportIds = reportCertificatePersistence.listCertificatesOfProjectReport(reportId)
                .mapTo(HashSet()) { it.id }

            val currentValues =
                reportExpenditureCostCategoryPersistence.getCostCategoriesCumulativeTotalEligible(partnerReportIds)

            costCategories.fillInCurrent(current = currentValues)
        }

        return costCategories.fillInOverviewFields()
    }

}
