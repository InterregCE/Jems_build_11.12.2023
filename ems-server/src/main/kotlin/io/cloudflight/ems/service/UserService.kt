package io.cloudflight.ems.service

import io.cloudflight.ems.api.dto.OutputUser
import io.cloudflight.ems.entity.Account
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserService {
    fun findOneByEmail(email: String): Account?
    fun getByEmail(email: String): OutputUser?
    fun findAll(pageable: Pageable): Page<OutputUser>
}
