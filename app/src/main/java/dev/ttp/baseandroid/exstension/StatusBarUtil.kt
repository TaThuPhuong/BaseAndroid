package dev.ttp.baseandroid.exstension

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import java.lang.ref.WeakReference
import kotlin.math.abs

// Object to handle StatusBar compatibility and modifications
object StatusBarUtil {

    // Set the StatusBar color
    fun setStatusBarColor(activity: Activity, @ColorInt color: Int) {
        val window: Window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30 and above
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = false  // Set StatusBar icons to dark
            window.statusBarColor = color  // Deprecated, but still works
        } else { // API 21 to 29
            window.statusBarColor = color  // Deprecated, but still works
        }
    }

    // Make the StatusBar transparent
    fun setStatusBarTranslucent(activity: Activity) {
        val window: Window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30 and above
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = false  // Set icons to dark if needed
            window.statusBarColor = Color.TRANSPARENT // Make StatusBar transparent
        } else { // API 24 to 29
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    // Hide the StatusBar
    fun hideStatusBar(activity: Activity) {
        val window: Window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30 and above
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            // Hide the StatusBar
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
        } else { // API 19 to 29
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    // Show the StatusBar
    fun showStatusBar(activity: Activity) {
        val window: Window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30 and above
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            // Show the StatusBar
            insetsController.show(WindowInsetsCompat.Type.statusBars())
        } else { // API 19 to 29
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    // Get the height of the StatusBar
    private fun getStatusBarHeight(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above, use WindowInsets
            val insets = activity.window.decorView.rootWindowInsets
            insets?.getInsets(WindowInsets.Type.systemBars())?.top ?: 0
        } else {
            // For lower versions, use a fixed height if needed
            val resources = activity.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }
    }

    // Set the StatusBar color based on CollapsingToolbar behavior
    fun setStatusBarColorForCollapsingToolbar(
        activity: Activity,
        appBarLayout: AppBarLayout?,
        collapsingToolbarLayout: CollapsingToolbarLayout?,
        toolbar: Toolbar?,
        @ColorInt statusColor: Int
    ) {
        appBarLayout?.let { appBar ->
            collapsingToolbarLayout?.let { collapsingToolbar ->
                toolbar?.let { tool ->
                    val window = activity.window
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbar) { _, insets -> insets }

                    val mContentView =
                        window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
                    val mChildView = mContentView.getChildAt(0)
                    mChildView?.let {
                        ViewCompat.setFitsSystemWindows(it, false)
                        ViewCompat.requestApplyInsets(it)
                    }

                    (appBar.parent as View).fitsSystemWindows = false
                    appBar.fitsSystemWindows = false
                    tool.fitsSystemWindows = false

                    // Apply padding if tag is not set
                    if (tool.tag == null) {
                        val lp = tool.layoutParams as CollapsingToolbarLayout.LayoutParams
                        val statusBarHeight = getStatusBarHeight(activity)
                        lp.height += statusBarHeight
                        tool.layoutParams = lp
                        tool.setPadding(
                            tool.paddingLeft,
                            tool.paddingTop + statusBarHeight,
                            tool.paddingRight,
                            tool.paddingBottom
                        )
                        tool.tag = true
                    }

                    // Check the Behavior of AppBarLayout
                    val behavior = (appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior
                    if (behavior is AppBarLayout.Behavior) {
                        val verticalOffset = behavior.topAndBottomOffset
                        window.statusBarColor =
                            if (abs(verticalOffset) > appBar.height - collapsingToolbar.scrimVisibleHeightTrigger) {
                                statusColor
                            } else {
                                Color.TRANSPARENT
                            }
                    } else {
                        window.statusBarColor = Color.TRANSPARENT
                    }

                    collapsingToolbar.fitsSystemWindows = false
                    val windowWeakReference = WeakReference(window)

                    // Listen for Offset changes of AppBarLayout
                    appBar.addOnOffsetChangedListener { _, verticalOffset ->
                        val weakWindow = windowWeakReference.get()
                        weakWindow?.let {
                            if (abs(verticalOffset) > appBar.height - collapsingToolbar.scrimVisibleHeightTrigger) {
                                if (it.statusBarColor != statusColor) {
                                    startColorAnimation(
                                        it.statusBarColor,
                                        statusColor,
                                        collapsingToolbar.scrimAnimationDuration,
                                        windowWeakReference
                                    )
                                }
                            } else {
                                if (it.statusBarColor != Color.TRANSPARENT) {
                                    startColorAnimation(
                                        it.statusBarColor,
                                        Color.TRANSPARENT,
                                        collapsingToolbar.scrimAnimationDuration,
                                        windowWeakReference
                                    )
                                }
                            }
                        }
                    }

                    collapsingToolbar.getChildAt(0).fitsSystemWindows = false
                    collapsingToolbar.setStatusBarScrimColor(statusColor)
                }
            }
        }
    }

    // Use ValueAnimator to transition StatusBar color when using CollapsingToolbarLayout
    private fun startColorAnimation(
        startColor: Int,
        endColor: Int,
        duration: Long,
        windowWeakReference: WeakReference<Window>
    ) {
        if (sAnimator.isStarted) { // Check if the animator has already started
            sAnimator.cancel()
        }
        sAnimator = ValueAnimator.ofArgb(startColor, endColor).apply {
            this.duration = duration
            addUpdateListener { valueAnimator ->
                val window = windowWeakReference.get()
                window?.let {
                    it.statusBarColor = (valueAnimator.animatedValue as Int)
                }
            }
            start()
        }
    }

    private var sAnimator: ValueAnimator = ValueAnimator()

}
