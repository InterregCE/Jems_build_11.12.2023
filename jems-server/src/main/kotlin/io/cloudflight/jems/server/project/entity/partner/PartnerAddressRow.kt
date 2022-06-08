package io.cloudflight.jems.server.project.entity.partner

import io.cloudflight.jems.server.project.service.partner.model.ProjectPartnerAddressType

interface PartnerAddressRow {
    val type: ProjectPartnerAddressType
    val country: String?
    val countryCode: String?
    val nutsRegion2: String?
    val nutsRegion2Code: String?
    val nutsRegion3: String?
    val nutsRegion3Code: String?
    val street: String?
    val houseNumber: String?
    val postalCode: String?
    val city: String?
    val homepage: String?
}
