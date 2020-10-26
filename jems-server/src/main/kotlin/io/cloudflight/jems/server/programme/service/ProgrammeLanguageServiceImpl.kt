package io.cloudflight.jems.server.programme.service

import io.cloudflight.jems.api.programme.dto.InputProgrammeLanguage
import io.cloudflight.jems.api.programme.dto.OutputProgrammeLanguage
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.exception.I18nValidationException
import io.cloudflight.jems.server.programme.repository.ProgrammeLanguageRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProgrammeLanguageServiceImpl(
    private val programmeLanguageRepository: ProgrammeLanguageRepository,
    private val auditService: AuditService
) : ProgrammeLanguageService {

    companion object {
        const val MAX_ALLOWED = 50
    }

    @Transactional(readOnly = true)
    override fun get(): List<OutputProgrammeLanguage> {
        return programmeLanguageRepository.findAll().map { it.toOutputProgrammeLanguage() }
    }

    @Transactional
    override fun update(programmeLanguages: Collection<InputProgrammeLanguage>): List<OutputProgrammeLanguage> {

        if (programmeLanguages.size > MAX_ALLOWED)
            throw I18nValidationException(
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
                i18nKey = "programme.language.max.allowed.reached"
            )

        val result = programmeLanguageRepository.saveAll(programmeLanguages.map { it.toEntity() })

        programmeUILanguagesChanged(result).logWith(auditService)
        programmeInputLanguagesChanged(result).logWith(auditService)

        return result.map { it.toOutputProgrammeLanguage() }
    }
}