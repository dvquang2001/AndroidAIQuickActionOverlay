package com.qcp.aioverlay.data.repository

import com.qcp.aioverlay.data.local.dao.HistoryDao
import com.qcp.aioverlay.data.local.entity.HistoryEntity
import com.qcp.aioverlay.domain.model.ActionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
) {
    fun observeHistory(): Flow<List<HistoryEntity>> = dao.observeAll()

    fun observeRecent(limit: Int = 20): Flow<List<HistoryEntity>> = dao.observeRecent(limit)

    suspend fun save(input: String, output: String, type: ActionType): Long =
        dao.insert(HistoryEntity(inputText = input, outputText = output, actionType = type))

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}