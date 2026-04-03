package com.qcp.aioverlay.ui.ext

import androidx.annotation.StringRes
import com.qcp.aioverlay.R
import com.qcp.aioverlay.domain.model.ActionType

/**
 * Maps a domain [ActionType] to its localised string resource.
 * Kept in the UI layer so the domain stays framework-free.
 */
val ActionType.labelRes: Int
    @StringRes get() = when (this) {
        ActionType.TRANSLATE -> R.string.action_translate
        ActionType.SUMMARIZE -> R.string.action_summarize
        ActionType.EXPLAIN   -> R.string.action_explain
        ActionType.CUSTOM    -> R.string.action_custom
    }
