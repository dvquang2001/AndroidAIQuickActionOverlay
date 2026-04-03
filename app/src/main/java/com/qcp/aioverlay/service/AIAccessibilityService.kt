package com.qcp.aioverlay.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.qcp.aioverlay.overlay.OverlayManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class AIAccessibilityService : AccessibilityService() {

    @Inject lateinit var overlayManager: OverlayManager

    private val handler = Handler(Looper.getMainLooper())
    private var lastLongClickedNode: AccessibilityNodeInfo? = null

    // ===== Lifecycle =====
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "onServiceConnected")
        overlayManager.attachService(this)
    }

    override fun onDestroy() {
        overlayManager.detachService()
        super.onDestroy()
    }

    // ===== Event handling =====
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {

            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                val source = event.source ?: return
                // Try standard selection first (works for EditText/TextView)
                val text = extractSelectedText(source)
                    ?: event.text.firstOrNull()?.toString()?.takeIf { it.length >= 3 } // WebView fallback
                if (!text.isNullOrBlank() && text.length >= 3) {
                    Log.d(TAG, "Selection changed: $text")
                    overlayManager.showFloatingButton(text)
                } else {
                    overlayManager.hideFloatingButton()
                }
            }

            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                Log.d(TAG, "Long click detected")
                lastLongClickedNode = event.source

                handler.postDelayed({
                    handleLongClick()
                }, 300)
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (lastLongClickedNode != null) {
                    val text = findSelectedTextInWindow()
                    if (!text.isNullOrBlank() && text.length >= 3) {
                        Log.d(TAG, "Window scan found: $text")
                        overlayManager.showFloatingButton(text)
                        lastLongClickedNode = null
                    }
                }
            }

            else -> Unit
        }
    }

    override fun onInterrupt() {
        overlayManager.hideAll()
    }

    // ===== Private =====

    private fun handleLongClick() {
        val windowText = findSelectedTextInWindow()
        if (!windowText.isNullOrBlank() && windowText.length >= 3) {
            Log.d(TAG, "Long click → window text: $windowText")
            overlayManager.showFloatingButton(windowText)
            lastLongClickedNode = null
            return
        }

        val nodeText = lastLongClickedNode?.text?.toString()
        if (!nodeText.isNullOrBlank() && nodeText.length >= 3) {
            Log.d(TAG, "Long click → node text: $nodeText")
            overlayManager.showFloatingButton(nodeText)
            lastLongClickedNode = null
            return
        }

        // Direct clipboard read is blocked by Android 10+ when not in focus.
        // Instead, perform ACTION_COPY via the accessibility API (permitted),
        // then read clipboard after the copy completes.
        val node = lastLongClickedNode
        if (node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_COPY)
            handler.postDelayed({
                val clipText = getClipboardText()
                if (!clipText.isNullOrBlank() && clipText.length >= 3) {
                    Log.d(TAG, "Long click → ACTION_COPY clipboard: $clipText")
                    overlayManager.showFloatingButton(clipText)
                }
                lastLongClickedNode = null
            }, 300)
        }
    }

    /**
     * Traverse toàn bộ view tree từ root, tìm node có text selection.
     */
    private fun findSelectedTextInWindow(): String? {
        val root = rootInActiveWindow ?: return null
        return try {
            findSelectedNode(root)
        } finally {
            root.recycle()
        }
    }

    private fun findSelectedNode(node: AccessibilityNodeInfo): String? {
        val start = node.textSelectionStart
        val end = node.textSelectionEnd
        if (start in 0..<end) {
            val text = node.text ?: return searchChildren(node)
            return try {
                text.subSequence(start, end).toString().takeIf { it.length >= 3 }
                    ?: searchChildren(node)
            } catch (e: Exception) {
                searchChildren(node)
            }
        }

        return searchChildren(node)
    }

    private fun searchChildren(node: AccessibilityNodeInfo): String? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findSelectedNode(child)
            child.recycle()
            if (result != null) return result
        }
        return null
    }

    private fun extractSelectedText(node: AccessibilityNodeInfo): String? {
        val start = node.textSelectionStart
        val end = node.textSelectionEnd
        if (start !in 0..<end) return null
        return try {
            node.text?.subSequence(start, end)?.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun getClipboardText(): String? {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        return cm?.primaryClip?.getItemAt(0)?.text?.toString()
    }

    companion object {
        private const val TAG = "AIAccessibilityService"
    }
}