package io.cloudflight.jems.server.project.service.report.partner.file.control

import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.common.file.service.model.JemsFile
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.report.model.partner.ProjectPartnerReport
import io.cloudflight.jems.server.project.service.report.model.partner.ReportStatus
import io.cloudflight.jems.server.project.service.report.partner.ProjectPartnerReportPersistence
import org.springframework.stereotype.Service

@Service
class ControlReportFileAuthorizationService(
    private val reportPersistence: ProjectPartnerReportPersistence,
    private val partnerPersistence: PartnerPersistence,
    private val filePersistence: JemsFilePersistence,
    private val securityService: SecurityService,
) {

    fun validateChangeToFileAllowed(partnerId: Long, reportId: Long, fileId: Long, requireNotClosedControl: Boolean) {
        val report = reportPersistence.getPartnerReportById(partnerId, reportId = reportId)

        verifyStatus(report.status, requireNotClosedControl)

        val projectId = partnerPersistence.getProjectIdForPartnerId(partnerId)
        // to make sure fileId corresponds to correct report, we need to verify it through location path
        val reportPrefix = JemsFileType.PartnerControlReport.generatePath(projectId, partnerId, reportId)

        val file = filePersistence.getFile(partnerId = partnerId, pathPrefix = reportPrefix, fileId = fileId)
            ?: throw FileNotFound()

        if (file.author.id != securityService.getUserIdOrThrow())
            throw UserIsNotOwnerOfFile()

        verifyControlReOpenChanges(report, file, requireNotClosedControl)
    }

    private fun verifyStatus(status: ReportStatus, requireNotClosedControl: Boolean) {
        when (requireNotClosedControl) {
            true -> if (status.controlNotEvenPartiallyOpen()) throw ReportControlNotOpen()
            else -> if (status.controlNotStartedYet()) throw ReportControlNotStartedYet()
        }
    }


    private fun verifyControlReOpenChanges(
        report: ProjectPartnerReport,
        file: JemsFile,
        requireNotClosedControl: Boolean
    ) {

        if (requireNotClosedControl && report.lastControlReopening != null && file.uploaded.isBefore(report.lastControlReopening))
            throw ReportControlFileInControlReOpened()

    }
}
