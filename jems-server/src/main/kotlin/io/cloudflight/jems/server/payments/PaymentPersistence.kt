package io.cloudflight.jems.server.payments

import io.cloudflight.jems.server.payments.entity.PaymentGroupingId
import io.cloudflight.jems.server.payments.service.model.PartnerPayment
import io.cloudflight.jems.server.payments.service.model.PartnerPaymentSimple
import io.cloudflight.jems.server.payments.service.model.PaymentConfirmedInfo
import io.cloudflight.jems.server.payments.service.model.PaymentDetail
import io.cloudflight.jems.server.payments.service.model.PaymentPartnerInstallment
import io.cloudflight.jems.server.payments.service.model.PaymentPartnerInstallmentUpdate
import io.cloudflight.jems.server.payments.service.model.PaymentPerPartner
import io.cloudflight.jems.server.payments.service.model.PaymentToCreate
import io.cloudflight.jems.server.payments.service.model.PaymentToProject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PaymentPersistence {

    fun existsById(id: Long): Boolean

    fun getAllPaymentToProject(pageable: Pageable): Page<PaymentToProject>

    fun getConfirmedInfosForPayment(paymentId: Long): PaymentConfirmedInfo

    fun getPaymentDetails(paymentId: Long): PaymentDetail

    fun getAllPartnerPayments(paymentId: Long): List<PartnerPayment>

    fun getAllPartnerPaymentsForPartner(partnerId: Long): List<PartnerPaymentSimple>

    fun deleteAllByProjectIdAndOrderNrIn(projectId: Long, orderNr: Set<Int>)

    fun getAmountPerPartnerByProjectIdAndLumpSumOrderNrIn(
        projectId: Long,
        orderNrsToBeAdded: MutableSet<Int>
    ): List<PaymentPerPartner>

    fun savePaymentToProjects(projectId: Long, paymentsToBeSaved:  Map<PaymentGroupingId, PaymentToCreate>)

    fun getPaymentPartnerId(paymentId: Long, partnerId: Long): Long

    fun findPaymentPartnerInstallments(paymentPartnerId: Long): List <PaymentPartnerInstallment>

    fun findByPartnerId(partnerId: Long): List<PaymentPartnerInstallment>

    fun updatePaymentPartnerInstallments(paymentPartnerId: Long,
                                         toDeleteInstallmentIds: Set<Long>,
                                         paymentPartnerInstallments: List<PaymentPartnerInstallmentUpdate>
    ): List<PaymentPartnerInstallment>
}
