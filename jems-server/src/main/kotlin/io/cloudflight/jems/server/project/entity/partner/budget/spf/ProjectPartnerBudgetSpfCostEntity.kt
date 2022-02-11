package io.cloudflight.jems.server.project.entity.partner.budget.spf

import io.cloudflight.jems.server.project.entity.partner.budget.BaseBudgetProperties
import io.cloudflight.jems.server.project.entity.partner.budget.ProjectPartnerBudgetBase
import java.math.BigDecimal
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity(name = "project_partner_budget_spfcost")
class ProjectPartnerBudgetSpfCostEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long = 0,

    @Embedded
    @field:NotNull
    override val baseProperties: BaseBudgetProperties,

    @Column
    @field:NotNull
    val pricePerUnit: BigDecimal,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "translationId.sourceEntity")
    val translatedValues: MutableSet<ProjectPartnerBudgetSpfCostTranslEntity> = mutableSetOf(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "budgetPeriodId.budget")
    val budgetPeriodEntities: MutableSet<ProjectPartnerBudgetSpfCostPeriodEntity>,

    val unitCostId: Long?

) : ProjectPartnerBudgetBase {

    override fun equals(other: Any?) =
        this === other ||
            other !== null &&
            other is ProjectPartnerBudgetSpfCostEntity &&
            id > 0 &&
            id == other.id

    override fun hashCode() =
        super.hashCode()

}
