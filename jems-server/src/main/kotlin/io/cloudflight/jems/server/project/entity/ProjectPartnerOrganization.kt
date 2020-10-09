package io.cloudflight.jems.server.project.entity

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany


@Entity(name = "project_partner_organization")
data class ProjectPartnerOrganization (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val nameInOriginalLanguage: String?,

    @Column
    val nameInEnglish: String?,

    @Column
    val department: String?,

    @OneToMany(mappedBy = "partnerOrganizationDetailId.organizationId", cascade = [CascadeType.ALL])
    val organizationsDetails: Set<ProjectPartnerOrganizationDetails>?= emptySet()
)
