package io.cloudflight.jems.server.programme.service.costoption.update_lump_sum

import io.cloudflight.jems.api.audit.dto.AuditAction
import io.cloudflight.jems.api.common.dto.I18nMessage
import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.OfficeAndAdministrationCosts
import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.StaffCosts
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeLumpSumPhase.Implementation
import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.api.project.dto.InputTranslation
import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.audit.model.AuditCandidateEvent
import io.cloudflight.jems.server.audit.service.AuditCandidate
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.common.validator.AppInputValidationException
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.programme.service.costoption.ProgrammeLumpSumPersistence
import io.cloudflight.jems.server.programme.service.costoption.model.PaymentClaim
import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeLumpSum
import io.cloudflight.jems.server.programme.service.info.isSetupLocked.IsProgrammeSetupLockedInteractor
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal

internal class UpdateLumpSumInteractorTest : UnitTest() {

    private val inputErrorMap = mapOf("error" to I18nMessage("error.key"))

    private val initialLumpSum = ProgrammeLumpSum(
        id = 4,
        name = setOf(InputTranslation(SystemLanguage.EN, "LS1")),
        description = setOf(InputTranslation(SystemLanguage.EN, "test lump sum 1")),
        cost = BigDecimal.ONE,
        splittingAllowed = true,
        phase = Implementation,
        categories = setOf(OfficeAndAdministrationCosts, StaffCosts),
        fastTrack = false,
        paymentClaim = PaymentClaim.IncurredByBeneficiaries
    )

    @MockK
    lateinit var persistence: ProgrammeLumpSumPersistence

    @MockK
    lateinit var isProgrammeSetupLocked: IsProgrammeSetupLockedInteractor

    @MockK
    lateinit var auditPublisher: ApplicationEventPublisher

    @RelaxedMockK
    lateinit var generalValidator: GeneralValidatorService

    @InjectMockKs
    lateinit var updateLumpSum: UpdateLumpSum

    @BeforeEach
    fun reset() {
        clearMocks(generalValidator)
        every { generalValidator.throwIfAnyIsInvalid(*varargAny { it.isEmpty() }) } returns Unit
        every { generalValidator.throwIfAnyIsInvalid(*varargAny { it.isNotEmpty() }) } throws AppInputValidationException(
            inputErrorMap
        )
    }

    @Test
    fun `update lump sum - invalid`() {
        every { isProgrammeSetupLocked.isLocked() } returns false
        every { isProgrammeSetupLocked.isAnyReportCreated() } returns true
        every { persistence.getLumpSum(any()) } returns initialLumpSum
        val wrongLumpSum = ProgrammeLumpSum(
            id = 4,
            name = setOf(InputTranslation(SystemLanguage.EN, " ")),
            description = setOf(InputTranslation(SystemLanguage.EN, "test lump sum 1")),
            cost = null,
            splittingAllowed = true,
            phase = null,
            categories = setOf(OfficeAndAdministrationCosts),
            fastTrack = true,
            paymentClaim = PaymentClaim.IncurredByBeneficiaries
        )
        val ex = assertThrows<LumpSumIsInvalid> { updateLumpSum.updateLumpSum(wrongLumpSum) }
        assertThat(ex.formErrors).containsExactlyInAnyOrderEntriesOf(mapOf(
            "cost" to I18nMessage(i18nKey = "lump.sum.out.of.range"),
            "categories" to I18nMessage(i18nKey = "programme.lumpSum.categories.min.2"),
        ))
    }

    @Test
    fun `update lump sum - invalid fast track`() {
        every { isProgrammeSetupLocked.isLocked() } returns false
        every { isProgrammeSetupLocked.isAnyReportCreated() } returns true
        every { persistence.getLumpSum(any()) } returns initialLumpSum
        val wrongLumpSum = ProgrammeLumpSum(
            id = 4,
            name = setOf(InputTranslation(SystemLanguage.EN, " ")),
            description = setOf(InputTranslation(SystemLanguage.EN, "test lump sum 1")),
            cost = BigDecimal.ONE,
            splittingAllowed = true,
            phase = Implementation,
            categories = setOf(OfficeAndAdministrationCosts, StaffCosts ),
            fastTrack = true,
            paymentClaim = PaymentClaim.IncurredByBeneficiaries
        )
        assertThrows<UpdateLumpSumWhenProgrammeSetupRestricted> { updateLumpSum.updateLumpSum(wrongLumpSum) }
    }

