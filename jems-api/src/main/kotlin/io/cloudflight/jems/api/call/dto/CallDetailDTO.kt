package io.cloudflight.jems.api.call.dto

import io.cloudflight.jems.api.call.dto.applicationFormConfiguration.ApplicationFormFieldConfigurationDTO
import io.cloudflight.jems.api.call.dto.flatrate.FlatRateSetupDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeLumpSumListDTO
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeUnitCostListDTO
import io.cloudflight.jems.api.programme.dto.priority.ProgrammePriorityDTO
import io.cloudflight.jems.api.programme.dto.stateaid.ProgrammeStateAidDTO
import io.cloudflight.jems.api.programme.dto.strategy.ProgrammeStrategy
import io.cloudflight.jems.api.project.dto.InputTranslation
import java.time.ZonedDateTime

data class CallDetailDTO(
    val id: Long,
    val name: String,
    val status: CallStatus,
    val type: CallType,
    val startDateTime: ZonedDateTime,
    val endDateTimeStep1: ZonedDateTime?,
    val endDateTime: ZonedDateTime,
    val additionalFundAllowed: Boolean,
    val directContributionsAllowed: Boolean,
    val lengthOfPeriod: Int?,
    val description: Set<InputTranslation> = emptySet(),
    val objectives: List<ProgrammePriorityDTO> = emptyList(),
    val strategies: List<ProgrammeStrategy> = emptyList(),
    val funds: List<CallFundRateDTO> = emptyList(),
    val stateAids: List<ProgrammeStateAidDTO> = emptyList(),
    val flatRates: FlatRateSetupDTO,
    val lumpSums: List<ProgrammeLumpSumListDTO> = emptyList(),
    val unitCosts: List<ProgrammeUnitCostListDTO> = emptyList(),
    val applicationFormFieldConfigurations: MutableSet<ApplicationFormFieldConfigurationDTO>,
    val preSubmissionCheckPluginKey: String?,
    val firstStepPreSubmissionCheckPluginKey: String?,
    val reportPartnerCheckPluginKey: String?,
    val reportProjectCheckPluginKey: String?,
    val controlReportPartnerCheckPluginKey: String?,
    val controlReportSamplingCheckPluginKey: String?,
)
