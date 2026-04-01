package com.qcp.aioverlay.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.qcp.aioverlay.domain.usecase.ProcessTextUseCase
import com.qcp.aioverlay.service.AIAccessibilityService
import com.qcp.aioverlay.ui.overlay.FloatingButtonContent
import com.qcp.aioverlay.ui.overlay.OverlayScreen
import com.qcp.aioverlay.ui.overlay.OverlayViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val processTextUseCase: ProcessTextUseCase,
) {
    private var service: AIAccessibilityService? = null
    private var windowManager: WindowManager? = null
    private var floatingButtonView: ComposeView? = null
    private var overlayPanelView: ComposeView? = null
    private var coroutineScope = CoroutineScope(AndroidUiDispatcher.Main + SupervisorJob())

    // ── ViewModel instance tái sử dụng ────────────────────────────────────────
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    private var overlayViewModel: OverlayViewModel? = null

    fun attachService(svc: AIAccessibilityService) {
        service = svc
        windowManager = svc.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Khởi tạo lifecycle + ViewModel một lần duy nhất
        overlayLifecycleOwner = OverlayLifecycleOwner().also { it.start() }
        overlayViewModel = createOverlayViewModel(overlayLifecycleOwner!!)
    }

    fun detachService() {
        hideAll()
        overlayLifecycleOwner?.stop()
        overlayLifecycleOwner = null
        overlayViewModel = null
        service = null
        windowManager = null
        coroutineScope.cancel()
    }

    // ── Floating button ────────────────────────────────────────────────────────

    fun showFloatingButton(selectedText: String) {
        val wm = windowManager ?: return
        if (floatingButtonView != null) return

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

        floatingButtonView = createComposeView(service!!, overlayLifecycleOwner!!) {
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

    // ── Overlay panel ──────────────────────────────────────────────────────────

    fun showOverlayPanel(selectedText: String) {
        val wm = windowManager ?: return
        if (overlayPanelView != null) return
        hideFloatingButton()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.BOTTOM }

        overlayPanelView = createComposeView(service!!, overlayLifecycleOwner!!) {
            OverlayScreen(
                selectedText = selectedText,
                viewModel = overlayViewModel!!, // ← pass trực tiếp, không dùng hiltViewModel()
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

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun createOverlayViewModel(lifecycleOwner: OverlayLifecycleOwner): OverlayViewModel {
        val factory = overlayViewModelFactory(processTextUseCase)
        return ViewModelProvider(lifecycleOwner, factory)[OverlayViewModel::class.java]
    }

    private fun createComposeView(
        service: AIAccessibilityService,
        lifecycleOwner: OverlayLifecycleOwner,
        content: @Composable () -> Unit,
    ): ComposeView {
        return ComposeView(service).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            val recomposer = Recomposer(coroutineScope.coroutineContext)
            compositionContext = recomposer
            coroutineScope.launch { recomposer.runRecomposeAndApplyChanges() }

            setContent { content() }
        }
    }

    companion object {
        private const val TAG = "OverlayManager"
    }
}