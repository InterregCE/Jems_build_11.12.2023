package io.cloudflight.jems.server.project.service.contracting.fileManagement.setInternalFileDescription

import io.cloudflight.jems.server.common.exception.ExceptionWrapper
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.common.file.service.JemsProjectFileService
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.authorization.CanSetProjectToContracted
import io.cloudflight.jems.server.project.service.report.partner.file.setDescriptionToFile.FileNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SetInternalFileDescription(
    private val filePersistence: JemsFilePersistence,
    private val fileService: JemsProjectFileService,
    private val generalValidator: GeneralValidatorService
): SetInternalFileDescriptionInteractor {


    @CanSetProjectToContracted
    @Transactional
    @ExceptionWrapper(SetDescriptionToInternalFileException::class)
    override fun setInternalFileDescription(projectId: Long, fileId: Long, description: String) {
        validateDescription(description)

        if (!filePersistence.existsFileByProjectIdAndFileIdAndFileTypeIn(
                projectId = projectId,
                fileId = fileId,
                setOf(JemsFileType.ContractInternal)
            )
        )
            throw FileNotFound()

        fileService.setDescription(fileId, description)
    }

    private fun validateDescription(text: String) {
        generalValidator.throwIfAnyIsInvalid(
            generalValidator.maxLength(text, 250, "description"),
        )
    }
}
