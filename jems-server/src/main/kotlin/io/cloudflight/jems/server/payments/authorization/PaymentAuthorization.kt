package io.cloudflight.jems.server.payments.authorization

import org.springframework.security.access.prepost.PreAuthorize

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('PaymentsRetrieve')")
annotation class CanRetrievePayments

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('PaymentsUpdate')")
annotation class CanUpdatePayments

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('AdvancePaymentsRetrieve')")
annotation class CanRetrieveAdvancePayments

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('AdvancePaymentsUpdate')")
annotation class CanUpdateAdvancePayments

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('PaymentsToEcRetrieve')")
annotation class CanRetrievePaymentApplicationsToEc

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("false")//Will be reverted in V9: MP2-3852
annotation class CanUpdatePaymentApplicationsToEc
