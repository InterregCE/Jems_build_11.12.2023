package io.cloudflight.jems.server.project.service.auditAndControl.correction.base.listAuditControlCorrection

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.authorization.CanViewAuditControl
import io.cloudflight.jems.server.project.repository.auditAndControl.correction.toLineModel
import io.cloudflight.jems.server.project.service.auditAndControl.correction.AuditControlCorrectionPersistence
import io.cloudflight.jems.server.project.service.auditAndControl.model.correction.AuditControlCorrectionLine
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListAuditControlCorrection(
    private val auditControlCorrectionPersistence: AuditControlCorrectionPersistence,
): ListAuditControlCorrectionInteractor {

    @CanViewAuditControl
    @Transactional
    @ExceptionWrapper(ListAuditControlCorrectionException::class)
    override fun listCorrections(
        auditControlId: Long,
        pageable: Pageable
    ): Page<AuditControlCorrectionLine> =
        auditControlCorrectionPersistence
            .getAllCorrectionsByAuditControlId(auditControlId, pageable)
            .toLineModel()

}