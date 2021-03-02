package io.cloudflight.jems.server.project.entity.partner.budget

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import java.io.Serializable
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Embeddable
class BudgetTranslation<T: ProjectPartnerBudgetBase>(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    @field:NotNull
    val budget: T,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    val language: SystemLanguage

) : Serializable {
    override fun equals(other: Any?) =
        this === other ||
            other !== null &&
            other is BudgetTranslation<*> &&
            budget == other.budget &&
            language == other.language

    override fun hashCode() =
        budget.hashCode().plus(language.translationKey.hashCode())

}