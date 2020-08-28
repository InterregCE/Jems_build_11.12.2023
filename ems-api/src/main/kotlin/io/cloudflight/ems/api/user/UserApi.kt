package io.cloudflight.ems.api.user

import io.cloudflight.ems.api.user.dto.InputPassword
import io.cloudflight.ems.api.user.dto.InputUserCreate
import io.cloudflight.ems.api.user.dto.InputUserUpdate
import io.cloudflight.ems.api.user.dto.OutputUserWithRole
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.Valid

@Api("User")
@RequestMapping("/api/user")
interface UserApi {

    @ApiOperation("Returns the users")
    @ApiImplicitParams(
        ApiImplicitParam(paramType = "query", name = "page", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "size", dataType = "integer"),
        ApiImplicitParam(paramType = "query", name = "sort", dataType = "string")
    )
    @GetMapping
    fun list(pageable: Pageable): Page<OutputUserWithRole>

    @ApiOperation("Creates new User")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createUser(@Valid @RequestBody user: InputUserCreate): OutputUserWithRole

    @ApiOperation("Updates a User")
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@Valid @RequestBody user: InputUserUpdate): OutputUserWithRole

    @ApiOperation("Returns a user by id")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): OutputUserWithRole

    @ApiOperation("Changes user password")
    @PutMapping("/{userId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun changePassword(@PathVariable userId: Long, @Valid @RequestBody passwordData: InputPassword)

}