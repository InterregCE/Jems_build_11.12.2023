package io.cloudflight.jems.server.project.service.contracting.fileManagement.listPartnerFiles

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.common.file.service.model.JemsFile
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import io.cloudflight.jems.server.project.authorization.CanRetrieveProjectContractingPartner
import io.cloudflight.jems.server.project.service.contracting.fileManagement.PARTNER_ALLOWED_FILE_TYPES
import io.cloudflight.jems.server.project.service.contracting.fileManagement.validateConfiguration
import io.cloudflight.jems.server.project.service.contracting.model.ProjectContractingFileSearchRequest
import io.cloudflight.jems.server.project.service.partner.PartnerPersistence
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListContractingPartnerFiles(
    private val partnerPersistence: PartnerPersistence,
    private val filePersistence: JemsFilePersistence
): ListContractingPartnerFilesInteractor {

    @CanRetrieveProjectContractingPartner
    @Transactional(readOnly = true)
    @ExceptionWrapper(ListContractingPartnerFilesException::class)
    override fun listPartner(
        partnerId: Long,
        pageable: Pageable,
        searchRequest: ProjectContractingFileSearchRequest
    ): Page<JemsFile> {

        validateConfiguration(searchRequest = searchRequest, partnerId, PARTNER_ALLOWED_FILE_TYPES)
        val projectId = partnerPersistence.getProjectIdForPartnerId(partnerId)
        val filePathPrefix = JemsFileType.ContractPartnerDoc.generatePath(projectId, partnerId)

        return filePersistence.listAttachments(
            pageable = pageable,
            indexPrefix = filePathPrefix,
            filterSubtypes = searchRequest.filterSubtypes,
            filterUserIds = emptySet(),
        )
    }

}
