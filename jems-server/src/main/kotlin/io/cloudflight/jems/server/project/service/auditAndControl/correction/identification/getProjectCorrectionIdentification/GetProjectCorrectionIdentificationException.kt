package io.cloudflight.jems.server.project.service.auditAndControl.correction.identification.getProjectCorrectionIdentification

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.server.common.exception.ApplicationException

private const val GET_PROJECT_AUDIT_CONTROL_CORRECTION_IDENTIFICATION_ERROR_CODE_PREFIX = "S-GPACCI"
private const val GET_PROJECT_AUDIT_CONTROL_CORRECTION_IDENTIFICATION_PREFIX = "use.case.get.project.audit.control.correction.identification"

class GetProjectCorrectionIdentificationException(cause: Throwable): ApplicationException(
    code = GET_PROJECT_AUDIT_CONTROL_CORRECTION_IDENTIFICATION_ERROR_CODE_PREFIX,
    i18nMessage = I18nMessage("$GET_PROJECT_AUDIT_CONTROL_CORRECTION_IDENTIFICATION_PREFIX.failed"),
    cause = cause
)

