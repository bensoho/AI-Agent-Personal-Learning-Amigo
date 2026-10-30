package com.agentlearning.di

import android.content.Context
import androidx.room.Room
import com.agentlearning.data.local.AppDatabase
import com.agentlearning.data.repository.BackupRepositoryImpl
import com.agentlearning.data.repository.CurriculumRepositoryImpl
import com.agentlearning.data.repository.SettingsRepositoryImpl
import com.agentlearning.data.repository.StatsRepositoryImpl
import com.agentlearning.data.repository.StudyRepositoryImpl
import com.agentlearning.domain.repository.BackupRepository
import com.agentlearning.domain.repository.CurriculumRepository
import com.agentlearning.domain.repository.SettingsRepository
import com.agentlearning.domain.repository.StatsRepository
import com.agentlearning.domain.repository.StudyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.ZoneId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideCourseDao(db: AppDatabase) = db.courseDao()
    @Provides fun provideTopicDao(db: AppDatabase) = db.topicDao()
    @Provides fun provideQuestionDao(db: AppDatabase) = db.questionDao()
    @Provides fun provideAnswerRecordDao(db: AppDatabase) = db.answerRecordDao()
    @Provides fun provideReviewRecordDao(db: AppDatabase) = db.reviewRecordDao()
    @Provides fun provideStudySessionDao(db: AppDatabase) = db.studySessionDao()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun provideZone(clock: Clock): ZoneId = clock.zone
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun curriculum(impl: CurriculumRepositoryImpl): CurriculumRepository
    @Binds abstract fun study(impl: StudyRepositoryImpl): StudyRepository
    @Binds abstract fun stats(impl: StatsRepositoryImpl): StatsRepository
    @Binds abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds abstract fun backup(impl: BackupRepositoryImpl): BackupRepository
}
