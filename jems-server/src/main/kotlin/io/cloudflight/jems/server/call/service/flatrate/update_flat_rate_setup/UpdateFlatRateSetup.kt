package io.cloudflight.jems.server.call.service.flatrate.update_flat_rate_setup

import io.cloudflight.jems.api.call.dto.flatrate.InputCallFlatRateSetup
import io.cloudflight.jems.server.call.authorization.CanUpdateCall
import io.cloudflight.jems.server.call.service.flatrate.CallFlatRateSetupPersistence
import io.cloudflight.jems.server.call.service.flatrate.toModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateFlatRateSetup(private val persistence: CallFlatRateSetupPersistence) : UpdateFlatRateSetupInteractor {

    @Transactional
    @CanUpdateCall
    override fun updateFlatRateSetup(callId: Long, flatRates: Set<InputCallFlatRateSetup>) {
        validateFlatRates(flatRates)
        persistence.updateProjectCallFlatRate(callId, flatRates.toModel(callId))
    }

}