package io.cloudflight.jems.api.payments

import io.cloudflight.jems.api.payments.dto.PaymentApplicationToEcDTO
import io.cloudflight.jems.api.payments.dto.PaymentApplicationToEcDetailDTO
import io.cloudflight.jems.api.payments.dto.PaymentApplicationToEcUpdateDTO
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@Api("Payment Application to EC")
interface PaymentApplicationToEcApi {
    companion object {
        const val ENDPOINT_API_EC_PAYMENTS = "/api/paymentApplicationsToEc"
    }

    @ApiOperation("Create payment applications to ec")
    @PostMapping("$ENDPOINT_API_EC_PAYMENTS/create", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createPaymentApplicationToEc(
        @RequestBody paymentApplicationToEcUpdate: PaymentApplicationToEcUpdateDTO
    ): PaymentApplicationToEcDetailDTO

    @ApiOperation("Update payment applications to ec")
    @PutMapping("$ENDPOINT_API_EC_PAYMENTS/detail", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updatePaymentApplicationToEc(
        @RequestBody paymentApplicationToEcUpdate: PaymentApplicationToEcUpdateDTO
    ): PaymentApplicationToEcDetailDTO

    @ApiOperation("Get payment applications to ec detail")
    @GetMapping("$ENDPOINT_API_EC_PAYMENTS/{id}")
    fun getPaymentApplicationToEcDetail(
        @PathVariable id: Long
    ): PaymentApplicationToEcDetailDTO

    @ApiOperation("Returns all payment applications to ec")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping(ENDPOINT_API_EC_PAYMENTS)
    fun getPaymentApplicationsToEc(pageable: Pageable): Page<PaymentApplicationToEcDTO>

    @ApiOperation("Delete payment application to ec by id")
    @DeleteMapping("${ENDPOINT_API_EC_PAYMENTS}/{id}")
    fun deletePaymentApplicationToEc(@PathVariable id: Long)
}
