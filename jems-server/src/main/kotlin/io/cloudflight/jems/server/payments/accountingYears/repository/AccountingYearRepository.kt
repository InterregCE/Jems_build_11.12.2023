package io.cloudflight.jems.server.payments.accountingYears.repository

import io.cloudflight.jems.server.payments.entity.AccountingYearEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountingYearRepository : JpaRepository<AccountingYearEntity, Long> {

    fun findAllByOrderByYear(): List<AccountingYearEntity>

}
