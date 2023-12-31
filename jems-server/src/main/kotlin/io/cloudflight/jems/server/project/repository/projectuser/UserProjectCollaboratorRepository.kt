package io.cloudflight.jems.server.project.repository.projectuser

import io.cloudflight.jems.server.project.entity.projectuser.UserProjectCollaboratorEntity
import io.cloudflight.jems.server.project.entity.projectuser.UserProjectId
import io.cloudflight.jems.server.user.service.model.assignment.CollaboratorAssignedToProject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserProjectCollaboratorRepository : JpaRepository<UserProjectCollaboratorEntity, UserProjectId> {

    fun findAllByIdUserId(userId: Long): Iterable<UserProjectCollaboratorEntity>

    @Query("""
        SELECT new io.cloudflight.jems.server.user.service.model.assignment.CollaboratorAssignedToProject(
            ap.id.userId,
            a.email,
            a.sendNotificationsToEmail,
            a.userStatus,
            ap.level)
        FROM #{#entityName} AS ap
        LEFT JOIN account a on a.id = ap.id.userId
        WHERE ap.id.projectId = :projectId
        ORDER BY a.email
    """)
    fun findAllByProjectId(projectId: Long): List<CollaboratorAssignedToProject>

    fun findAllByIdProjectId(projectId: Long): Iterable<UserProjectCollaboratorEntity>

    fun deleteAllByIdIn(id: Collection<UserProjectId>)

}
