package io.cloudflight.jems.server.project.service.contracting.fileManagement.setPartnerFileDescription

import io.cloudflight.jems.server.UnitTest
import io.cloudflight.jems.server.common.file.service.JemsFilePersistence
import io.cloudflight.jems.server.common.file.service.JemsProjectFileService
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import io.cloudflight.jems.server.common.validator.AppInputValidationException
import io.cloudflight.jems.server.common.validator.GeneralValidatorService
import io.cloudflight.jems.server.project.service.contracting.ContractingModificationDeniedException
import io.cloudflight.jems.server.project.service.contracting.ContractingValidator
import io.cloudflight.jems.server.project.service.report.partner.file.setDescriptionToFile.FileNotFound
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SetPartnerFileDescriptionTest: UnitTest() {

    companion object {
        private const val PARTNER_ID = 20L
        private const val FILE_ID = 30L
    }

    @MockK
    lateinit var filePersistence: JemsFilePersistence

    @MockK
    lateinit var fileService: JemsProjectFileService

    @MockK
    lateinit var generalValidator: GeneralValidatorService

    @MockK
    lateinit var validator: ContractingValidator

    @InjectMockKs
    lateinit var setPartnerFileDescription: SetPartnerFileDescription

    @BeforeEach
    fun setup() {
        clearMocks(generalValidator, filePersistence, fileService)
        every { generalValidator.throwIfAnyIsInvalid(*varargAny { it.isEmpty() }) } returns Unit
        every { generalValidator.throwIfAnyIsInvalid(*varargAny { it.isNotEmpty() }) } throws
            AppInputValidationException(emptyMap())
        every { generalValidator.maxLength(any<String>(), 250, "description") } returns emptyMap()
    }

    @Test
    fun setDescription() {
        every { validator.validatePartnerLock(PARTNER_ID) } returns Unit
        every {
            filePersistence.existsFileByPartnerIdAndFileIdAndFileTypeIn(
                PARTNER_ID,
                FILE_ID,
                setOf(JemsFileType.ContractPartnerDoc)
            )
        } returns true
        every { fileService.setDescription(FILE_ID, "new desc") } answers { }

        setPartnerFileDescription.setPartnerFileDescription(PARTNER_ID, FILE_ID, "new desc")
        verify(exactly = 1) { fileService.setDescription(FILE_ID, "new desc") }
    }

    @Test
    fun `setDescription - does not exist`() {
        every { validator.validatePartnerLock(PARTNER_ID) } returns Unit
        every {
            filePersistence.existsFileByPartnerIdAndFileIdAndFileTypeIn(
                PARTNER_ID,
                FILE_ID,
                setOf(JemsFileType.ContractPartnerDoc)
            )
        } returns false
        assertThrows<FileNotFound> { setPartnerFileDescription.setPartnerFileDescription(PARTNER_ID, FILE_ID, "new desc") }
    }

    @Test
    fun `setDescription - section locked`() {
        val exception = ContractingModificationDeniedException()
        every { validator.validatePartnerLock(PARTNER_ID) } throws exception

        assertThrows<ContractingModificationDeniedException> {
            setPartnerFileDescription.setPartnerFileDescription(PARTNER_ID, FILE_ID, "new desc")
        }
    }
}
