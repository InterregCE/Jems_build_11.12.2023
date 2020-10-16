package io.cloudflight.jems.server.project.service.associatedorganization

import io.cloudflight.jems.api.project.dto.associatedorganization.InputProjectAssociatedOrganizationAddress
import io.cloudflight.jems.api.project.dto.associatedorganization.InputProjectAssociatedOrganizationCreate
import io.cloudflight.jems.api.project.dto.InputProjectContact
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganization
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganizationAddress
import io.cloudflight.jems.api.project.dto.associatedorganization.OutputProjectAssociatedOrganizationDetail
import io.cloudflight.jems.api.project.dto.partner.OutputProjectPartnerContact
import io.cloudflight.jems.server.project.entity.Address
import io.cloudflight.jems.server.project.entity.Contact
import io.cloudflight.jems.server.project.entity.partner.ProjectPartnerContactId
import io.cloudflight.jems.server.project.entity.Project
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganization
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationAddress
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationContact
import io.cloudflight.jems.server.project.entity.associatedorganization.ProjectAssociatedOrganizationContactId
import io.cloudflight.jems.server.project.entity.partner.ProjectPartner
import io.cloudflight.jems.server.project.service.partner.toOutputProjectPartner

fun InputProjectAssociatedOrganizationCreate.toEntity(
    partner: ProjectPartner
) = ProjectAssociatedOrganization(
    project = partner.project,
    partner = partner,
    nameInOriginalLanguage = nameInOriginalLanguage,
    nameInEnglish = nameInEnglish
    // addresses - need organization Id
    // contacts - need organization Id
)

fun InputProjectAssociatedOrganizationAddress.toEntity(organizationId: Long) = ProjectAssociatedOrganizationAddress(
    organizationId = organizationId,
    address = Address(
        country = country,
        nutsRegion2 = nutsRegion2,
        nutsRegion3 = nutsRegion3,
        street = street,
        houseNumber = houseNumber,
        postalCode = postalCode,
        city = city,
        homepage = homepage
    )
)

fun ProjectAssociatedOrganization.toOutputProjectAssociatedOrganization() = OutputProjectAssociatedOrganization(
    id = id!!,
    partnerAbbreviation = partner.abbreviation,
    nameInOriginalLanguage = nameInOriginalLanguage,
    nameInEnglish = nameInEnglish,
    sortNumber = sortNumber
)

fun ProjectAssociatedOrganization.toOutputProjectAssociatedOrganizationDetail() = OutputProjectAssociatedOrganizationDetail(
    id = id!!,
    partner = partner.toOutputProjectPartner(),
    nameInOriginalLanguage = nameInOriginalLanguage,
    nameInEnglish = nameInEnglish,
    sortNumber = sortNumber,
    address = addresses.map { it.toOutputProjectAssociatedOrganizationDetails() }.firstOrNull(),
    contacts = contacts.map { it.toOutputProjectAssociatedOrganizationContact() }
)

fun InputProjectContact.toEntity(associatedOrganization: ProjectAssociatedOrganization) = ProjectAssociatedOrganizationContact(
    contactId = ProjectAssociatedOrganizationContactId(associatedOrganization.id!!, type),
    contact = Contact(
        title = title,
        firstName = firstName,
        lastName = lastName,
        email = email,
        telephone = telephone
    )
)

fun ProjectAssociatedOrganizationContact.toOutputProjectAssociatedOrganizationContact() = OutputProjectPartnerContact(
    type = contactId.type,
    title = contact?.title,
    firstName = contact?.firstName,
    lastName = contact?.lastName,
    email = contact?.email,
    telephone = contact?.telephone
)

fun ProjectAssociatedOrganizationAddress.toOutputProjectAssociatedOrganizationDetails() = OutputProjectAssociatedOrganizationAddress(
    country = address?.country,
    nutsRegion2 = address?.nutsRegion2,
    nutsRegion3 = address?.nutsRegion3,
    street = address?.street,
    houseNumber = address?.houseNumber,
    postalCode = address?.postalCode,
    city = address?.city,
    homepage = address?.homepage
)
