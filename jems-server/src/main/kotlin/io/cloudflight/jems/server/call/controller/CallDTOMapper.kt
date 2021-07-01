package io.cloudflight.jems.server.call.controller

import io.cloudflight.jems.api.call.dto.CallDTO
import io.cloudflight.jems.api.call.dto.CallDetailDTO
import io.cloudflight.jems.api.call.dto.CallUpdateRequestDTO
import io.cloudflight.jems.api.call.dto.application_form_configuration.ApplicationFormFieldConfigurationDTO
import io.cloudflight.jems.api.call.dto.application_form_configuration.StepSelectionOptionDTO
import io.cloudflight.jems.api.call.dto.application_form_configuration.UpdateApplicationFormFieldConfigurationRequestDTO
import io.cloudflight.jems.server.call.service.model.ApplicationFormFieldConfiguration
import io.cloudflight.jems.server.call.service.model.Call
import io.cloudflight.jems.server.call.service.model.CallDetail
import io.cloudflight.jems.server.call.service.model.CallSummary
import io.cloudflight.jems.server.call.service.model.FieldVisibilityStatus
import io.cloudflight.jems.server.common.CommonDTOMapper
import io.cloudflight.jems.server.programme.controller.costoption.toDto
import io.cloudflight.jems.server.programme.controller.fund.toDto
import io.cloudflight.jems.server.programme.controller.priority.toDto
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import org.springframework.data.domain.Page

fun Page<CallSummary>.toDto() = map { it.toDto() }

fun CallSummary.toDto() = CallDTO(
    id = id,
    name = name,
    status = status,
    startDateTime = startDate,
    endDateTime = endDate,
    endDateTimeStep1 = endDateStep1
)

fun CallDetail.toDto() = CallDetailDTO(
    id = id,
    name = name,
    isAdditionalFundAllowed = isAdditionalFundAllowed,
    status = status,
    startDateTime = startDate,
    endDateTimeStep1 = endDateStep1,
    endDateTime = endDate,
    lengthOfPeriod = lengthOfPeriod,
    description = description,
    objectives = objectives.map { it.toDto() },
    strategies = strategies.sorted(),
    funds = funds.toDto(),
    flatRates = flatRates.toDto(),
    lumpSums = lumpSums.toDto(),
    unitCosts = unitCosts.toDto(),
    applicationFormFieldConfigurations = applicationFormFieldConfigurations.toDTO()
)

fun CallUpdateRequestDTO.toModel() = Call(
    id = id ?: 0,
    name = name,
    isAdditionalFundAllowed = isAdditionalFundAllowed,
    lengthOfPeriod = lengthOfPeriod,
    startDate = startDateTime,
    endDateStep1 = endDateTimeStep1,
    endDate = endDateTime,
    description = description,
    priorityPolicies = priorityPolicies,
    strategies = strategies,
    fundIds = fundIds,
)


fun MutableSet<ApplicationFormFieldConfiguration>.toDTO() =
    map { callDTOMapper.map(it) }.toMutableSet()

fun MutableSet<UpdateApplicationFormFieldConfigurationRequestDTO>.toModel() =
    callDTOMapper.mapUpdateRequest(this)

fun StepSelectionOptionDTO.toModel() =
    callDTOMapper.map(this)

fun FieldVisibilityStatus.toDTO() =
    callDTOMapper.map(this)

private val callDTOMapper = Mappers.getMapper(CallDTOMapper::class.java)

@Mapper(uses = [CommonDTOMapper::class])
abstract class CallDTOMapper {

    abstract fun map(fieldVisibilityStatus: FieldVisibilityStatus): StepSelectionOptionDTO
    abstract fun map(stepSelectionOptionDTO: StepSelectionOptionDTO): FieldVisibilityStatus

    fun mapUpdateRequest(updateApplicationFormFieldConfigurationDTOs: MutableSet<UpdateApplicationFormFieldConfigurationRequestDTO>): MutableSet<ApplicationFormFieldConfiguration> =
        updateApplicationFormFieldConfigurationDTOs.map {
            ApplicationFormFieldConfiguration(
                it.id,
                if (!it.isVisible) FieldVisibilityStatus.NONE else it.availableInStep.toModel()
            )
        }.toMutableSet()


    fun map(applicationFormFieldConfiguration: ApplicationFormFieldConfiguration): ApplicationFormFieldConfigurationDTO =
        ApplicationFormFieldConfigurationDTO(
            applicationFormFieldConfiguration.id,
            isVisible = applicationFormFieldConfiguration.visibilityStatus != FieldVisibilityStatus.NONE,
            isVisibilityLocked = !applicationFormFieldConfiguration.getValidVisibilityStatusSet()
                .contains(FieldVisibilityStatus.NONE),
            availableInStep = applicationFormFieldConfiguration.visibilityStatus.toDTO(),
            isStepSelectionLocked = !applicationFormFieldConfiguration.getValidVisibilityStatusSet()
                .containsAll(listOf(FieldVisibilityStatus.STEP_ONE_AND_TWO, FieldVisibilityStatus.STEP_TWO_ONLY))
        )
}
