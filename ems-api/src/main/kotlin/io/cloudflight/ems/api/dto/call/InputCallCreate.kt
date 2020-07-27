package io.cloudflight.ems.api.dto.call

import java.time.LocalDate
import java.time.ZonedDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class InputCallCreate (

    @field:NotBlank
    @field:Size(max = 150, message = "call.name.wrong.size")
    val name: String,

    @field:NotNull(message = "call.startDate.unknown")
    val startDate: ZonedDateTime?,

    @field:NotNull(message = "call.endDate.unknown")
    val endDate: ZonedDateTime?,

    @field:Size(max = 1000, message = "call.description.wrong.size")
    val description: String?

)
