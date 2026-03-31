package com.qcp.aioverlay.domain.repository

import com.qcp.aioverlay.domain.model.ActionType
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    fun observeHistory()

    fun save(
        input: String,
        output: String,
        type: ActionType
    )
}