package io.cloudflight.jems.server.project.service.file.model

import io.cloudflight.jems.server.common.file.service.model.JemsFileCreate
import io.cloudflight.jems.server.common.file.service.model.JemsFileType
import java.io.InputStream

data class ProjectFile(
    val stream: InputStream,
    val name: String,
    val size: Long
) {
    fun getFileMetadata(
        projectId: Long?,
        partnerId: Long?,
        location: String,
        type: JemsFileType,
        userId: Long,
    ) = JemsFileCreate(
        projectId = projectId,
        partnerId = partnerId,
        name = name,
        path = location,
        type = type,
        size = size,
        content = stream,
        userId = userId,
    )
}
