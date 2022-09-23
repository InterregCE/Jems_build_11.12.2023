package io.cloudflight.jems.server.project.service.checklist.create

import io.cloudflight.jems.server.authentication.service.SecurityService
import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.project.service.checklist.model.ChecklistInstanceDetail
import io.cloudflight.jems.server.project.authorization.CanCreateChecklistAssessment
import io.cloudflight.jems.server.project.service.checklist.ChecklistInstancePersistence
import io.cloudflight.jems.server.project.service.checklist.checklistCreated
import io.cloudflight.jems.server.project.service.checklist.model.CreateChecklistInstanceModel
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateChecklistInstance(
    private val persistence: ChecklistInstancePersistence,
    private val securityService: SecurityService,
    private val auditPublisher: ApplicationEventPublisher
) : CreateChecklistInstanceInteractor {

    @CanCreateChecklistAssessment
    @Transactional
    @ExceptionWrapper(CreateChecklistInstanceException::class)
    override fun create(createCheckList: CreateChecklistInstanceModel): ChecklistInstanceDetail {
        return persistence.create(
            createChecklist = createCheckList,
            creatorId = securityService.currentUser?.user?.id!!
        ).also {
            auditPublisher.publishEvent(
                checklistCreated(
                    context = this,
                    checklist = createCheckList
                )
            )
        }
    }
}
