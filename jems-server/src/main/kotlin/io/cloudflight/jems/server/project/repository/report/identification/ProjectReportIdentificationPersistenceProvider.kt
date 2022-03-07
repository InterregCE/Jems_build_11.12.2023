package io.cloudflight.jems.server.project.repository.report.identification

import io.cloudflight.jems.api.programme.dto.language.SystemLanguage
import io.cloudflight.jems.server.common.entity.TranslationId
import io.cloudflight.jems.server.project.entity.report.ProjectPartnerReportEntity
import io.cloudflight.jems.server.project.entity.report.identification.ProjectPartnerReportIdentificationEntity
import io.cloudflight.jems.server.project.entity.report.identification.ProjectPartnerReportIdentificationTargetGroupEntity
import io.cloudflight.jems.server.project.entity.report.identification.ProjectPartnerReportIdentificationTargetGroupTranslEntity
import io.cloudflight.jems.server.project.entity.report.identification.ProjectPartnerReportIdentificationTranslEntity
import io.cloudflight.jems.server.project.repository.report.ProjectPartnerReportRepository
import io.cloudflight.jems.server.project.service.report.model.identification.ProjectPartnerReportIdentification
import io.cloudflight.jems.server.project.service.report.model.identification.UpdateProjectPartnerReportIdentification
import io.cloudflight.jems.server.project.service.report.partner.identification.ProjectReportIdentificationPersistence
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import kotlin.collections.HashSet

@Repository
class ProjectReportIdentificationPersistenceProvider(
    private val reportRepository: ProjectPartnerReportRepository,
    private val identificationRepository: ProjectPartnerReportIdentificationRepository,
    private val identificationTargetGroupRepository: ProjectPartnerReportIdentificationTargetGroupRepository,
) : ProjectReportIdentificationPersistence {

    @Transactional(readOnly = true)
    override fun getPartnerReportIdentification(partnerId: Long, reportId: Long): Optional<ProjectPartnerReportIdentification> =
        identificationRepository.findByReportEntityIdAndReportEntityPartnerId(
            reportId = reportId,
            partnerId = partnerId,
        ).map { it.toModel(
            targetGroups = identificationTargetGroupRepository.findAllByReportIdentificationEntityOrderBySortNumber(
                reportIdentificationEntity = it,
            ),
        ) }

    @Transactional
    override fun updatePartnerReportIdentification(
        partnerId: Long,
        reportId: Long,
        data: UpdateProjectPartnerReportIdentification,
    ): ProjectPartnerReportIdentification {
        val entity = identificationRepository.findByReportEntityIdAndReportEntityPartnerId(
            reportId = reportId,
            partnerId = partnerId,
        ).orElse(null) ?: createIdentificationEntityFrom(
            entity = reportRepository.findByIdAndPartnerId(id = reportId, partnerId = partnerId)
        )

        val targetGroupsExisting = identificationTargetGroupRepository
            .findAllByReportIdentificationEntityOrderBySortNumber(entity)

        updateBaseData(entity = entity, data)
        updateTranslations(entity = entity, data)
        updateTargetGroups(targetGroupsExisting, data)

        return entity.toModel(targetGroups = targetGroupsExisting)
    }

    private fun updateBaseData(
        entity: ProjectPartnerReportIdentificationEntity,
        data: UpdateProjectPartnerReportIdentification
    ) {
        entity.startDate = data.startDate
        entity.endDate = data.endDate
        entity.periodNumber = data.period
    }

    private fun updateTranslations(
        entity: ProjectPartnerReportIdentificationEntity,
        data: UpdateProjectPartnerReportIdentification
    ) {
        val summaryAsMap = data.getSummaryAsMap()
        val problemsAsMap = data.getProblemsAndDeviationsAsMap()

        entity.addMissingLanguagesIfNeeded(languages = summaryAsMap.keys union problemsAsMap.keys)
        entity.translatedValues.forEach {
            it.summary = summaryAsMap[it.language()]
            it.problemsAndDeviations = problemsAsMap[it.language()]
        }
    }

    private fun updateTargetGroups(
        targetGroupsExisting: List<ProjectPartnerReportIdentificationTargetGroupEntity>,
        data: UpdateProjectPartnerReportIdentification
    ) {
        targetGroupsExisting.forEachIndexed { index, targetGroupEntity ->
            val translationsByLanguage = data.targetGroups
                .getOrElse(index) { emptySet() }
                .associateBy({ it.language }, { it.translation })

            targetGroupEntity.addMissingLanguagesIfNeeded(languages = translationsByLanguage.keys)
            targetGroupEntity.translatedValues.forEach {
                it.description = translationsByLanguage[it.language()]
            }
        }
    }

    private fun ProjectPartnerReportIdentificationEntity.addMissingLanguagesIfNeeded(languages: Set<SystemLanguage>) {
        val existingLanguages = translatedValues.mapTo(HashSet()) { it.translationId.language }
        languages.filter { !existingLanguages.contains(it) }.forEach { language ->
            translatedValues.add(
                ProjectPartnerReportIdentificationTranslEntity(
                    translationId = TranslationId(this, language = language),
                    summary = null,
                    problemsAndDeviations = null,
                )
            )
        }
    }

    private fun ProjectPartnerReportIdentificationTargetGroupEntity.addMissingLanguagesIfNeeded(languages: Set<SystemLanguage>) {
        val existingLanguages = translatedValues.mapTo(HashSet()) { it.translationId.language }
        languages.filter { !existingLanguages.contains(it) }.forEach { language ->
            translatedValues.add(
                ProjectPartnerReportIdentificationTargetGroupTranslEntity(
                    translationId = TranslationId(this, language = language),
                    specification = null,
                    description = null,
                )
            )
        }
    }

    private fun createIdentificationEntityFrom(entity: ProjectPartnerReportEntity) =
        identificationRepository.save(
            ProjectPartnerReportIdentificationEntity(
                reportEntity = entity,
                startDate = null,
                endDate = null,
                periodNumber = null,
                translatedValues = mutableSetOf(),
            )
        )
}