    @Test
    fun `update lump sum - check if name length is validated`() {
        val name = setOf(InputTranslation(SystemLanguage.SK, getStringOfLength(51)))
        every {
            generalValidator.maxLength(name, 50, "name")
        } returns inputErrorMap
        assertThrows<AppInputValidationException> {
            updateLumpSum.updateLumpSum(initialLumpSum.copy(name = name))
        }
        verify(exactly = 1) { generalValidator.maxLength(name, 50, "name") }
    }

    @Test
    fun `update lump sum - check if description length is validated`() {
        val description = setOf(InputTranslation(SystemLanguage.EN, getStringOfLength(256)))
        every {
            generalValidator.maxLength(description, 255, "description")
        } returns inputErrorMap
        assertThrows<AppInputValidationException> {
            updateLumpSum.updateLumpSum(initialLumpSum.copy(description = description))
        }
        verify(exactly = 1) { generalValidator.maxLength(description, 255, "description") }
    }

    @Test
    fun `update lump sum - check if phase is validated`() {
        val phase = null
        every {
            generalValidator.notNull(phase, "phase")
        } returns inputErrorMap
        assertThrows<AppInputValidationException> {
            updateLumpSum.updateLumpSum(initialLumpSum.copy(phase = phase))
        }
        verify(exactly = 1) { generalValidator.notNull(phase, "phase") }
    }

