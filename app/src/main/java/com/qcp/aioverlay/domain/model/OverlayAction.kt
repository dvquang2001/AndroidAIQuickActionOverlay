package com.qcp.aioverlay.domain.model

data class OverlayAction(
    val id: Long = 0,
    val inputText: String,
    val actionType: ActionType,
    val customPrompt: String? = null
)