package io.cloudflight.jems.server.project.entity.partner.budget.staff_cost

import io.cloudflight.jems.server.project.entity.BudgetTranslation
import io.cloudflight.jems.server.project.entity.partner.budget.ProjectPartnerBudgetTranslBase
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity(name = "project_partner_budget_staff_cost_transl")
data class ProjectPartnerBudgetStaffCostTranslEntity(

    @EmbeddedId
    override val budgetTranslation: BudgetTranslation<ProjectPartnerBudgetStaffCostEntity>,

    val description: String? = null,

    val comment: String? = null

) : ProjectPartnerBudgetTranslBase {
    override fun equals(other: Any?) =
        this === other ||
            other !== null &&
            other is ProjectPartnerBudgetStaffCostTranslEntity &&
            budgetTranslation == other.budgetTranslation

    override fun hashCode() =
        if (budgetTranslation.budget.id <= 0) super.hashCode()
        else budgetTranslation.budget.id.toInt().plus(budgetTranslation.language.translationKey.hashCode())
}