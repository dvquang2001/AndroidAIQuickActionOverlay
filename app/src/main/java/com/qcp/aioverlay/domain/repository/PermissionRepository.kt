package com.qcp.aioverlay.domain.repository

/**
 * Domain-layer contract for querying system permission state.
 * No Android framework types — implementation lives in the data layer.
 */
interface PermissionRepository {
    /** Returns true if the AI Accessibility Service is currently enabled. */
    fun isAccessibilityServiceEnabled(): Boolean

    /** Returns true if the app has SYSTEM_ALERT_WINDOW (draw-over-other-apps) permission. */
    fun hasOverlayPermission(): Boolean
}
