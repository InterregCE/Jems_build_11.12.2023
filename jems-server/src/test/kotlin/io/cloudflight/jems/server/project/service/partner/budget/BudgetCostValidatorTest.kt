package io.cloudflight.jems.server.project.service.partner.budget

import io.cloudflight.jems.api.call.dto.CallType
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.call.service.CallPersistence
import io.cloudflight.jems.server.common.exception.I18nValidationException
import io.cloudflight.jems.server.project.service.model.ProjectCallSettings
import io.cloudflight.jems.server.project.service.partner.model.BaseBudgetEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetPeriod
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.stream.IntStream
import kotlin.streams.toList

internal class BudgetCostValidatorTest : UnitTest() {

    @RelaxedMockK
    lateinit var callPersistence: CallPersistence

    @InjectMockKs
    lateinit var budgetCostValidator: BudgetCostValidator

    @Test
    fun `should throw I18nValidationException when number of entries is more that allowed`() {
        val budgetCostEntries = IntStream.range(0, MAX_ALLOWED_NUMBER_OF_BUDGET_ENTRIES.plus(1)).toList().map {
            object : BaseBudgetEntry {
                override val id = it.toLong()
                override val numberOfUnits = BigDecimal.ZERO
                override val budgetPeriods = mutableSetOf<BudgetPeriod>()
                override val rowSum = BigDecimal.ZERO
                override val unitCostId = null
            }
        }
        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validateBaseEntries(budgetCostEntries)
        }

