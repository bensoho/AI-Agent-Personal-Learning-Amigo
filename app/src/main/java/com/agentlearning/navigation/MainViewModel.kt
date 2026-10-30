package com.agentlearning.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentlearning.data.importer.QuestionBankImporter
import com.agentlearning.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InitState {
    data object Loading : InitState
    data object Ready : InitState
    data class Error(val message: String) : InitState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val importer: QuestionBankImporter,
    settings: SettingsRepository,
) : ViewModel() {

    val fontScale: StateFlow<Float> = settings.fontScale
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1f)

    private val _state = MutableStateFlow<InitState>(InitState.Loading)
    val state: StateFlow<InitState> = _state.asStateFlow()

    init {
        initialize()
    }

    fun initialize() {
        _state.value = InitState.Loading
        viewModelScope.launch {
            _state.value = try {
                importer.importIfNeeded()
                InitState.Ready
            } catch (e: Exception) {
                InitState.Error(e.message ?: "题库导入失败")
            }
        }
    }
}
