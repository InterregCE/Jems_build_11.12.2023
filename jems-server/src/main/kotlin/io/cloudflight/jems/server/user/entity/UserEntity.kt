package io.cloudflight.jems.server.user.entity

import io.cloudflight.jems.server.user.service.model.UserStatus
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import java.io.Serializable

@Entity(name = "account")
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    @field:NotNull
    var email: String,

    @field:NotNull
    var sendNotificationsToEmail: Boolean,

    @field:NotNull
    var name: String,

    @field:NotNull
    var surname: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_role_id")
    @field:NotNull
    var userRole: UserRoleEntity,

    @field:NotNull
    var password: String,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    var userStatus: UserStatus

) : Serializable {
    companion object {
        private const val serialVersionUID: Long = -2664483879421303925L
    }
}
// hibernate needs this to be Serializable since it's email property
// is used as primary key in FailedLoginAttemptEntity
