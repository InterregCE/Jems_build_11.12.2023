package io.cloudflight.jems.server.programme.controller.costoption

import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeUnitCostDTO
import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeUnitCost

fun ProgrammeUnitCost.toDto() = ProgrammeUnitCostDTO(
    id = id,
    name = name,
    description = description,
    type = type,
    costPerUnit = costPerUnit,
    categories = categories
)

fun ProgrammeUnitCostDTO.toModel() = ProgrammeUnitCost(
    id = id,
    name = name,
    description = description,
    type = type,
    costPerUnit = costPerUnit,
    categories = categories
)