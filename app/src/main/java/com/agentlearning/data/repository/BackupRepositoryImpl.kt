package com.agentlearning.data.repository

import androidx.room.withTransaction
import com.agentlearning.data.local.AppDatabase
import com.agentlearning.data.local.entity.AnswerRecordEntity
import com.agentlearning.data.local.entity.ReviewRecordEntity
import com.agentlearning.data.local.entity.StudySessionEntity
import com.agentlearning.domain.model.BackupPreview
import com.agentlearning.domain.repository.BackupException
import com.agentlearning.domain.repository.BackupRepository
import com.agentlearning.domain.repository.ParsedBackup
import com.agentlearning.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupFile(
    val format: String = FORMAT,
    val schemaVersion: Int = SCHEMA_VERSION,
    val bankContentVersion: Int,
    val exportedAt: Long,
    val sessions: List<StudySessionEntity>,
    val answers: List<AnswerRecordEntity>,
    val reviews: List<ReviewRecordEntity>,
) {
    companion object {
        const val FORMAT = "agent-learning-backup"
        const val SCHEMA_VERSION = 1
    }
}

private class ParsedBackupImpl(val file: BackupFile) : ParsedBackup {
    override val preview = BackupPreview(
        exportedAt = file.exportedAt,
        bankContentVersion = file.bankContentVersion,
        sessionCount = file.sessions.size,
        answerCount = file.answers.size,
        reviewCount = file.reviews.size,
    )
}

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val settings: SettingsRepository,
    private val json: Json,
    private val clock: Clock,
) : BackupRepository {

    override suspend fun exportJson(): String = database.withTransaction {
        val file = BackupFile(
            bankContentVersion = settings.bankContentVersion.first(),
            exportedAt = clock.millis(),
            sessions = database.studySessionDao().getAll(),
            answers = database.answerRecordDao().getAll(),
            reviews = database.reviewRecordDao().getAll(),
        )
        json.encodeToString(BackupFile.serializer(), file)
    }

    override suspend fun parse(json: String): ParsedBackup {
        val file = try {
            this.json.decodeFromString(BackupFile.serializer(), json)
        } catch (e: SerializationException) {
            throw BackupException("备份文件格式无效")
        } catch (e: IllegalArgumentException) {
            throw BackupException("备份文件格式无效")
        }
        if (file.format != BackupFile.FORMAT) throw BackupException("不是 Agent Learning 备份文件")
        if (file.schemaVersion != BackupFile.SCHEMA_VERSION) {
            throw BackupException("不支持的备份版本: ${file.schemaVersion}")
        }

        val knownQuestions = database.questionDao().getAllIds().toSet()
        val referenced = file.answers.map { it.questionId } + file.reviews.map { it.questionId }
        val missing = referenced.filter { it !in knownQuestions }.toSet()
        if (missing.isNotEmpty()) throw BackupException("备份包含当前题库中不存在的题目: ${missing.size} 道")

        val sessionIds = file.sessions.map { it.id }.toSet()
        if (file.answers.any { it.sessionId != null && it.sessionId !in sessionIds }) {
            throw BackupException("备份中的答题记录引用了不存在的学习会话")
        }
        return ParsedBackupImpl(file)
    }

    override suspend fun restore(backup: ParsedBackup) {
        val file = (backup as ParsedBackupImpl).file
        database.withTransaction {
            clearUserData()
            database.studySessionDao().insertAll(file.sessions)
            database.answerRecordDao().insertAll(file.answers)
            database.reviewRecordDao().insertAll(file.reviews)
        }
    }

    override suspend fun resetUserData() {
        database.withTransaction { clearUserData() }
    }

    private suspend fun clearUserData() {
        database.answerRecordDao().deleteAll()
        database.reviewRecordDao().deleteAll()
        database.studySessionDao().deleteAll()
    }
}
