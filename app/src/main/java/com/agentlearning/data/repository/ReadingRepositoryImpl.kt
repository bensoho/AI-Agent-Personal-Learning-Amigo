package com.agentlearning.data.repository

import com.agentlearning.data.local.dao.ReadCountDao
import com.agentlearning.domain.repository.ReadingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepositoryImpl @Inject constructor(
    private val dao: ReadCountDao,
    private val clock: Clock,
) : ReadingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun observeReadCounts(): Flow<Map<String, Int>> =
        dao.observeAll().map { rows -> rows.associate { it.contentKey to it.count } }

    override fun recordRead(contentKey: String) {
        val readAt = clock.millis()
        scope.launch { dao.recordRead(contentKey, readAt) }
    }
}
