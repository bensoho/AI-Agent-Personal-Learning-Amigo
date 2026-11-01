package com.agentlearning.feature.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.repository.CurriculumRepository
import com.agentlearning.domain.repository.ReadingRepository
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
    readingRepository: ReadingRepository,
) : ViewModel() {

    val readCounts: StateFlow<Map<String, Int>> = readingRepository.observeReadCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _scope = MutableStateFlow(KnowledgeScope())
    val scope: StateFlow<KnowledgeScope> = _scope.asStateFlow()
    val outline: StateFlow<List<CourseOutline>> = repository.observeOutline()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Debounced so a keystroke does not rescan every topic's notes on the main thread.
    @OptIn(FlowPreview::class)
    val filterQuery: StateFlow<String> = _query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce { if (it.isEmpty()) 0L else 200L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val courses: StateFlow<List<CourseOutline>> =
        combine(outline, filterQuery, scope) { outline, query, scope ->
            filterKnowledge(outline, query, scope)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun selectMonth(monthNumber: Int?) {
        _scope.value = KnowledgeScope(monthNumber = monthNumber)
    }

    fun selectWeek(weekNumber: Int?) {
        _scope.value = _scope.value.copy(weekNumber = weekNumber)
    }

    fun resetFilters() {
        _query.value = ""
        _scope.value = KnowledgeScope()
    }
}
