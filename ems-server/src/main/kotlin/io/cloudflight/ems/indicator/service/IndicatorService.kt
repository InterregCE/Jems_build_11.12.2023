package io.cloudflight.ems.indicator.service

import io.cloudflight.ems.api.indicator.dto.InputIndicatorOutputCreate
import io.cloudflight.ems.api.indicator.dto.InputIndicatorOutputUpdate
import io.cloudflight.ems.api.indicator.dto.InputIndicatorResultCreate
import io.cloudflight.ems.api.indicator.dto.InputIndicatorResultUpdate
import io.cloudflight.ems.api.indicator.dto.OutputIndicatorOutput
import io.cloudflight.ems.api.indicator.dto.OutputIndicatorResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IndicatorService {

    //region INDICATOR OUTPUT

    fun getOutputIndicatorById(id: Long): OutputIndicatorOutput

    fun getOutputIndicators(pageable: Pageable): Page<OutputIndicatorOutput>

    fun existsOutputByIdentifier(identifier: String): Boolean

    fun save(indicator: InputIndicatorOutputCreate): OutputIndicatorOutput

    fun save(indicator: InputIndicatorOutputUpdate): OutputIndicatorOutput
    //endregion

    //region INDICATOR RESULT

    fun getResultIndicatorById(id: Long): OutputIndicatorResult

    fun getResultIndicators(pageable: Pageable): Page<OutputIndicatorResult>

    fun existsResultByIdentifier(identifier: String): Boolean

    fun save(indicator: InputIndicatorResultCreate): OutputIndicatorResult

    fun save(indicator: InputIndicatorResultUpdate): OutputIndicatorResult
    //endregion

}
