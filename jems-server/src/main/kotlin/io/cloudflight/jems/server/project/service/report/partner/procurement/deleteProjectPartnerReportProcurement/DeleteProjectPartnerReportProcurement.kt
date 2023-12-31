package io.cloudflight.jems.server.project.service.report.partner.procurement.deleteProjectPartnerReportProcurement

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanEditPartnerReport
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import io.cloudflight.jems.server.project.service.report.partner.procurement.ProjectPartnerReportProcurementPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteProjectPartnerReportProcurement(
    private val reportPersistence: ProjectPartnerReportPersistence,
    private val reportProcurementPersistence: ProjectPartnerReportProcurementPersistence,
) : DeleteProjectPartnerReportProcurementInteractor {

    @CanEditPartnerReport
    @Transactional
    @ExceptionWrapper(DeleteProjectPartnerReportProcurementException::class)
    override fun delete(partnerId: Long, reportId: Long, procurementId: Long) {
        val report = reportPersistence.getPartnerReportStatusAndVersion(partnerId = partnerId, reportId)
        if (!report.status.isOpenForNumbersChanges())
            throw ReportAlreadyClosed()

        reportProcurementPersistence.deletePartnerReportProcurement(
            partnerId = partnerId,
            reportId = reportId,
            procurementId = procurementId,
        )
    }

}
