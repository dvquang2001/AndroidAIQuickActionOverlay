package com.qcp.aioverlay.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.qcp.aioverlay.service.AIAccessibilityService
import com.qcp.aioverlay.ui.main.Greeting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var service: AIAccessibilityService? = null
    private var windowManager: WindowManager? = null

    private var floatingButtonView: ComposeView? = null
    private var overlayPanelView: ComposeView? = null

    private var coroutineScope = CoroutineScope(AndroidUiDispatcher.Main + SupervisorJob())

    fun attachService(svc: AIAccessibilityService) {
        service = svc
        windowManager = svc.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun detachService() {
        hideAll()
        service = null
        windowManager = null
        coroutineScope.cancel()
    }

    fun showFloatingButton(selectedText: String) {
        val wm = windowManager ?: return
        if(floatingButtonView != null) {
            // update text in existing view rather than re-adding
            return
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 200
        }

        floatingButtonView = createComposeView(service!!) {
            FloatingButtonContent(
                onActionClick = { showOverlayPanel(selectedText) },
                onDismiss = { hideFloatingButton() }
            )
        }

        wm.addView(floatingButtonView, params)
    }

    fun hideFloatingButton() {
        floatingButtonView?.let {
            windowManager?.removeViewImmediate(it)
            floatingButtonView = null
        }
    }

    fun showOverlayPanel(selectedText: String) {
        val wm = windowManager ?: return
        if(overlayPanelView != null) return

        hideFloatingButton()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM
        }

        overlayPanelView = createComposeView(service!!) {
            OverlayScreen(
                selectedText = selectedText,
                onDismiss = { hideOverlayPanel() }
            )
        }

        wm.addView(overlayPanelView, params)
    }

    fun hideOverlayPanel() {
        overlayPanelView?.let {
            windowManager?.removeViewImmediate(it)
            overlayPanelView = null
        }
    }

    fun hideAll() {
        hideFloatingButton()
        hideOverlayPanel()
    }

    private fun createComposeView(
        service: AIAccessibilityService,
        content: @Composable () -> Unit,
    ): ComposeView {
        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.start()

        return ComposeView(service).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            val recomposer = Recomposer(coroutineScope.coroutineContext)
            compositionContext = recomposer
            coroutineScope.launch {
                recomposer.runRecomposeAndApplyChanges()
            }

            setContent { content() }
        }
    }

}