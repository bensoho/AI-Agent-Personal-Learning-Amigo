package com.agentlearning.feature.knowledge.plan

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Loads the bundled learning-plan markdown (assets/docs/study_plan.md) once and caches the parsed result. */
@Singleton
class StudyPlanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mutex = Mutex()
    private var cached: StudyPlan? = null

    suspend fun load(): StudyPlan = mutex.withLock {
        cached ?: withContext(Dispatchers.IO) {
            val text = context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
            StudyPlanParser.parse(text)
        }.also { cached = it }
    }

    companion object {
        const val ASSET_PATH = "docs/study_plan.md"
    }
}
