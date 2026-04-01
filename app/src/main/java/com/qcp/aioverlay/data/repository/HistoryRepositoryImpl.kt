package com.qcp.aioverlay.data.repository

import com.qcp.aioverlay.data.local.dao.HistoryDao
import com.qcp.aioverlay.data.local.entity.HistoryEntity
import com.qcp.aioverlay.data.mapper.toDomain
import com.qcp.aioverlay.domain.model.ActionType
import com.qcp.aioverlay.domain.model.HistoryItem
import com.qcp.aioverlay.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
): HistoryRepository {
    override fun observeHistory(): Flow<List<HistoryItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<HistoryItem>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun save(input: String, output: String, type: ActionType): Long =
        dao.insert(HistoryEntity(inputText = input, outputText = output, actionType = type))

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun clearAll() = dao.clearAll()
}