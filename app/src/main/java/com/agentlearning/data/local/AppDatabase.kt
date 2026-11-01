package com.agentlearning.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.agentlearning.data.local.dao.AnswerRecordDao
import com.agentlearning.data.local.dao.CourseDao
import com.agentlearning.data.local.dao.QuestionDao
import com.agentlearning.data.local.dao.ReadCountDao
import com.agentlearning.data.local.dao.ReviewRecordDao
import com.agentlearning.data.local.dao.StudySessionDao
import com.agentlearning.data.local.dao.TopicDao
import com.agentlearning.data.local.entity.AnswerRecordEntity
import com.agentlearning.data.local.entity.CourseEntity
import com.agentlearning.data.local.entity.QuestionEntity
import com.agentlearning.data.local.entity.ReadCountEntity
import com.agentlearning.data.local.entity.ReviewRecordEntity
import com.agentlearning.data.local.entity.StudySessionEntity
import com.agentlearning.data.local.entity.TopicEntity

@Database(
    entities = [
        CourseEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        AnswerRecordEntity::class,
        ReviewRecordEntity::class,
        StudySessionEntity::class,
        ReadCountEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun answerRecordDao(): AnswerRecordDao
    abstract fun reviewRecordDao(): ReviewRecordDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun readCountDao(): ReadCountDao

    companion object {
        const val NAME = "agent_learning.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE topics ADD COLUMN notes TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `read_counts` (`contentKey` TEXT NOT NULL, " +
                        "`count` INTEGER NOT NULL, `lastReadAt` INTEGER NOT NULL, PRIMARY KEY(`contentKey`))",
                )
            }
        }
    }
}
