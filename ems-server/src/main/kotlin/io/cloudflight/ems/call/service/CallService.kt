package io.cloudflight.ems.call.service

import io.cloudflight.ems.api.call.dto.InputCallCreate
import io.cloudflight.ems.api.call.dto.InputCallUpdate
import io.cloudflight.ems.api.call.dto.OutputCall
import io.cloudflight.ems.api.call.dto.OutputCallList
import io.cloudflight.ems.api.call.dto.OutputCallProgrammePriority
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CallService {

    fun getCallById(id: Long): OutputCall

    /**
     * List of calls is restricted based on actual user role.
     */
    fun getCalls(pageable: Pageable): Page<OutputCallList>

    fun createCall(inputCall: InputCallCreate): OutputCall

    fun updateCall(inputCall: InputCallUpdate): OutputCall

    fun publishCall(callId: Long): OutputCall

    fun findOneByName(name: String): OutputCall?

    fun getPriorityAndPoliciesForCall(callId: Long): List<OutputCallProgrammePriority>

}
