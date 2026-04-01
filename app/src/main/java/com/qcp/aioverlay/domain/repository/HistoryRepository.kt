package com.qcp.aioverlay.domain.repository

import com.qcp.aioverlay.domain.model.ActionType
import com.qcp.aioverlay.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    fun observeHistory(): Flow<List<HistoryItem>>
    fun observeRecent(limit: Int = 20): Flow<List<HistoryItem>>
    suspend fun save(
        input: String,
        output: String,
        type: ActionType
    ): Long
    suspend fun delete(id: Long)
    suspend fun clearAll()
}