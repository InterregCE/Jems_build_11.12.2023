package io.cloudflight.ems.api.indicator.dto

import io.cloudflight.ems.api.indicator.validator.UniqueIndicatorResultIdentifier
import io.cloudflight.ems.api.programme.dto.ProgrammeObjectivePolicy
import java.math.BigDecimal
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class InputIndicatorResultCreate(

    @field:NotBlank(message = "indicator.id.should.not.be.empty")
    @field:Size(max = 5, message = "indicator.id.size.too.long")
    @UniqueIndicatorResultIdentifier
    val identifier: String?,

    @field:Size(max = 6, message = "indicator.code.size.too.long")
    val code: String?,

    @field:NotBlank(message = "indicator.name.should.not.be.empty")
    @field:Size(max = 255, message = "indicator.name.size.too.long")
    val name: String?,

    val programmeObjectivePolicy: ProgrammeObjectivePolicy?,

    @field:Size(max = 255, message = "indicator.id.size.too.long")
    val measurementUnit: String?,

    @field:DecimalMin(value = "0.0", message = "indicator.baseline.out.of.range")
    @field:Digits(integer = 9, fraction = 2, message = "indicator.baseline.out.of.range")
    val baseline: BigDecimal?,

    @field:Size(max = 10, message = "indicator.referenceYear.size.too.long")
    val referenceYear: String?,

    @field:DecimalMin(value = "0.0", message = "indicator.finalTarget.out.of.range")
    @field:Digits(integer = 9, fraction = 2, message = "indicator.finalTarget.out.of.range")
    val finalTarget: BigDecimal?,

    @field:Size(max = 1000, message = "indicator.sourceOfData.size.too.long")
    val sourceOfData: String?,

    @field:Size(max = 1000, message = "indicator.comment.size.too.long")
    val comment: String?

)
