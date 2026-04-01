package com.qcp.aioverlay.data.mapper

import com.qcp.aioverlay.data.local.entity.HistoryEntity
import com.qcp.aioverlay.domain.model.HistoryItem

fun HistoryEntity.toDomain(): HistoryItem = HistoryItem(
    id = id,
    inputText = inputText,
    outputText = outputText,
    actionType = actionType,
    createdAt = createdAt
)

fun HistoryItem.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    inputText = inputText,
    outputText = outputText,
    actionType = actionType,
    createdAt = createdAt
)
