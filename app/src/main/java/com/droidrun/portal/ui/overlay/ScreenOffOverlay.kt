package com.droidrun.portal.ui.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

/**
 * Manages a full-screen black overlay to simulate screen off state.
 * Uses TYPE_ACCESSIBILITY_OVERLAY for maximum coverage.
 */
class ScreenOffOverlay(private val context: Context) {

    companion object {
        private const val TAG = "ScreenOffOverlay"
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null

    val isShowing: Boolean
        get() = overlayView != null

    fun show() {
        if (overlayView != null) {
            Log.d(TAG, "Overlay already showing")
            return
        }

        handler.post {
            try {
                val view = FrameLayout(context).apply {
                    setBackgroundColor(Color.BLACK)
                }

                val params = createLayoutParams()

                windowManager.addView(view, params)
                overlayView = view
                Log.i(TAG, "Screen off overlay shown")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show overlay", e)
            }
        }
    }

    fun hide() {
        handler.post {
            try {
                overlayView?.let {
                    windowManager.removeView(it)
                    overlayView = null
                    Log.i(TAG, "Screen off overlay hidden")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to hide overlay", e)
            }
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE
        )

        params.gravity = Gravity.TOP or Gravity.START

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        return params
    }
}
