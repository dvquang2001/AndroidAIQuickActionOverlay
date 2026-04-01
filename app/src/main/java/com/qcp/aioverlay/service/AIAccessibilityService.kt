package com.qcp.aioverlay.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.qcp.aioverlay.overlay.OverlayManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class AIAccessibilityService: AccessibilityService() {

    @Inject lateinit var overlayManager: OverlayManager

    // ===== Lifecycle =====
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "onServiceConnected")
//        serviceInfo = AccessibilityServiceInfo().apply {
//            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or
//                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
//            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
//            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
//                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
//            notificationTimeout = 100
//        }
        overlayManager.attachService(this)
    }

    override fun onDestroy() {
        overlayManager.detachService()
        super.onDestroy()
    }


    // ===== Event handling =====
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "eventType: ${AccessibilityEvent.eventTypeToString(event?.eventType ?: 0)}, text: ${event?.text}")
        when(event?.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> handleTextSelection(event)
            else -> Unit
        }
    }

    override fun onInterrupt() {
        overlayManager.hideAll()
    }

    // ===== Private funciton =====
    private fun handleTextSelection(event: AccessibilityEvent) {
        val source = event.source ?: return
        val selectedText = extractSelectedText(source)
        Log.d(TAG, "Selected text: $selectedText")

        if(selectedText.isNullOrBlank() || selectedText.length < 3) {
            overlayManager.hideFloatingButton()
            return
        }

        overlayManager.showFloatingButton(selectedText)
    }

    private fun extractSelectedText(node: AccessibilityNodeInfo): String? {
        val start = node.textSelectionStart
        val end = node.textSelectionEnd
        if(start !in 0..<end) return null

        return try {
            node.text?.subSequence(start, end)?.toString()
        } catch (e: Exception) {
            null
        }
    }

    // ===== Companion =====
    companion object {
        var instance: AIAccessibilityService? = null
            private set
        private const val TAG = "AIAccessibilityService"
    }
}