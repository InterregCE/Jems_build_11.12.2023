package io.cloudflight.jems.server.common.validator

import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.api.project.dto.InputTranslation
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class GeneralValidatorDefaultImpl : GeneralValidatorService {

    override fun maxLength(input: String?, maxLength: Int, fieldName: String) =
        mutableMapOf<String, I18nMessage>().apply {
            if (!input.isNullOrBlank() && input.length > maxLength)
                this[fieldName] = I18nMessage(
                    "common.error.field.max.length",
                    mapOf("currentLength" to input.length.toString(), "maxLength" to maxLength.toString())
                )
        }


    override fun maxLength(translations: Set<InputTranslation>, maxLength: Int, fieldName: String) =
        mutableMapOf<String, I18nMessage>().apply {
            translations.forEach {
                if (!it.translation.isNullOrBlank() && it.translation!!.length > maxLength)
                    this["$fieldName.${it.language.translationKey}"] = I18nMessage(
                        "common.error.field.max.length",
                        mapOf(
                            "currentLength" to it.translation!!.length.toString(),
                            "maxLength" to maxLength.toString()
                        )
                    )
            }
        }

    override fun notBlank(input: String?, fieldName: String) =
        mutableMapOf<String, I18nMessage>().apply {
            if (input.isNullOrBlank())
                this[fieldName] = I18nMessage("common.error.field.blank")
        }

    override fun notNullOrZero(input: Long?, fieldName: String) =
        mutableMapOf<String, I18nMessage>().apply {
            if (input == null || input == 0L)
                this[fieldName] = I18nMessage("common.error.field.should.not.be.null.or.zero")
        }

    override fun nullOrZero(input: Long?, fieldName: String) =
        mutableMapOf<String, I18nMessage>().apply {
            if (input != null && input != 0L)
                this[fieldName] = I18nMessage("common.error.field.should.be.null.or.zero")
        }

    override fun minDecimal(input: BigDecimal?, minValue: BigDecimal, fieldName: String) =
        mutableMapOf<String, I18nMessage>().apply {
            if (input != null && input < minValue)
                this[fieldName] = I18nMessage(
                    "common.error.field.min.decimal",
                    mapOf("minValue" to minValue.toString())
                )
        }

    override fun digits(input: BigDecimal?, maxIntegerLength: Int, maxFractionLength: Int, fieldName: String) =
        mutableMapOf<String, I18nMessage>().apply {
            if (input != null) {
                val integerPartLength: Int = input.precision() - input.scale()
                val fractionPartLength = if (input.scale() < 0) 0 else input.scale()
                if (integerPartLength > maxIntegerLength || fractionPartLength > maxFractionLength)
                    this[fieldName] = I18nMessage(
                        "common.error.field.digit",
                        mapOf(
                            "maxInteger" to maxIntegerLength.toString(),
                            "maxFraction" to maxFractionLength.toString(),
                        )
                    )
            }
        }

    override fun throwIfAnyIsInvalid(vararg validationResult: Map<String, I18nMessage>) =
        mutableMapOf<String, I18nMessage>().run {
            validationResult.forEach { this.putAll(it) }
            if (this.isNotEmpty())
                throw AppInputValidationException(this)
        }

}