package io.cloudflight.jems.server.project.repository.partner.budget

import io.cloudflight.jems.server.project.entity.partner.budget.ProjectPartnerBudgetEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

@NoRepositoryBean
interface ProjectPartnerBaseBudgetRepository<T> : CrudRepository<T, Long> {

    fun findAllByBasePropertiesPartnerIdOrderByIdAsc(partnerId: Long): List<T>

    @Query("SELECT SUM(e.baseProperties.rowSum) FROM #{#entityName} e WHERE e.baseProperties.partnerId = :id")
    fun sumTotalForPartner(@Param("id") partnerId: Long): BigDecimal?

    @Query("SELECT new io.cloudflight.jems.server.project.entity.partner.budget.ProjectPartnerBudgetEntity(e.baseProperties.partnerId, SUM(e.baseProperties.rowSum)) FROM #{#entityName} e WHERE e.baseProperties.partnerId IN :ids GROUP BY e.baseProperties.partnerId")
    fun sumForAllPartners(@Param("ids") partnerIds: Set<Long>): List<ProjectPartnerBudgetEntity>

    fun deleteAllByBasePropertiesPartnerId(partnerId: Long)

    fun deleteAllByBasePropertiesPartnerIdAndIdNotIn(partnerId: Long, idList: List<Long>)
}