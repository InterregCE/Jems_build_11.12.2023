package io.cloudflight.jems.server.project.service.budget.get_project_budget

import io.cloudflight.jems.api.project.dto.partner.ProjectPartnerRole
import io.cloudflight.jems.server.audit.service.AuditService
import io.cloudflight.jems.server.project.service.budget.ProjectBudgetPersistence
import io.cloudflight.jems.server.project.service.budget.model.PartnerBudget
import io.cloudflight.jems.server.project.service.budget.model.ProjectPartnerCost
import io.cloudflight.jems.server.project.service.lumpsum.ProjectLumpSumPersistence
import io.cloudflight.jems.server.project.service.lumpsum.model.ProjectLumpSum
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerBudgetOptions
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetOptionsPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartner
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class GetProjectBudgetInteractorTest {

    companion object {
        private const val P1_ID = 1L
        private const val P2_ID = 2L
        private val partner1 = ProjectPartner(
            id = P1_ID,
            abbreviation = "PP 2",
            role = ProjectPartnerRole.PARTNER,
            sortNumber = 2,
            country = "SK"
        )
        private val partner2 = ProjectPartner(
            id = P2_ID,
            abbreviation = "LP 1",
            role = ProjectPartnerRole.LEAD_PARTNER,
            sortNumber = 1
        )

        private val partner1Options = ProjectPartnerBudgetOptions(
                partnerId = partner1.id!!,
                officeAndAdministrationOnStaffCostsFlatRate = 10,
                staffCostsFlatRate = 10,
                travelAndAccommodationOnStaffCostsFlatRate = 15,
        )

        private fun budget(partnerId: Long, sum: Double) = ProjectPartnerCost(
            partnerId = partnerId,
            sum = decimal(sum)
        )
        private fun decimal(value: Double) = BigDecimal.valueOf((value * 100).toLong(), 2)

        private val lumpSumId1: UUID = UUID.randomUUID()
        private val lumpSumId2: UUID = UUID.randomUUID()

        private val lumpSums = listOf(
            ProjectLumpSum(id = lumpSumId1, programmeLumpSumId = 40, period = 1, lumpSumContributions = emptyList()),
            ProjectLumpSum(id = lumpSumId2, programmeLumpSumId = 40, period = 2, lumpSumContributions = emptyList()),
        )
    }

    @MockK
    lateinit var persistence: ProjectBudgetPersistence

    @MockK
    lateinit var optionOptionsPersistence: ProjectPartnerBudgetOptionsPersistence

    @MockK
    lateinit var lumpSumPersistence: ProjectLumpSumPersistence

    @RelaxedMockK
    lateinit var auditService: AuditService

    private lateinit var getProjectBudgetInteractor: GetProjectBudgetInteractor

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        getProjectBudgetInteractor = GetProjectBudget(persistence, optionOptionsPersistence, lumpSumPersistence)
    }

    @Test
    fun getBudget() {
        every { persistence.getPartnersForProjectId(1) } returns listOf(partner1, partner2)
        every { optionOptionsPersistence.getBudgetOptions(setOf(P1_ID, P2_ID)) } returns listOf(partner1Options)
        every { persistence.getStaffCosts(setOf(P1_ID, P2_ID)) } returns listOf(budget(P1_ID, 50.0))
        every { persistence.getTravelCosts(setOf(P1_ID, P2_ID)) } returns listOf(budget(P1_ID, 800.0), budget(P2_ID, 100.0))
        every { persistence.getExternalCosts(setOf(P1_ID, P2_ID)) } returns listOf(budget(P2_ID, 1000.0))
        every { persistence.getEquipmentCosts(setOf(P1_ID, P2_ID)) } returns emptyList()
        every { persistence.getInfrastructureCosts(setOf(P1_ID, P2_ID)) } returns listOf(budget(P1_ID, 300.0), budget(P2_ID, 300.0))

        every { lumpSumPersistence.getLumpSums(1) } returns lumpSums
        val lumpSumIds = slot<Set<UUID>>()
        every { persistence.getLumpSumContributionPerPartner(capture(lumpSumIds)) } returns mapOf(
            P1_ID to BigDecimal.ONE,
            P2_ID to BigDecimal.TEN,
        )

        assertThat(getProjectBudgetInteractor.getBudget(1))
            .containsExactlyInAnyOrder(
                PartnerBudget(
                    partner = partner2,
                    staffCostsFlatRate = null,
                    officeAndAdministrationOnStaffCostsFlatRate = null,
                    travelAndAccommodationOnStaffCostsFlatRate = null,
                    otherCostsOnStaffCostsFlatRate = null,
                    travelCosts = decimal(100.0),
                    externalCosts = decimal(1000.0),
                    infrastructureCosts = decimal(300.0),
                    lumpSumContribution = BigDecimal.TEN,
                ),
                PartnerBudget(
                    partner = partner1,
                    staffCostsFlatRate = 10,
                    officeAndAdministrationOnStaffCostsFlatRate = 10,
                    travelAndAccommodationOnStaffCostsFlatRate = 15,
                    otherCostsOnStaffCostsFlatRate = null,
                    staffCosts = decimal(50.0),
                    travelCosts = decimal(800.0),
                    infrastructureCosts = decimal(300.0),
                    lumpSumContribution = BigDecimal.ONE,
                )
            )
        assertThat(lumpSumIds.captured).containsExactlyInAnyOrder(lumpSumId1, lumpSumId2)
    }

}
