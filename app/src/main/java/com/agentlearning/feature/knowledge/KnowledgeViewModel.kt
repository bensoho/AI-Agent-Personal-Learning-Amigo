package com.agentlearning.feature.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.repository.CurriculumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    repository: CurriculumRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val courses: StateFlow<List<CourseOutline>> = combine(repository.observeOutline(), _query) { outline, query ->
        filter(outline, query.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    private fun filter(outline: List<CourseOutline>, query: String): List<CourseOutline> {
        if (query.isEmpty()) return outline
        return outline.mapNotNull { course ->
            val weeks = course.weeks.mapNotNull { week ->
                val topics = week.topics.filter { progress ->
                    val topic = progress.topic
                    listOfNotNull(topic.title, topic.description, topic.notes)
                        .any { it.contains(query, ignoreCase = true) }
                }
                if (topics.isEmpty()) null else week.copy(topics = topics)
            }
            if (weeks.isEmpty()) null else course.copy(weeks = weeks)
        }
    }
}
