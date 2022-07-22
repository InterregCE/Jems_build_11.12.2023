package io.cloudflight.jems.server.controllerInstitution.service.createControllerInstitution

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationNotFoundException

const val UPDATE_CONTROLLER_INSTITUTION_ERROR_CODE_PREFIX = "S-CIN"
const val UPDATE_CONTROLLER_INSTITUTION_ERROR_KEY_PREFIX = "use.case.update.controller.institution"

class UpdateControllerInstitutionException : ApplicationNotFoundException(
    code = "$UPDATE_CONTROLLER_INSTITUTION_ERROR_CODE_PREFIX-001",
    i18nMessage = I18nMessage("$UPDATE_CONTROLLER_INSTITUTION_ERROR_KEY_PREFIX.failed")
)
