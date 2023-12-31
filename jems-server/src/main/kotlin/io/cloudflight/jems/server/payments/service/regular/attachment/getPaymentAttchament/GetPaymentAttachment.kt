package io.cloudflight.jems.server.payments.service.regular.attachment.getPaymentAttchament

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.common.file.service.model.JemsFile
import io.cloudflight.jems.server.common.file.service.model.JemsFileType.PaymentAttachment
import io.cloudflight.jems.server.payments.authorization.CanRetrievePayments
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPaymentAttachment(
    private val filePersistence: JemsFilePersistence
) : GetPaymentAttachmentInteractor {

    @CanRetrievePayments
    @Transactional(readOnly = true)
    @ExceptionWrapper(GetPaymentAttachmentException::class)
    override fun list(paymentId: Long, pageable: Pageable): Page<JemsFile> =
        filePersistence.listAttachments(
            pageable = pageable,
            indexPrefix = PaymentAttachment.generatePath(paymentId),
            filterSubtypes = emptySet(),
            filterUserIds = emptySet(),
        )

}
