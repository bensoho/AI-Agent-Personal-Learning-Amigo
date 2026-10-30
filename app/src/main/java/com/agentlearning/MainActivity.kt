package com.agentlearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agentlearning.navigation.AppRoot
import com.agentlearning.navigation.MainViewModel
import com.agentlearning.ui.theme.AgentLearningTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fontScale by mainViewModel.fontScale.collectAsStateWithLifecycle()
            AgentLearningTheme(fontScale = fontScale) {
                AppRoot(mainViewModel)
            }
        }
    }
}
