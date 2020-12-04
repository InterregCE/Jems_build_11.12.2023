package io.cloudflight.jems.server.programme.repository.costoption

import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.ExternalCosts
import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.EquipmentCosts
import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.OfficeAndAdministrationCosts
import io.cloudflight.jems.api.programme.dto.costoption.BudgetCategory.StaffCosts
import io.cloudflight.jems.api.programme.dto.costoption.ProgrammeLumpSumPhase
import io.cloudflight.jems.server.common.exception.ResourceNotFoundException
import io.cloudflight.jems.server.programme.entity.costoption.ProgrammeLumpSumBudgetCategoryEntity
import io.cloudflight.jems.server.programme.entity.costoption.ProgrammeLumpSumEntity
import io.cloudflight.jems.server.programme.service.costoption.ProgrammeLumpSumPersistence
import io.cloudflight.jems.server.programme.service.costoption.model.ProgrammeLumpSum
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.Optional

class ProgrammeLumpSumPersistenceTest {

    companion object {
        private val categoryStaff = ProgrammeLumpSumBudgetCategoryEntity(
            id = 10,
            programmeLumpSumId = 1,
            category = StaffCosts,
        )
        private val categoryOffice = ProgrammeLumpSumBudgetCategoryEntity(
            id = 11,
            programmeLumpSumId = 1,
            category = OfficeAndAdministrationCosts,
        )
    }

    private lateinit var testLumpSum: ProgrammeLumpSumEntity
    private lateinit var expectedLumpSum: ProgrammeLumpSum

    @MockK
    lateinit var repository: ProgrammeLumpSumRepository

    private lateinit var programmeLumpSumPersistence: ProgrammeLumpSumPersistence

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        programmeLumpSumPersistence = ProgrammeLumpSumPersistenceProvider(repository)

        testLumpSum = ProgrammeLumpSumEntity(
            id = 1,
            name = "LS1",
            description = "test lump sum 1",
            cost = BigDecimal.ONE,
            splittingAllowed = true,
            phase = ProgrammeLumpSumPhase.Implementation,
            categories = mutableSetOf(categoryStaff, categoryOffice),
        )
        expectedLumpSum = ProgrammeLumpSum(
            id = testLumpSum.id,
            name = testLumpSum.name,
            description = testLumpSum.description,
            cost = testLumpSum.cost,
            splittingAllowed = testLumpSum.splittingAllowed,
            phase = testLumpSum.phase,
            categories = setOf(categoryStaff.category, categoryOffice.category),
        )
    }

    @Test
    fun getLumpSums() {
        every { repository.findAll(any<Pageable>()) } returns PageImpl(listOf(testLumpSum))
        assertThat(programmeLumpSumPersistence.getLumpSums(Pageable.unpaged()).content).containsExactly(
            expectedLumpSum
        )
    }

    @Test
    fun getLumpSum() {
        every { repository.findById(1L) } returns Optional.of(testLumpSum)
        assertThat(programmeLumpSumPersistence.getLumpSum(1L)).isEqualTo(expectedLumpSum)
    }

    @Test
    fun `getLumpSum - not existing`() {
        every { repository.findById(-1L) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { programmeLumpSumPersistence.getLumpSum(-1L) }
    }

    @Test
    fun createLumpSum() {
        val entitySaved = slot<ProgrammeLumpSumEntity>()
        every { repository.save(capture(entitySaved)) } returnsArgument 0

        val toSave = expectedLumpSum.copy(
            id = null,
            categories = setOf(StaffCosts, OfficeAndAdministrationCosts),
        )

        val result = programmeLumpSumPersistence.createLumpSum(toSave)

        assertThat(entitySaved.captured).isEqualTo(
            testLumpSum.copy(
                id = 0,
                categories = mutableSetOf(
                    ProgrammeLumpSumBudgetCategoryEntity(programmeLumpSumId = 0, category = StaffCosts),
                    ProgrammeLumpSumBudgetCategoryEntity(programmeLumpSumId = 0, category = OfficeAndAdministrationCosts),
                ),
            )
        )
        assertThat(result).isEqualTo(expectedLumpSum.copy(id = 0))
    }

    @Test
    fun updateLumpSum() {
        every { repository.findById(testLumpSum.id) } returns Optional.of(testLumpSum)

        val toBeUpdated = ProgrammeLumpSum(
            id = testLumpSum.id,
            name = "new name",
            description = "new description",
            cost = BigDecimal.TEN,
            splittingAllowed = false,
            phase = ProgrammeLumpSumPhase.Closure,
            categories = setOf(ExternalCosts, EquipmentCosts),
        )

        assertThat(programmeLumpSumPersistence.updateLumpSum(toBeUpdated)).isEqualTo(ProgrammeLumpSum(
            id = testLumpSum.id,
            name = "new name",
            description = "new description",
            cost = BigDecimal.TEN,
            splittingAllowed = false,
            phase = ProgrammeLumpSumPhase.Closure,
            categories = setOf(ExternalCosts, EquipmentCosts),
        ))
    }

    @Test
    fun `updateLumpSum - not existing`() {
        every { repository.findById(testLumpSum.id) } returns Optional.empty()

        val toBeUpdated = ProgrammeLumpSum(
            id = testLumpSum.id,
            name = "new name",
            description = "new description",
            cost = BigDecimal.TEN,
            splittingAllowed = false,
            phase = ProgrammeLumpSumPhase.Closure,
            categories = setOf(ExternalCosts, EquipmentCosts),
        )

        assertThrows<ResourceNotFoundException> { programmeLumpSumPersistence.updateLumpSum(toBeUpdated) }
    }

    @Test
    fun deleteLumpSum() {
        val ID = 555L
        every { repository.findById(ID) } returns Optional.of(testLumpSum.copy(id = ID))
        every { repository.delete(any()) } answers {}
        programmeLumpSumPersistence.deleteLumpSum(ID)
        verify { repository.delete(testLumpSum.copy(id = ID)) }
    }

    @Test
    fun `deleteLumpSum - not existing`() {
        every { repository.findById(-1) } returns Optional.empty()
        assertThrows<ResourceNotFoundException> { programmeLumpSumPersistence.deleteLumpSum(-1) }
    }

}