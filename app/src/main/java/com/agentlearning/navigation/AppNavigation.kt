package com.agentlearning.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agentlearning.domain.model.StudyMode
import com.agentlearning.feature.curriculum.CurriculumScreen
import com.agentlearning.feature.curriculum.TopicDetailScreen
import com.agentlearning.feature.home.HomeScreen
import com.agentlearning.feature.knowledge.KnowledgeDetailScreen
import com.agentlearning.feature.knowledge.KnowledgeScreen
import com.agentlearning.feature.knowledge.plan.StudyPlanDetailScreen
import com.agentlearning.feature.quiz.QuizScreen
import com.agentlearning.feature.review.ReviewScreen
import com.agentlearning.feature.settings.SettingsScreen
import com.agentlearning.feature.statistics.StatisticsScreen

object Routes {
    const val HOME = "home"
    const val CURRICULUM = "curriculum"
    const val KNOWLEDGE = "knowledge"
    const val REVIEW = "review"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val TOPIC = "topic/{topicId}"
    const val KNOWLEDGE_DETAIL = "knowledge/{topicId}"
    const val PLAN_DETAIL = "plan/{documentId}"
    const val QUIZ = "quiz/{mode}?topicId={topicId}"

    fun topic(topicId: String) = "topic/$topicId"

    fun knowledgeDetail(topicId: String) = "knowledge/$topicId"

    fun planDetail(documentId: String) = "plan/$documentId"

    fun quiz(mode: StudyMode, topicId: String? = null) =
        "quiz/${mode.name}" + (topicId?.let { "?topicId=$it" } ?: "")
}

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, "首页", Icons.Filled.Home),
    TopLevelDestination(Routes.CURRICULUM, "课程", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(Routes.KNOWLEDGE, "知识点", Icons.Filled.Info),
    TopLevelDestination(Routes.REVIEW, "复习", Icons.Filled.Refresh),
    TopLevelDestination(Routes.STATS, "统计", Icons.Filled.DateRange),
)

@Composable
fun AppRoot(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        InitState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is InitState.Error -> Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("题库加载失败")
            Text(s.message)
            Button(onClick = viewModel::initialize) { Text("重试") }
        }
        InitState.Ready -> AppScaffold()
    }
}

@Composable
private fun AppScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomBar = topLevelDestinations.any { top -> destination?.hierarchy?.any { it.route == top.route } == true }

    Scaffold(
        contentWindowInsets = if (showBottomBar) ScaffoldDefaults.contentWindowInsets else WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { top ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.route == top.route } == true,
                            onClick = {
                                navController.navigate(top.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(top.icon, contentDescription = top.label) },
                            label = { Text(top.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStartQuiz = { navController.navigate(Routes.quiz(it)) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.CURRICULUM) {
                CurriculumScreen(onTopicClick = { navController.navigate(Routes.topic(it)) })
            }
            composable(Routes.KNOWLEDGE) {
                KnowledgeScreen(
                    onTopicClick = { navController.navigate(Routes.knowledgeDetail(it)) },
                    onPlanDocumentClick = { navController.navigate(Routes.planDetail(it)) },
                )
            }
            composable(
                Routes.PLAN_DETAIL,
                arguments = listOf(navArgument("documentId") { type = NavType.StringType }),
            ) {
                StudyPlanDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REVIEW) {
                ReviewScreen(onStartQuiz = { navController.navigate(Routes.quiz(it)) })
            }
            composable(Routes.STATS) { StatisticsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
            composable(
                Routes.KNOWLEDGE_DETAIL,
                arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
            ) { entry ->
                val topicId = entry.arguments?.getString("topicId").orEmpty()
                KnowledgeDetailScreen(
                    onBack = { navController.popBackStack() },
                    onStartQuiz = { navController.navigate(Routes.quiz(StudyMode.TOPIC, topicId)) },
                )
            }
            composable(
                Routes.TOPIC,
                arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
            ) { entry ->
                val topicId = entry.arguments?.getString("topicId").orEmpty()
                TopicDetailScreen(
                    onBack = { navController.popBackStack() },
                    onStartQuiz = { navController.navigate(Routes.quiz(StudyMode.TOPIC, topicId)) },
                )
            }
            composable(
                Routes.QUIZ,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("topicId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                QuizScreen(onExit = { navController.popBackStack() })
            }
        }
    }
}
