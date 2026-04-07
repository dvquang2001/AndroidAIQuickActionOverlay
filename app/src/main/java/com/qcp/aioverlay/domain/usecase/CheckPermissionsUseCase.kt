package com.qcp.aioverlay.domain.usecase

import com.qcp.aioverlay.domain.repository.PermissionRepository
import javax.inject.Inject

/**
 * Returns a point-in-time snapshot of both required permission states.
 *
 * Intentionally non-suspend and non-Flow: permissions are checked on demand
 * (e.g. on screen resume) rather than observed continuously, which avoids
 * setting up a polling loop for a value that only changes when the user
 * explicitly goes to system settings.
 */
class CheckPermissionsUseCase @Inject constructor(
    private val repository: PermissionRepository
) {

    data class Result(
        val isAccessibilityEnabled: Boolean,
        val hasOverlayPermission: Boolean
    )

    operator fun invoke(): Result = Result(
        isAccessibilityEnabled = repository.isAccessibilityServiceEnabled(),
        hasOverlayPermission = repository.hasOverlayPermission()
    )
}
