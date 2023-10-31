package io.cloudflight.jems.server.payments.service.ecPayment.getPaymentApplicationToEcDetail

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.payments.authorization.CanRetrievePaymentApplicationsToEc
import io.cloudflight.jems.server.payments.model.ec.PaymentApplicationToEcDetail
import io.cloudflight.jems.server.payments.model.regular.PaymentEcStatus
import io.cloudflight.jems.server.payments.service.ecPayment.PaymentApplicationToEcPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetPaymentApplicationToEcDetail(
    private val persistence: PaymentApplicationToEcPersistence
) : GetPaymentApplicationToEcDetailInteractor {

    @CanRetrievePaymentApplicationsToEc
    @Transactional(readOnly = true)
    @ExceptionWrapper(GetPaymentApplicationToEcDetailException::class)
    override fun getPaymentApplicationToEcDetail(id: Long): PaymentApplicationToEcDetail {
        return persistence.getPaymentApplicationToEcDetail(id).fillInFlagForReOpening()
    }

    private fun PaymentApplicationToEcDetail.fillInFlagForReOpening() = apply {
        val programmeFundId = this.paymentApplicationToEcSummary.programmeFund.id
        val accountingYearId = this.paymentApplicationToEcSummary.accountingYear.id
        this.isAvailableToReOpen = status == PaymentEcStatus.Finished &&
                noOtherDraftEcPaymentExistsFor(programmeFundId, accountingYearId)

    }

    private fun noOtherDraftEcPaymentExistsFor(programmeFundId: Long, accountingYearId: Long): Boolean =
        !persistence.existsDraftByFundAndAccountingYear(programmeFundId, accountingYearId)
}