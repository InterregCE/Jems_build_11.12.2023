package io.cloudflight.ems.service;

import io.cloudflight.ems.api.dto.user.OutputUserRole
import io.cloudflight.ems.repository.AccountRoleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountRoleServiceImpl(
    private val accountRoleRepository: AccountRoleRepository
) : AccountRoleService {

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<OutputUserRole> {
        return accountRoleRepository.findAll(pageable).map { it.toOutputUserRole() }
    }

}