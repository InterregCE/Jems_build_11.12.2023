package io.cloudflight.jems.server.payments.service.ecPayment.linkToCorrection.getAvailableClosedCorrections

import io.cloudflight.jems.server.payments.model.ec.PaymentToEcCorrectionLinking
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetAvailableClosedCorrectionsInteractor {

    fun getClosedCorrectionList(pageable: Pageable, ecApplicationId: Long): Page<PaymentToEcCorrectionLinking>

}