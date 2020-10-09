package io.cloudflight.jems.server.project.service

import io.cloudflight.jems.api.project.dto.InputProjectPartnerContact
import io.cloudflight.jems.api.project.dto.InputProjectPartnerContribution
import io.cloudflight.jems.api.project.dto.InputProjectPartnerCreate
import io.cloudflight.jems.api.project.dto.InputProjectPartnerOrganization
import io.cloudflight.jems.api.project.dto.InputProjectPartnerOrganizationDetails
import io.cloudflight.jems.api.project.dto.OutputProjectPartner
import io.cloudflight.jems.api.project.dto.OutputProjectPartnerContact
import io.cloudflight.jems.api.project.dto.OutputProjectPartnerContribution
import io.cloudflight.jems.api.project.dto.OutputProjectPartnerDetail
import io.cloudflight.jems.api.project.dto.OutputProjectPartnerOrganization
import io.cloudflight.jems.api.project.dto.OutputProjectPartnerOrganizationDetails
import io.cloudflight.jems.server.project.entity.Project
import io.cloudflight.jems.server.project.entity.ProjectPartner
import io.cloudflight.jems.server.project.entity.PartnerContactPersonId
import io.cloudflight.jems.server.project.entity.PartnerContactPerson
import io.cloudflight.jems.server.project.entity.ProjectPartnerContribution
import io.cloudflight.jems.server.project.entity.ProjectPartnerOrganization
import io.cloudflight.jems.server.project.entity.ProjectPartnerOrganizationDetails
import io.cloudflight.jems.server.project.entity.PartnerOrganizationDetailId


fun InputProjectPartnerCreate.toEntity(project: Project) = ProjectPartner(
    name = name!!,
    project = project,
    role = role!!
)

fun ProjectPartner.toOutputProjectPartner() = OutputProjectPartner(
    id = id,
    name = name,
    role = role,
    sortNumber = sortNumber
)

fun ProjectPartner.toOutputProjectPartnerDetail() = OutputProjectPartnerDetail(
    id = id,
    name = name,
    role = role,
    sortNumber = sortNumber,
    partnerContactPersons = partnerContactPersons?.map { it.toOutputProjectPartnerContact() }?.toHashSet(),
    partnerContribution = partnerContribution?.toOutputProjectPartnerContribution(),
    organization = organization?.toOutputProjectPartnerOrganization()
)

fun InputProjectPartnerContact.toEntity(partner: ProjectPartner) = PartnerContactPerson(
    partnerContactPersonId = PartnerContactPersonId(partner.id!!, type),
    title = title,
    firstName =  firstName,
    lastName = lastName,
    email = email,
    telephone = telephone
)

fun PartnerContactPerson.toOutputProjectPartnerContact() = OutputProjectPartnerContact(
    type = partnerContactPersonId.type,
    title = title,
    firstName =  firstName,
    lastName = lastName,
    email = email,
    telephone = telephone
)

fun InputProjectPartnerContribution.toEntity(partner: ProjectPartner) = ProjectPartnerContribution(
    partnerId = partner.id!!,
    partner = partner,
    organizationRelevance = organizationRelevance,
    organizationRole = organizationRole,
    organizationExperience = organizationExperience
)

fun ProjectPartnerContribution.toOutputProjectPartnerContribution() = OutputProjectPartnerContribution(
    organizationRelevance = organizationRelevance,
    organizationRole = organizationRole,
    organizationExperience = organizationExperience
)

fun InputProjectPartnerOrganization.toEntity() = ProjectPartnerOrganization(
    id = id,
    nameInOriginalLanguage = nameInOriginalLanguage,
    nameInEnglish = nameInEnglish,
    department = department
)

fun ProjectPartnerOrganization.toOutputProjectPartnerOrganization() = OutputProjectPartnerOrganization(
    id = id,
    nameInOriginalLanguage = nameInOriginalLanguage,
    nameInEnglish = nameInEnglish,
    department = department,
    organizationDetails = organizationsDetails?.map { it.toOutputProjectPartnerOrganizationDetails() }?.toHashSet()
)

fun ProjectPartnerOrganizationDetails.toOutputProjectPartnerOrganizationDetails() = OutputProjectPartnerOrganizationDetails(
    type = partnerOrganizationDetailId.type,
    country = country,
    nutsRegion2 = nutsRegion2,
    nutsRegion3 = nutsRegion3,
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    homepage = homepage
)

fun InputProjectPartnerOrganizationDetails.toEntity(partnerOrganization: ProjectPartnerOrganization) = ProjectPartnerOrganizationDetails(
    partnerOrganizationDetailId = PartnerOrganizationDetailId(partnerOrganization.id!!, type),
    country = country,
    nutsRegion2 = nutsRegion2,
    nutsRegion3 = nutsRegion3,
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    homepage = homepage
)
