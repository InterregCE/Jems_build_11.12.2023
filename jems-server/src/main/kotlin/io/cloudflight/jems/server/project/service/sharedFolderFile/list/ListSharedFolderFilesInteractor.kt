package io.cloudflight.jems.server.project.service.sharedFolderFile.list

import io.cloudflight.jems.server.common.file.service.model.JemsFile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ListSharedFolderFilesInteractor {

    fun list(projectId: Long, pageable: Pageable): Page<JemsFile>
}
