package com.agentlearning.data.repository

import com.agentlearning.data.local.dao.CourseDao
import com.agentlearning.data.local.dao.QuestionDao
import com.agentlearning.data.local.dao.TopicDao
import com.agentlearning.data.local.dao.TopicStatRow
import com.agentlearning.data.mapper.QuestionMapper
import com.agentlearning.data.mapper.toDomain
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.Question
import com.agentlearning.domain.model.Topic
import com.agentlearning.domain.model.TopicProgress
import com.agentlearning.domain.model.WeekOutline
import com.agentlearning.domain.repository.CurriculumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurriculumRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao,
    private val topicDao: TopicDao,
    private val questionDao: QuestionDao,
    private val mapper: QuestionMapper,
) : CurriculumRepository {

    override fun observeOutline(): Flow<List<CourseOutline>> =
        combine(
            courseDao.observeAll(),
            topicDao.observeAll(),
            questionDao.observeTopicStats(),
        ) { courses, topics, stats ->
            val statsByTopic = stats.associateBy { it.topicId }
            courses.map { course ->
                val weeks = topics.filter { it.courseId == course.id }
                    .groupBy { it.weekNumber }
                    .map { (weekNumber, weekTopics) ->
                        WeekOutline(
                            weekNumber = weekNumber,
                            title = weekTopics.first().weekTitle,
                            topics = weekTopics.map { it.toDomain().withStats(statsByTopic[it.id]) },
                        )
                    }
                    .sortedBy { it.weekNumber }
                CourseOutline(course.toDomain(), weeks)
            }
        }

    override fun observeTopic(topicId: String): Flow<Topic?> =
        topicDao.observeById(topicId).map { it?.toDomain() }

    override fun observeQuestions(topicId: String): Flow<List<Question>> =
        questionDao.observeByTopic(topicId).map { list -> list.map(mapper::toDomain) }

    private fun Topic.withStats(row: TopicStatRow?) = TopicProgress(
        topic = this,
        total = row?.total ?: 0,
        answered = row?.answered ?: 0,
        mastered = row?.mastered ?: 0,
        reviewing = row?.reviewing ?: 0,
        learning = row?.learning ?: 0,
    )
}
