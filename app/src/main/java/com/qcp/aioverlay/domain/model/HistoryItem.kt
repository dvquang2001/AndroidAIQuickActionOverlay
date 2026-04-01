package com.qcp.aioverlay.domain.model

data class HistoryItem(
    val id: Long = 0,
    val inputText: String,
    val outputText: String,
    val actionType: ActionType,
    val createdAt: Long = System.currentTimeMillis()
)
