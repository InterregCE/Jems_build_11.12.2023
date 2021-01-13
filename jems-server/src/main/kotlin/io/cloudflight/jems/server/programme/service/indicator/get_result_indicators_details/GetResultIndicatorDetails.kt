package io.cloudflight.jems.server.programme.service.indicator.get_result_indicators_details

import io.cloudflight.jems.server.programme.authorization.CanReadIndicators
import io.cloudflight.jems.server.programme.service.indicator.IndicatorResultPersistence
import org.springframework.stereotype.Service

@Service
class GetResultIndicatorDetails(
    private val indicatorResultPersistence: IndicatorResultPersistence
) : GetResultIndicatorDetailsInteractor {

    @CanReadIndicators
    override fun getResultIndicatorsDetails() =
        indicatorResultPersistence.getResultIndicatorsDetails()

}