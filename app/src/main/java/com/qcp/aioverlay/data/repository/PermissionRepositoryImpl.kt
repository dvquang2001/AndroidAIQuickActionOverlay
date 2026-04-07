package com.qcp.aioverlay.data.repository

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.qcp.aioverlay.domain.repository.PermissionRepository
import com.qcp.aioverlay.service.AIAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads real-time permission state from the Android system.
 *
 * Both checks are synchronous Binder calls that complete in microseconds —
 * safe to call on any thread including the main thread.
 */
@Singleton
class PermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionRepository {

    /**
     * Parses the colon-separated list stored in [Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES]
     * and checks whether [AIAccessibilityService] is present.
     *
     * This is the authoritative, non-cached check — every call reads the current value
     * from the Settings provider so it never returns stale state.
     */
    override fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val target = ComponentName(context, AIAccessibilityService::class.java)
            .flattenToString()

        // The field is a colon-delimited list, e.g.:
        //   "com.example/.ServiceA:com.qcp.aioverlay/.AIAccessibilityService"
        return enabledServices
            .split(":")
            .any { it.equals(target, ignoreCase = true) }
    }

    /**
     * Delegates to [Settings.canDrawOverlays] — the single source of truth for
     * SYSTEM_ALERT_WINDOW permission on API 23+.
     */
    override fun hasOverlayPermission(): Boolean = Settings.canDrawOverlays(context)
}
