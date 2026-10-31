package com.agentlearning.feature.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.repository.CurriculumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    repository: CurriculumRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Debounced so a keystroke does not rescan every topic's notes on the main thread.
    @OptIn(FlowPreview::class)
    val filterQuery: StateFlow<String> = _query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce { if (it.isEmpty()) 0L else 200L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val courses: StateFlow<List<CourseOutline>> =
        combine(repository.observeOutline(), filterQuery) { outline, query ->
            filter(outline, query)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
