package io.cloudflight.jems.server.project.service.auditAndControl.correction.financialDescription.updateProjectCorrectionFinancialDescription

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanEditAuditControlCorrection
import io.cloudflight.jems.server.project.service.auditAndControl.AuditControlPersistence
import io.cloudflight.jems.server.project.service.auditAndControl.correction.AuditControlCorrectionPersistence
import io.cloudflight.jems.server.project.service.auditAndControl.correction.financialDescription.AuditControlCorrectionFinancePersistence
import io.cloudflight.jems.server.project.service.auditAndControl.model.AuditControl
import io.cloudflight.jems.server.project.service.auditAndControl.model.ProjectCorrectionFinancialDescription
import io.cloudflight.jems.server.project.service.auditAndControl.model.ProjectCorrectionFinancialDescriptionUpdate
import io.cloudflight.jems.server.project.service.auditAndControl.model.correction.AuditControlCorrectionDetail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateProjectProjectCorrectionFinancialDescription(
    private val auditControlPersistence: AuditControlPersistence,
    private val auditControlCorrectionPersistence: AuditControlCorrectionPersistence,
    private val financialDescriptionPersistence: AuditControlCorrectionFinancePersistence,
): UpdateProjectCorrectionFinancialDescriptionInteractor {

    @CanEditAuditControlCorrection
    @Transactional
    @ExceptionWrapper(UpdateCorrectionFinancialDescriptionException::class)
    override fun updateCorrectionFinancialDescription(
        correctionId: Long,
        correctionFinancialDescriptionUpdate: ProjectCorrectionFinancialDescriptionUpdate,
    ): ProjectCorrectionFinancialDescription {
        val correction = auditControlCorrectionPersistence.getByCorrectionId(correctionId)
        val auditControl = auditControlPersistence.getById(correction.auditControlId)

        validateAuditControlNotClosed(auditControl)
        validateAuditControlCorrectionNotClosed(correction)

        return financialDescriptionPersistence
            .updateCorrectionFinancialDescription(correctionId, correctionFinancialDescriptionUpdate)
    }

    private fun validateAuditControlNotClosed(auditControl: AuditControl) {
        if (auditControl.status.isClosed())
            throw AuditControlIsInStatusClosedException()
    }

    private fun validateAuditControlCorrectionNotClosed(correction: AuditControlCorrectionDetail) {
        if (correction.status.isClosed())
            throw CorrectionIsInStatusClosedException()
    }

}