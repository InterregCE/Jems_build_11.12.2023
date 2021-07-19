package io.cloudflight.jems.server.project.service.budget.get_project_budget

import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectForm
import io.cloudflight.jems.server.project.service.budget.ProjectBudgetPersistence
import io.cloudflight.jems.server.project.service.budget.model.BudgetCostsCalculationResult
import io.cloudflight.jems.server.project.service.budget.model.PartnerBudget
import io.cloudflight.jems.server.project.service.budget.model.ProjectPartnerCost
import io.cloudflight.jems.server.project.service.common.BudgetCostsCalculatorService
import io.cloudflight.jems.server.project.service.partner.budget.ProjectPartnerBudgetOptionsPersistence
import io.cloudflight.jems.server.project.service.partner.model.ProjectPartner
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class GetProjectBudget(
    private val persistence: ProjectBudgetPersistence,
    private val optionPersistence: ProjectPartnerBudgetOptionsPersistence,
    private val budgetCostsCalculator: BudgetCostsCalculatorService
) : GetProjectBudgetInteractor {

    @Transactional(readOnly = true)
    @CanRetrieveProjectForm
    override fun getBudget(projectId: Long, version: String?): List<PartnerBudget> {
        val partners = persistence.getPartnersForProjectId(projectId = projectId, version).associateBy { it.id!! }

        val options =
            optionPersistence.getBudgetOptions(partners.keys, projectId, version).iterator().asSequence().associateBy { it.partnerId }

        val lumpSumContributionPerPartner = persistence.getLumpSumContributionPerPartner(partners.keys, projectId, version)
        val unitCostsPerPartner = persistence.getUnitCostsPerPartner(partners.keys, projectId, version)

        val externalCostsPerPartner = persistence.getExternalCosts(partners.keys, projectId, version).groupByPartnerId()
        val equipmentCostsPerPartner = persistence.getEquipmentCosts(partners.keys, projectId, version).groupByPartnerId()
        val infrastructureCostsPerPartner = persistence.getInfrastructureCosts(partners.keys, projectId, version).groupByPartnerId()

        val staffCostsPerPartner =
            persistence.getStaffCosts(partners.filter { options[it.key]?.staffCostsFlatRate == null }.keys, projectId, version)
                .groupByPartnerId()
        val travelCostsPerPartner =
            persistence.getTravelCosts(partners.filter { options[it.key]?.travelAndAccommodationOnStaffCostsFlatRate == null }.keys, projectId, version)
                .groupByPartnerId()

        return partners.map { (partnerId, partner) ->
            val unitCosts = unitCostsPerPartner[partnerId] ?: BigDecimal.ZERO
            val lumpSumsCosts = lumpSumContributionPerPartner[partnerId] ?: BigDecimal.ZERO
            val externalCosts = externalCostsPerPartner[partnerId] ?: BigDecimal.ZERO
            val equipmentCosts = equipmentCostsPerPartner[partnerId] ?: BigDecimal.ZERO
            val infrastructureCosts = infrastructureCostsPerPartner[partnerId] ?: BigDecimal.ZERO
            budgetCostsCalculator.calculateCosts(
                options[partnerId],
                unitCosts = unitCosts,
                lumpSumsCosts = lumpSumsCosts,
                externalCosts = externalCosts,
                equipmentCosts = equipmentCosts,
                infrastructureCosts = infrastructureCosts,
                travelCosts = travelCostsPerPartner[partnerId] ?: BigDecimal.ZERO,
                staffCosts = staffCostsPerPartner[partnerId] ?: BigDecimal.ZERO,
            ).toPartnerBudget(
                partner,
                unitCosts = unitCosts,
                lumpSumCosts = lumpSumsCosts,
                externalCosts = externalCosts,
                equipmentCosts = equipmentCosts,
                infrastructureCosts = infrastructureCosts,
            )
        }
    }

    private fun Collection<ProjectPartnerCost>.groupByPartnerId() = associateBy({ it.partnerId }, { it.sum })

    private fun BudgetCostsCalculationResult.toPartnerBudget(
        partner: ProjectPartner,
        unitCosts: BigDecimal,
        lumpSumCosts: BigDecimal,
        externalCosts: BigDecimal,
        equipmentCosts: BigDecimal,
        infrastructureCosts: BigDecimal
    ) =
        PartnerBudget(
            partner = partner,
            staffCosts = this.staffCosts,
            travelCosts = this.travelCosts,
            externalCosts = externalCosts,
            equipmentCosts = equipmentCosts,
            infrastructureCosts = infrastructureCosts,
            officeAndAdministrationCosts = this.officeAndAdministrationCosts,
            otherCosts = this.otherCosts,
            lumpSumContribution = lumpSumCosts,
            unitCosts = unitCosts,
            totalCosts = this.totalCosts
        )
}