    @Test
    fun `update lump sum - OK`() {
        every { isProgrammeSetupLocked.isLocked() } returns false
        every { isProgrammeSetupLocked.isAnyReportCreated() } returns false
        every { isProgrammeSetupLocked.isFastTrackLumpSumReadyForPayment(4L) } returns false
        every { persistence.getLumpSum(any()) } returns initialLumpSum
        every { persistence.updateLumpSum(any()) } returnsArgument 0

        val lumpSum = ProgrammeLumpSum(
            id = 4,
            name = setOf(InputTranslation(SystemLanguage.EN, "LS1")),
            description = setOf(InputTranslation(SystemLanguage.EN, "test lump sum 1")),
            cost = BigDecimal.ONE,
            splittingAllowed = true,
            phase = Implementation,
            categories = setOf(OfficeAndAdministrationCosts, StaffCosts),
            fastTrack = false,
            paymentClaim = PaymentClaim.IncurredByBeneficiaries
        )
        val auditSlot = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(auditSlot)) } answers { }
        assertThat(updateLumpSum.updateLumpSum(lumpSum)).isEqualTo(lumpSum.copy())
        assertThat(auditSlot.captured.auditCandidate).isEqualTo(AuditCandidate(
            action = AuditAction.PROGRAMME_LUMP_SUM_CHANGED,
            description = "Programme lump sum (id=4) '[EN=LS1]' has been changed: (no-change)"
        ))
    }

    @Test
    fun `update lump sum - wrong ID filled in`() {
        every { isProgrammeSetupLocked.isLocked() } returns false
        every { persistence.getLumpSum(any()) } returns initialLumpSum
        val lumpSum = ProgrammeLumpSum(
            name = setOf(InputTranslation(SystemLanguage.EN, "LS1")),
            cost = BigDecimal.ONE,
            splittingAllowed = true,
            phase = Implementation,
            fastTrack = false,
            paymentClaim = PaymentClaim.IncurredByBeneficiaries
        )

        assertThrows<LumpSumIsInvalid>("when updating id cannot be invalid") {
            updateLumpSum.updateLumpSum(lumpSum.copy(id = 0)) }
    }

    @Test
    fun `update lump sum - not existing`() {
        every { isProgrammeSetupLocked.isLocked() } returns false
        every { isProgrammeSetupLocked.isAnyReportCreated() } returns false
        every { isProgrammeSetupLocked.isFastTrackLumpSumReadyForPayment(777L) } returns false
        every { persistence.getLumpSum(any()) } returns initialLumpSum
        val lumpSum = ProgrammeLumpSum(
            id = 777,
            name = setOf(InputTranslation(SystemLanguage.EN, "LS1")),
            cost = BigDecimal.ONE,
            splittingAllowed = true,
            phase = Implementation,
            categories = setOf(OfficeAndAdministrationCosts, StaffCosts),
            fastTrack = false,
            paymentClaim = PaymentClaim.IncurredByBeneficiaries
        )
        every { persistence.updateLumpSum(any()) } throws ResourceNotFoundException("programmeLumpSum")

        assertThrows<ResourceNotFoundException>("when updating not existing lump sum") {
            updateLumpSum.updateLumpSum(lumpSum) }
    }

    @Test
    fun `update lump sum - call already published with same cost effective value but different number of decimal zeros`() {
        every { persistence.getLumpSum(any()) } returns initialLumpSum
        every { persistence.updateLumpSum(any()) } returnsArgument 0
        every { isProgrammeSetupLocked.isLocked() } returns true
        every { isProgrammeSetupLocked.isAnyReportCreated() } returns false
        every { isProgrammeSetupLocked.isFastTrackLumpSumReadyForPayment(4L) } returns false
        val lumpSum = ProgrammeLumpSum(
            id = 4,
            name = setOf(InputTranslation(SystemLanguage.EN, "LS1 changed")),
            description = setOf(InputTranslation(SystemLanguage.EN, "test lump sum 1 changed")),
            cost = BigDecimal(1),
            splittingAllowed = true,
            phase = Implementation,
            categories = setOf(OfficeAndAdministrationCosts, StaffCosts),
            fastTrack = false,
            paymentClaim = PaymentClaim.IncurredByBeneficiaries
        )
        val auditSlot = slot<AuditCandidateEvent>()
        every { auditPublisher.publishEvent(capture(auditSlot)) } answers { }
        assertThat(updateLumpSum.updateLumpSum(lumpSum)).isEqualTo(lumpSum.copy())
        assertThat(auditSlot.captured.auditCandidate).isEqualTo(AuditCandidate(
            action = AuditAction.PROGRAMME_LUMP_SUM_CHANGED,
            description = "Programme lump sum (id=4) '[EN=LS1 changed]' has been changed: name changed from [\n" +
                "  EN=LS1\n" +
                "] to [\n" +
                "  EN=LS1 changed\n" +
                "],\n" +
                "description changed from [\n" +
                "  EN=test lump sum 1\n" +
                "] to [\n" +
                "  EN=test lump sum 1 changed\n" +
                "]"
        ))
    }

    @Test
    fun `update lump sum - call already published with different cost value`() {
        every { persistence.getLumpSum(any()) } returns initialLumpSum
        every { isProgrammeSetupLocked.isLocked() } returns true

        assertThrows<UpdateLumpSumWhenProgrammeSetupRestricted> {updateLumpSum.updateLumpSum(initialLumpSum.copy(cost = BigDecimal.TEN))}
    }

    @Test
    fun `update lump sum - fast truck lump sum was already set as already ready for payment`() {
        every { persistence.getLumpSum(any()) } returns initialLumpSum.copy(fastTrack = true)
        every { isProgrammeSetupLocked.isLocked() } returns true
        every { isProgrammeSetupLocked.isFastTrackLumpSumReadyForPayment(4L) } returns true
        every { isProgrammeSetupLocked.isAnyReportCreated() } returns false

        assertThrows<UpdateLumpSumWhenProgrammeSetupRestricted> {updateLumpSum.updateLumpSum(initialLumpSum)}
    }
}
