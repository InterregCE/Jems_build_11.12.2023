package io.cloudflight.jems.server.project.repository.partner.budget

import io.cloudflight.jems.server.project.entity.partner.budget.unit_cost.ProjectPartnerBudgetUnitCostRow
import io.cloudflight.jems.server.project.service.partner.model.BudgetStaffCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetTravelAndAccommodationCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetGeneralCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetUnitCostEntry
import io.cloudflight.jems.server.project.service.partner.model.BudgetSpfCostEntry
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal

/**
 * tests implementation of ProjectPartnerBudgetCostsPersistence including mappings and projectVersionUtils.
 */
class ProjectPartnerBudgetCostsPersistenceProviderTest : ProjectPartnerBudgetCostsPersistenceProviderTestBase() {

    @TestFactory
    fun `should return current version of budget costs`() =
        testInputsForGettingCurrentVerionOfBudgetCosts().map {
            DynamicTest.dynamicTest(
                "should return current version of budget ${it.name}"
            ) {
                every { it.repository.findAllByBasePropertiesPartnerIdInOrderByIdAsc(setOf(partnerId)) } returns listOf(it.entity)
                assertThat(it.callback.invoke(setOf(partnerId), null)).containsExactly(it.expectedResult)
            }
        }

    @TestFactory
    fun `should return previous version of budget costs`() =
        testInputsForGettingPreviousVersionOfBudgetCosts().map {
            DynamicTest.dynamicTest(
                "should return previous version of budget ${it.name}"
            ) {
                if (it.isForGettingUnitCosts) {
                    every {
                        (it.repository as ProjectPartnerBudgetUnitCostRepository)
                            .findAllByPartnerIdAsOfTimestamp(setOf(partnerId), timestamp)
                    } returns listOf(it.row as ProjectPartnerBudgetUnitCostRow)
                } else {
                    every {
                        it.repository.findAllByPartnerIdAsOfTimestamp(setOf(partnerId), timestamp, it.projectClass.java)
                    } returns listOf(it.row)
                }
                assertThat(it.callback.invoke(setOf(partnerId), version)).containsExactly(it.expectedResult)
            }
        }

    @TestFactory
    fun `should return current version of budget costs total`() =
        testInputsForGettingCostsTotal().map {
            DynamicTest.dynamicTest(
                "should return current version of budget ${it.first}"
            ) {
                every { it.second.sumTotalForPartner(partnerId) } returns BigDecimal.TEN
                assertThat(it.third.invoke(partnerId, null)).isEqualTo(BigDecimal.TEN)
                verify { it.second.sumTotalForPartner(partnerId) }

            }
        }

    @TestFactory
    fun `should return previous version of budget costs total`() {
        testInputsForGettingCostsTotal().map {
            DynamicTest.dynamicTest(
                "should return previous version of budget ${it.first}"
            ) {
                every { it.second.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) } returns BigDecimal.ONE
                assertThat(it.third.invoke(partnerId, null)).isEqualTo(BigDecimal.ONE)
                verify { it.second.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) }
            }
        }
    }

    @TestFactory
    fun `should return zero as current version of budget costs total when sum of costs is null`() =
        testInputsForGettingCostsTotal().map {
            DynamicTest.dynamicTest(
                "should return zero as current version of budget ${it.first}  when sum of costs is null"
            ) {
                every { it.second.sumTotalForPartner(partnerId) } returns null
                assertThat(it.third.invoke(partnerId, null)).isEqualTo(BigDecimal.ZERO)
                verify { it.second.sumTotalForPartner(partnerId) }

            }
        }

    @TestFactory
    fun `should return zero as previous version of budget costs total when sum of costs is null`() {
        testInputsForGettingCostsTotal().map {
            DynamicTest.dynamicTest(
                "should return zero as previous version of budget ${it.first} when sum of costs is null"
            ) {
                every { it.second.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) } returns null
                assertThat(it.third.invoke(partnerId, null)).isEqualTo(BigDecimal.ZERO)
                verify { it.second.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) }
            }
        }
    }

    @Test
    fun `should return current version of budget lump sums total`() {
        every { budgetPartnerLumpSumRepository.sumTotalForPartner(partnerId) } returns BigDecimal.TEN
        assertThat(persistence.getBudgetLumpSumsCostTotal(partnerId)).isEqualTo(BigDecimal.TEN)
        verify { budgetPartnerLumpSumRepository.sumTotalForPartner(partnerId) }
    }

    @Test
    fun `should return previous version of budget lump sums total`() {
        every { budgetPartnerLumpSumRepository.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) } returns BigDecimal.ONE
        assertThat(persistence.getBudgetLumpSumsCostTotal(partnerId, version)).isEqualTo(BigDecimal.ONE)
        verify { budgetPartnerLumpSumRepository.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) }
    }

    @Test
    fun `should return zero as current version of budget lump sums total when sum of costs is null`() {
        every { budgetPartnerLumpSumRepository.sumTotalForPartner(partnerId) } returns null
        assertThat(persistence.getBudgetLumpSumsCostTotal(partnerId)).isEqualTo(BigDecimal.ZERO)
        verify { budgetPartnerLumpSumRepository.sumTotalForPartner(partnerId) }
    }

    @Test
    fun `should return zero as previous version of budget lump sums total when sum of costs is null`() {
        every { budgetPartnerLumpSumRepository.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) } returns null
        assertThat(persistence.getBudgetLumpSumsCostTotal(partnerId, version)).isEqualTo(BigDecimal.ZERO)
        verify { budgetPartnerLumpSumRepository.sumTotalForPartnerAsOfTimestamp(partnerId, timestamp) }
    }

    @Test
    fun `should return empty list of budget staff costs when partner set is empty`() {
        assertThat(persistence.getBudgetStaffCosts(setOf())).isEqualTo(listOf<BudgetStaffCostEntry>())
    }

    @Test
    fun `should return empty list of travel and accommodation costs when partner set is empty`() {
        assertThat(persistence.getBudgetTravelAndAccommodationCosts(setOf())).isEqualTo(listOf<BudgetTravelAndAccommodationCostEntry>())
    }

    @Test
    fun `should return empty list of budget general costs when partner set is empty - infrastructure & works costs`() {
        assertThat(persistence.getBudgetInfrastructureAndWorksCosts(setOf())).isEqualTo(listOf<BudgetGeneralCostEntry>())
    }

    @Test
    fun `should return empty list of budget unit costs when partner set is empty`() {
        assertThat(persistence.getBudgetUnitCosts(setOf())).isEqualTo(listOf<BudgetUnitCostEntry>())
    }

    @Test
    fun `should return empty list of budget general cost when partner set is empty - external expertise & services costs`() {
        assertThat(persistence.getBudgetExternalExpertiseAndServicesCosts(setOf())).isEqualTo(listOf<BudgetGeneralCostEntry>())
    }

    @Test
    fun `should return empty list of budget general cost when partner set is empty - equipment costs`() {
        assertThat(persistence.getBudgetEquipmentCosts(setOf())).isEqualTo(listOf<BudgetGeneralCostEntry>())
    }

    @Test
    fun `should return empty list of spf cost when partner set is empty`() {
        assertThat(persistence.getBudgetSpfCosts(setOf())).isEqualTo(listOf<BudgetSpfCostEntry>())
    }
}
