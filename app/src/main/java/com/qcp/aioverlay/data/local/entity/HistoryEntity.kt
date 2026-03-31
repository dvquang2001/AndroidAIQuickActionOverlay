package com.qcp.aioverlay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qcp.aioverlay.domain.model.ActionType

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inputText: String,
    val outputText: String,
    val actionType: ActionType,
    val createdAt: Long = System.currentTimeMillis()
)
