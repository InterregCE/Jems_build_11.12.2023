package io.cloudflight.jems.api.project.dto

import io.cloudflight.jems.api.call.dto.CallCostOptionDTO
import io.cloudflight.jems.api.call.dto.CallType
import io.cloudflight.jems.api.call.dto.applicationFormConfiguration.ApplicationFormFieldConfigurationDTO
import io.cloudflight.jems.api.call.dto.flatrate.FlatRateSetupDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeLumpSumDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeUnitCostDTO
import io.cloudflight.jems.api.programme.dto.stateaid.ProgrammeStateAidDTO
import java.time.ZonedDateTime

data class ProjectCallSettingsDTO(
    val callId: Long,
    val callName: String,
    val callType: CallType,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val endDateStep1: ZonedDateTime?,
    val lengthOfPeriod: Int,
    val additionalFundAllowed: Boolean,
    val directContributionsAllowed: Boolean,
    val flatRates: FlatRateSetupDTO,
    val lumpSums: List<ProgrammeLumpSumDTO>,
    val unitCosts: List<ProgrammeUnitCostDTO>,
    val stateAids: List<ProgrammeStateAidDTO>,
    var applicationFormFieldConfigurations: MutableSet<ApplicationFormFieldConfigurationDTO>,
    val costOption: CallCostOptionDTO,
    val jsNotifiable: Boolean,
    )
