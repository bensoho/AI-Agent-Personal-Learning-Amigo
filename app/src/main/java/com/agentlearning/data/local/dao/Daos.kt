package com.agentlearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.agentlearning.data.local.entity.AnswerRecordEntity
import com.agentlearning.data.local.entity.CourseEntity
import com.agentlearning.data.local.entity.QuestionEntity
import com.agentlearning.data.local.entity.ReadCountEntity
import com.agentlearning.data.local.entity.ReviewRecordEntity
import com.agentlearning.data.local.entity.StudySessionEntity
import com.agentlearning.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

data class TopicStatRow(
    val topicId: String,
    val total: Int,
    val answered: Int,
    val mastered: Int,
    val reviewing: Int,
    val learning: Int,
)

data class DailyStatRow(val day: String, val total: Int, val correct: Int)

data class WeeklyStatRow(val week: String, val total: Int, val correct: Int)

@Dao
interface CourseDao {
    @Upsert
    suspend fun upsertAll(items: List<CourseEntity>)

    @Query("SELECT * FROM courses ORDER BY sortOrder")
    fun observeAll(): Flow<List<CourseEntity>>
}

@Dao
interface TopicDao {
    @Upsert
    suspend fun upsertAll(items: List<TopicEntity>)

    @Query("SELECT * FROM topics ORDER BY weekNumber, sortOrder")
    fun observeAll(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :topicId LIMIT 1")
    fun observeById(topicId: String): Flow<TopicEntity?>
}

@Dao
interface QuestionDao {
    @Upsert
    suspend fun upsertAll(items: List<QuestionEntity>)

    @Query("UPDATE questions SET isActive = 0")
    suspend fun deactivateAll()

    @Query("SELECT COUNT(*) FROM questions WHERE isActive = 1")
    suspend fun count(): Int

    @Query("SELECT id FROM questions")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE topicId = :topicId AND isActive = 1 ORDER BY id")
    fun observeByTopic(topicId: String): Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN review_records r ON r.questionId = q.id
        WHERE q.isActive = 1 AND r.nextReviewAt <= :now
        ORDER BY r.nextReviewAt, q.id
        LIMIT :limit
        """
    )
    fun observeDue(now: Long, limit: Int): Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM questions q
        INNER JOIN review_records r ON r.questionId = q.id
        WHERE q.isActive = 1 AND r.nextReviewAt <= :now
        """
    )
    fun observeDueCount(now: Long): Flow<Int>

    // Questions whose most recent answer was wrong.
    @Query(
        """
        SELECT q.* FROM questions q
        WHERE q.isActive = 1 AND (
            SELECT a.isCorrect FROM answer_records a
            WHERE a.questionId = q.id
            ORDER BY a.answeredAt DESC, a.id DESC LIMIT 1
        ) = 0
        ORDER BY q.id
        LIMIT :limit
        """
    )
    fun observeRecentWrong(limit: Int): Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM questions q
        WHERE q.isActive = 1 AND (
            SELECT a.isCorrect FROM answer_records a
            WHERE a.questionId = q.id
            ORDER BY a.answeredAt DESC, a.id DESC LIMIT 1
        ) = 0
        """
    )
    fun observeRecentWrongCount(): Flow<Int>

    @Query(
        """
        SELECT q.* FROM questions q
        WHERE q.isActive = 1
          AND (SELECT COUNT(*) FROM answer_records a WHERE a.questionId = q.id AND a.isCorrect = 0) >= 2
        ORDER BY q.id
        LIMIT :limit
        """
    )
    fun observeRepeatedWrong(limit: Int): Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN review_records r ON r.questionId = q.id
        WHERE q.isActive = 1 AND r.status = 'MASTERED'
        ORDER BY q.id
        LIMIT :limit
        """
    )
    fun observeMastered(limit: Int): Flow<List<QuestionEntity>>

    // Never-answered questions, earliest week first.
    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN topics t ON t.id = q.topicId
        WHERE q.isActive = 1
          AND NOT EXISTS (SELECT 1 FROM answer_records a WHERE a.questionId = q.id)
        ORDER BY t.weekNumber, t.sortOrder, q.id
        LIMIT :limit
        """
    )
    fun observeUnanswered(limit: Int): Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT MIN(t.weekNumber) FROM questions q
        INNER JOIN topics t ON t.id = q.topicId
        WHERE q.isActive = 1
          AND NOT EXISTS (SELECT 1 FROM answer_records a WHERE a.questionId = q.id)
        """
    )
    fun observeCurrentWeek(): Flow<Int?>

    @Query(
        """
        SELECT q.topicId AS topicId,
            COUNT(*) AS total,
            SUM(CASE WHEN EXISTS (SELECT 1 FROM answer_records a WHERE a.questionId = q.id) THEN 1 ELSE 0 END) AS answered,
            SUM(CASE WHEN r.status = 'MASTERED' THEN 1 ELSE 0 END) AS mastered,
            SUM(CASE WHEN r.status = 'REVIEWING' THEN 1 ELSE 0 END) AS reviewing,
            SUM(CASE WHEN r.status = 'LEARNING' THEN 1 ELSE 0 END) AS learning
        FROM questions q
        LEFT JOIN review_records r ON r.questionId = q.id
        WHERE q.isActive = 1
        GROUP BY q.topicId
        """
    )
    fun observeTopicStats(): Flow<List<TopicStatRow>>
}

