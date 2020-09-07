package io.cloudflight.ems.programme.service

import io.cloudflight.ems.api.programme.dto.InputProgrammeFund
import io.cloudflight.ems.api.programme.dto.OutputProgrammeFund
import io.cloudflight.ems.programme.entity.ProgrammeFund

fun InputProgrammeFund.toEntity() = ProgrammeFund(
    id = id,
    abbreviation = if (id == null) abbreviation else null,
    description = if (id == null) description else null,
    selected = selected
)

fun ProgrammeFund.toOutputProgrammeFund(): OutputProgrammeFund {
    return if (id!! <= 9) { // only first 9 funds are translated
        OutputProgrammeFund(id = id!!, selected = selected)
    } else {
        OutputProgrammeFund(id = id!!, abbreviation = abbreviation, description = description, selected = selected)
    }
}