        assertEquals(BUDGET_COST_MAX_NUMBER_OF_ENTRIES_ERROR_KEY, ex.i18nKey)
    }

    @Test
    fun `should throw I18nValidationException when at least to entries have the same id`() {
        val budgetCostEntries = listOf(
            object : BaseBudgetEntry {
                override val id: Long? = null
                override val numberOfUnits = BigDecimal.ZERO
                override val budgetPeriods = mutableSetOf<BudgetPeriod>()
                override val rowSum = BigDecimal.ZERO
                override val unitCostId = null
            },
            object : BaseBudgetEntry {
                override val id = 1L
                override val numberOfUnits = BigDecimal.ZERO
                override val budgetPeriods = mutableSetOf<BudgetPeriod>()
                override val rowSum = BigDecimal.ZERO
                override val unitCostId = null
            },
            object : BaseBudgetEntry {
                override val id = 1L
                override val numberOfUnits = BigDecimal.ZERO
                override val budgetPeriods = mutableSetOf<BudgetPeriod>()
                override val rowSum = BigDecimal.ZERO
                override val unitCostId = null
            }
        )
        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validateBaseEntries(budgetCostEntries)
        }

        assertEquals(BUDGET_COST_DUPLICATE_ENTRY_ERROR_KEY, ex.i18nKey)
    }

    @Test
    fun `should not throw Exception when there are multiple entries with id equal to null`() {
        val budgetCostEntries = listOf(
            object : BaseBudgetEntry {
                override val id: Long? = null
                override val numberOfUnits = BigDecimal.ZERO
                override val budgetPeriods = mutableSetOf<BudgetPeriod>()
                override val rowSum = BigDecimal.ZERO
                override val unitCostId = null
            },
            object : BaseBudgetEntry {
                override val id: Long? = null
                override val numberOfUnits = BigDecimal.ZERO
                override val budgetPeriods = mutableSetOf<BudgetPeriod>()
                override val rowSum = BigDecimal.ZERO
                override val unitCostId = null
            },
        )
        val result = budgetCostValidator.validateBaseEntries(budgetCostEntries)

        assertEquals(Unit, result)

    }

    @Test
    fun `should throw I18nValidationException when at least amount of one period is more than allowed`() {
        val invalidPeriods = mutableSetOf(BudgetPeriod(1, MAX_ALLOWED_BUDGET_VALUE.plus(BigDecimal.ONE).truncate()))

        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validateBudgetPeriods(invalidPeriods, setOf(1))
        }
        assertEquals(BUDGET_COST_VALUE_LIMIT_ERROR_KEY, ex.i18nKey)
    }

    @Test
    fun `should throw I18nValidationException when at least scale of amount of one period is more that 2`() {
        val invalidPeriods = mutableSetOf(BudgetPeriod(1, BigDecimal.valueOf(22.345)))

        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validateBudgetPeriods(invalidPeriods, setOf(1))
        }
        assertEquals(BUDGET_COST_INVALID_PERIOD_AMOUNT_SCALE_ERROR_KEY, ex.i18nKey)
    }

    @Test
    fun `should throw I18nValidationException when at least one period number is invalid`() {
        val invalidPeriods = mutableSetOf(BudgetPeriod(1, BigDecimal.TEN))
        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validateBudgetPeriods(invalidPeriods, setOf(2))
        }
        assertEquals(BUDGET_COST_PERIOD_NOT_EXISTS_ERROR_KEY, ex.i18nKey)
    }

    @Test
    fun `should throw I18nValidationException when at least one pricePerUnit value is more than allowed`() {
        val pricePerUnits = listOf(BigDecimal.ONE, BigDecimal.TEN, MAX_ALLOWED_BUDGET_VALUE.plus(BigDecimal.ONE))
        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validatePricePerUnits(pricePerUnits)
        }
        assertEquals(BUDGET_COST_VALUE_LIMIT_ERROR_KEY, ex.i18nKey)
    }

    @Test
    fun `should throw I18nValidationException when at least one pricePerUnit scale is more that 2`() {
        val pricePerUnits = listOf(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(34.212))
        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validatePricePerUnits(pricePerUnits)
        }
        assertEquals(BUDGET_COST_INVALID_PRICE_PER_UNIT_SCALE_ERROR_KEY, ex.i18nKey)
    }

    @Test
    fun `should not throw exception if spf costs are added for spf call`() {
        assertEquals(budgetCostValidator.validateAllowedSpfCosts(createCallSettings(CallType.SPF)), Unit)
    }

    @Test
    fun `should throw I18nValidationException when at spf costs are added for standard call`() {
        val ex = assertThrows<I18nValidationException> {
            budgetCostValidator.validateAllowedSpfCosts(createCallSettings(CallType.STANDARD))
        }
        assertEquals(BUDGET_COST_SPF_COST_NOT_ALLOWED, ex.i18nKey)
    }

    @TestFactory
    fun `should throw I18nValidationException when at least one of number of units is more than allowed`() =
        listOf(
            createBaseBudgetEntries(withInvalidNumberOfUnits = true) to "numberOfUnits is more than allowed ",
            createBaseBudgetEntries(withInvalidNumberOfUnits = true) to "period amount is more than allowed ",
        ).map { (budgetEntries, argument) ->
            DynamicTest.dynamicTest(
                "should throw I18nValidationException when at least in one budget cost entry $argument"
            ) {
                val ex = assertThrows<I18nValidationException> {
                    budgetCostValidator.validateBaseEntries(budgetEntries)
                }
                assertEquals(BUDGET_COST_VALUE_LIMIT_ERROR_KEY, ex.i18nKey)
            }
        }

    private fun createBaseBudgetEntries(
        withInvalidNumberOfUnits: Boolean = false,
        withInvalidPeriodAmount: Boolean = false
    ): List<BaseBudgetEntry> =
        IntStream.range(1, 10).toList().map {
            object : BaseBudgetEntry {
                override val id = it.toLong()
                override val numberOfUnits = BigDecimal.ZERO.truncate()
                override val budgetPeriods = mutableSetOf<BudgetPeriod>()
                override val rowSum = BigDecimal.ZERO.truncate()
                override val unitCostId = null
            }
        }.plus(
            object : BaseBudgetEntry {
                override val id = 20L
                override val numberOfUnits = if (withInvalidNumberOfUnits) MAX_ALLOWED_BUDGET_VALUE.plus(BigDecimal.ONE)
                    .truncate() else BigDecimal.ZERO.truncate()
                override val budgetPeriods = if (withInvalidPeriodAmount) mutableSetOf(
                    BudgetPeriod(
                        1,
                        MAX_ALLOWED_BUDGET_VALUE.plus(BigDecimal.ONE).truncate()
                    )
                ) else mutableSetOf()
                override val rowSum = BigDecimal.ZERO.truncate()
                override val unitCostId = null
            }
        )

    private fun createCallSettings(callType: CallType): ProjectCallSettings {
        return ProjectCallSettings(
            callId = 1,
            callName = "callName",
            callType = callType,
            startDate = ZonedDateTime.now(),
            endDate = ZonedDateTime.now(),
            endDateStep1 = null,
            lengthOfPeriod = 2,
            isAdditionalFundAllowed = false,
            flatRates = emptySet(),
            lumpSums = emptyList(),
            unitCosts = emptyList(),
            stateAids = emptyList(),
            applicationFormFieldConfigurations = mutableSetOf(),
            preSubmissionCheckPluginKey = null,
            firstStepPreSubmissionCheckPluginKey = null
        )
    }
}
