package io.cloudflight.jems.server.project.service.report.partner.expenditure.uploadFileToProjectPartnerReportExpenditure

import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.file.service.model.JemsFileMetadata
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import io.cloudflight.jems.server.project.authorization.CanEditPartnerReport
import io.cloudflight.jems.server.project.service.file.model.ProjectFile
import io.cloudflight.jems.server.project.service.file.uploadProjectFile.isFileTypeInvalid
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import io.cloudflight.jems.server.project.service.report.partner.expenditure.ProjectPartnerReportExpenditurePersistence
import io.cloudflight.jems.server.project.service.report.partner.file.ProjectPartnerReportFilePersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UploadFileToProjectPartnerReportExpenditure(
    private val reportFilePersistence: ProjectPartnerReportFilePersistence,
    private val reportExpenditurePersistence: ProjectPartnerReportExpenditurePersistence,
    private val partnerPersistence: PartnerPersistence,
    private val securityService: SecurityService,
): UploadFileToProjectPartnerReportExpenditureInteractor {

    @CanEditPartnerReport
    @Transactional
    @ExceptionWrapper(UploadFileToProjectPartnerReportExpenditureException::class)
    override fun uploadToExpenditure(
        partnerId: Long,
        reportId: Long,
        expenditureId: Long,
        file: ProjectFile,
    ): JemsFileMetadata {
        if (!reportExpenditurePersistence.existsByExpenditureId(partnerId, reportId = reportId, expenditureId = expenditureId))
            throw ExpenditureNotFoundException(expenditureId = expenditureId)

        if (isFileTypeInvalid(file))
            throw FileTypeNotSupported()

        with(JemsFileType.Expenditure) {
            val projectId = partnerPersistence.getProjectIdForPartnerId(partnerId)
            val location = generatePath(projectId, partnerId, reportId, expenditureId)

            return reportFilePersistence.updatePartnerReportExpenditureAttachment(
                expenditureId = expenditureId,
                file = file.getFileMetadata(projectId, partnerId, location, type = this, securityService.getUserIdOrThrow()),
            )
        }
    }
}