@Dao
interface AnswerRecordDao {
    @Insert
    suspend fun insert(record: AnswerRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<AnswerRecordEntity>)

    @Query("SELECT * FROM answer_records ORDER BY id")
    suspend fun getAll(): List<AnswerRecordEntity>

    @Query("DELETE FROM answer_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM answer_records")
    fun observeTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM answer_records WHERE isCorrect = 1")
    fun observeCorrect(): Flow<Int>

    @Query("SELECT COUNT(*) FROM answer_records WHERE answeredAt >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("SELECT DISTINCT date(answeredAt / 1000, 'unixepoch', 'localtime') FROM answer_records")
    fun observeStudyDays(): Flow<List<String>>

    @Query(
        """
        SELECT date(answeredAt / 1000, 'unixepoch', 'localtime') AS day,
            COUNT(*) AS total,
            SUM(isCorrect) AS correct
        FROM answer_records
        WHERE answeredAt >= :since
        GROUP BY day
        ORDER BY day
        """
    )
    fun observeDailyStats(since: Long): Flow<List<DailyStatRow>>

    @Query(
        """
        SELECT strftime('%Y-W%W', answeredAt / 1000, 'unixepoch', 'localtime') AS week,
            COUNT(*) AS total,
            SUM(isCorrect) AS correct
        FROM answer_records
        GROUP BY week
        ORDER BY week DESC
        LIMIT :limit
        """
    )
    fun observeWeeklyStats(limit: Int): Flow<List<WeeklyStatRow>>
}

@Dao
interface ReviewRecordDao {
    @Query("SELECT * FROM review_records WHERE questionId = :questionId LIMIT 1")
    suspend fun get(questionId: String): ReviewRecordEntity?

    @Upsert
    suspend fun upsert(record: ReviewRecordEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<ReviewRecordEntity>)

    @Query("SELECT * FROM review_records ORDER BY questionId")
    suspend fun getAll(): List<ReviewRecordEntity>

    @Query("DELETE FROM review_records")
    suspend fun deleteAll()
}

@Dao
interface ReadCountDao {
    @Query("SELECT * FROM read_counts")
    fun observeAll(): Flow<List<ReadCountEntity>>

    @Query("UPDATE read_counts SET count = count + 1, lastReadAt = :readAt WHERE contentKey = :contentKey")
    suspend fun increment(contentKey: String, readAt: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: ReadCountEntity)

    @Transaction
    suspend fun recordRead(contentKey: String, readAt: Long) {
        if (increment(contentKey, readAt) == 0) insert(ReadCountEntity(contentKey, count = 1, lastReadAt = readAt))
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<ReadCountEntity>)

    @Query("SELECT * FROM read_counts ORDER BY contentKey")
    suspend fun getAll(): List<ReadCountEntity>

    @Query("DELETE FROM read_counts")
    suspend fun deleteAll()
}

@Dao
interface StudySessionDao {
    @Insert
    suspend fun insert(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<StudySessionEntity>)

    @Query("SELECT * FROM study_sessions ORDER BY id")
    suspend fun getAll(): List<StudySessionEntity>

    @Query(
        """
        UPDATE study_sessions
        SET answeredCount = answeredCount + 1, correctCount = correctCount + :correct
        WHERE id = :sessionId
        """
    )
    suspend fun addAnswer(sessionId: Long, correct: Int)

    @Query("UPDATE study_sessions SET endedAt = :endedAt WHERE id = :sessionId")
    suspend fun finish(sessionId: Long, endedAt: Long)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}
