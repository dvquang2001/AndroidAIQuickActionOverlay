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

    // Debounce: text selection events fire rapidly while the user drags the
    // selection handles. We wait SELECTION_DEBOUNCE_MS after the last event
    // before showing the button so the UI doesn't flicker mid-drag.
    private var pendingSelectedText: String? = null
    private val showButtonRunnable = Runnable {
        val text = pendingSelectedText ?: return@Runnable
        Log.d(TAG, "Debounced show — selected: \"$text\"")
        overlayManager.showFloatingButton(text)
    }

    // ===== Lifecycle =====

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "onServiceConnected")
        overlayManager.attachService(this)
    }

    override fun onDestroy() {
        handler.removeCallbacks(showButtonRunnable)
        overlayManager.detachService()
        super.onDestroy()
    }

    // ===== Event handling =====

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {

            // ── Text selection in non-editable views (TextView, WebView, etc.) ──
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                val source = event.source ?: return

                // Guard 1 — skip editable fields (EditText, input, password).
                //
                // When the user taps into or types in an EditText, Android fires
                // TYPE_VIEW_TEXT_SELECTION_CHANGED to report the new cursor position
                // (a zero-length selection).  We must ignore these events entirely
                // so the floating button never appears while the user is typing.
                //
                // isEditable is true for any view that accepts keyboard input,
                // including EditText, AppCompatEditText, and most custom inputs.
                if (source.isEditable) {
                    Log.d(TAG, "SELECTION_CHANGED ignored — editable (${source.className})")
                    return
                }

                // Guard 2 — actual text must be selected (selectionStart < selectionEnd).
                // extractSelectedText returns null when start == end (cursor only, no selection).
                //
                // NOTE: The previous code had a fallback that read event.text.firstOrNull()
                // here as a "WebView fallback". That was WRONG — event.text contains the
                // entire text content of the view, not the selected portion. It caused the
                // button to appear whenever ANY view with 3+ chars fired this event.
                // WebView selections are handled via the TYPE_VIEW_LONG_CLICKED path instead.
                val text = extractSelectedText(source)
                if (!text.isNullOrBlank() && text.length >= 3) {
                    Log.d(TAG, "SELECTION_CHANGED — selected: \"$text\" in ${source.className}")
                    handler.removeCallbacks(showButtonRunnable)
                    pendingSelectedText = text
                    handler.postDelayed(showButtonRunnable, SELECTION_DEBOUNCE_MS)
                } else {
                    // Empty or too-short selection — hide if showing.
                    handler.removeCallbacks(showButtonRunnable)
                    pendingSelectedText = null
                    overlayManager.hideFloatingButton()
                }
            }

            // ── Long press on a non-editable node ─────────────────────────────
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                val source = event.source

                // Skip long-clicks inside editable fields.  The user long-pressing
                // inside an EditText is initiating text editing, not requesting an
                // AI action on static content.
                if (source?.isEditable == true) {
                    Log.d(TAG, "LONG_CLICKED ignored — editable (${source.className})")
                    return
                }

                Log.d(TAG, "LONG_CLICKED on ${source?.className}")
                lastLongClickedNode = source
                handler.postDelayed({ handleLongClick() }, LONG_CLICK_SCAN_DELAY_MS)
            }

            // ── Window change following a long-click ──────────────────────────
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (lastLongClickedNode != null) {
                    val text = findSelectedTextInWindow()
                    if (!text.isNullOrBlank() && text.length >= 3) {
                        Log.d(TAG, "WINDOW_CONTENT_CHANGED — scan found: \"$text\"")
                        overlayManager.showFloatingButton(text)
                        lastLongClickedNode = null
                    }
                }
            }

            else -> Unit
        }
    }

    override fun onInterrupt() {
        handler.removeCallbacks(showButtonRunnable)
        overlayManager.hideAll()
    }

    // ===== Private helpers =====

    private fun handleLongClick() {
        val windowText = findSelectedTextInWindow()
        if (!windowText.isNullOrBlank() && windowText.length >= 3) {
            Log.d(TAG, "Long click → window text: \"$windowText\"")
            overlayManager.showFloatingButton(windowText)
            lastLongClickedNode = null
            return
        }

        val nodeText = lastLongClickedNode?.text?.toString()
        if (!nodeText.isNullOrBlank() && nodeText.length >= 3) {
            Log.d(TAG, "Long click → node text: \"$nodeText\"")
            overlayManager.showFloatingButton(nodeText)
            lastLongClickedNode = null
            return
        }

        // Direct clipboard read is blocked on Android 10+ when not in focus.
        // Perform ACTION_COPY via the accessibility API (permitted), then read
        // clipboard after the copy completes.
        val node = lastLongClickedNode
        if (node != null) {
            node.performAction(AccessibilityNodeInfo.ACTION_COPY)
            handler.postDelayed({
                val clipText = getClipboardText()
                if (!clipText.isNullOrBlank() && clipText.length >= 3) {
                    Log.d(TAG, "Long click → ACTION_COPY clipboard: \"$clipText\"")
                    overlayManager.showFloatingButton(clipText)
                }
                lastLongClickedNode = null
            }, CLIPBOARD_READ_DELAY_MS)
        }
    }

    /**
     * Traverse the full view tree from root and find a node with an active
     * text selection. Editable nodes are skipped — we only process selections
     * in non-editable content (TextView, WebView, etc.).
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
        // Never report selections inside editable fields — even if the tree-walk
        // finds a non-zero selection range in an EditText (e.g. the user selected
        // all text via the context menu), we do not want to surface the AI button.
        if (node.isEditable) return searchChildren(node)

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

    /**
     * Returns the selected substring from [node], or null if no real selection
     * exists (start == end means cursor only, not a selection).
     */
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
        private const val SELECTION_DEBOUNCE_MS = 300L    // wait for drag to settle
        private const val LONG_CLICK_SCAN_DELAY_MS = 300L // existing delay, unchanged
        private const val CLIPBOARD_READ_DELAY_MS = 300L  // existing delay, unchanged
    }
}
