package com.agentlearning.data.importer

import androidx.room.withTransaction
import com.agentlearning.data.local.AppDatabase
import com.agentlearning.data.mapper.QuestionMapper
import com.agentlearning.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Imports the bundled question bank on first launch and whenever its contentVersion increases. */
@Singleton
class QuestionBankImporter @Inject constructor(
    private val database: AppDatabase,
    private val reader: QuestionAssetReader,
    private val validator: QuestionBankValidator,
    private val mapper: QuestionMapper,
    private val settings: SettingsRepository,
) {
    suspend fun importIfNeeded() = withContext(Dispatchers.IO) {
        val manifest = reader.loadManifest()
        val installed = settings.bankContentVersion.first()
        val hasQuestions = database.questionDao().count() > 0
        if (hasQuestions && installed >= manifest.contentVersion) return@withContext

        val bank = reader.loadAll(manifest)
        val errors = validator.validate(bank)
        if (errors.isNotEmpty()) throw QuestionBankException(errors)

        val entities = mapper.toEntities(bank)
        // Upserts keep answer history intact; questions missing from the new bank stay inactive.
        database.withTransaction {
            database.questionDao().deactivateAll()
            database.courseDao().upsertAll(entities.courses)
            database.topicDao().upsertAll(entities.topics)
            database.questionDao().upsertAll(entities.questions)
        }
        settings.setBankContentVersion(manifest.contentVersion)
    }
}
