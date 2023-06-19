package dev.ttp.baseandroid.exstension

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import java.lang.ref.WeakReference

object StatusBarCompat {
    //Get alpha color
    private fun calculateStatusBarColor(color: Int, alpha: Int): Int {
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }

    /**
     * set statusBarColor
     * @param statusColor color
     * @param alpha       0 - 255
     */
    fun setStatusBarColor(activity: Activity, @ColorInt statusColor: Int, alpha: Int) {
        setStatusBarColor(activity, calculateStatusBarColor(statusColor, alpha))
    }

    /**
     * set StatusBarColor
     *
     * 1. set Flags to call setStatusBarColor
     * 2. call setSystemUiVisibility to clear translucentStatusBar's Flag.
     * 3. set FitsSystemWindows to false
     */
    private fun setStatusBarColor(activity: Activity, @ColorInt statusColor: Int) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusColor
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        val mContentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
    }

    fun translucentStatusBar(activity: Activity) {
        translucentStatusBar(activity, false)
    }

    /**
     * translucentStatusBar(full-screen)
     *
     * 1. set Flags to full-screen
     * 2. set FitsSystemWindows to false
     *
     * @param hideStatusBarBackground hide statusBar's shadow
     */
    fun translucentStatusBar(activity: Activity, hideStatusBarBackground: Boolean) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (hideStatusBarBackground) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        val mContentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
    }

    fun setStatusBarColorForCollapsingToolbar(
        activity: Activity,
        appBarLayout: AppBarLayout?,
        collapsingToolbarLayout: CollapsingToolbarLayout?,
        toolbar: Toolbar?,
        @ColorInt statusColor: Int
    ) {
        appBarLayout?.let {
            collapsingToolbarLayout?.let { it1 ->
                toolbar?.let { it2 ->
                    val window = activity.window
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = Color.TRANSPARENT
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    ViewCompat.setOnApplyWindowInsetsListener(
                        collapsingToolbarLayout
                    ) { v, insets -> insets }
                    val mContentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
                    val mChildView = mContentView.getChildAt(0)
                    if (mChildView != null) {
                        ViewCompat.setFitsSystemWindows(mChildView, false)
                        ViewCompat.requestApplyInsets(mChildView)
                    }
                    (appBarLayout.parent as View).fitsSystemWindows = false
                    appBarLayout.fitsSystemWindows = false
                    toolbar.fitsSystemWindows = false
                    if (toolbar.tag == null) {
                        val lp = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
                        val statusBarHeight = getStatusBarHeight(activity)
                        lp.height += statusBarHeight
                        toolbar.layoutParams = lp
                        toolbar.setPadding(
                            toolbar.paddingLeft,
                            toolbar.paddingTop + statusBarHeight,
                            toolbar.paddingRight,
                            toolbar.paddingBottom
                        )
                        toolbar.tag = true
                    }
                    val behavior = (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior
                    if (behavior != null && behavior is AppBarLayout.Behavior) {
                        val verticalOffset = behavior.topAndBottomOffset
                        if (Math.abs(verticalOffset) > appBarLayout.height - collapsingToolbarLayout.scrimVisibleHeightTrigger) {
                            window.statusBarColor = statusColor
                        } else {
                            window.statusBarColor = Color.TRANSPARENT
                        }
                    } else {
                        window.statusBarColor = Color.TRANSPARENT
                    }
                    collapsingToolbarLayout.fitsSystemWindows = false
                    val windowWeakReference = WeakReference(window)
                    appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                        val weakWindow = windowWeakReference.get()
                        if (weakWindow != null) {
                            if (Math.abs(verticalOffset) > appBarLayout.height - collapsingToolbarLayout.scrimVisibleHeightTrigger) {
                                if (weakWindow.statusBarColor != statusColor) {
                                    startColorAnimation(
                                        weakWindow.statusBarColor,
                                        statusColor,
                                        collapsingToolbarLayout.scrimAnimationDuration,
                                        windowWeakReference
                                    )
                                }
                            } else {
                                if (weakWindow.statusBarColor != Color.TRANSPARENT) {
                                    startColorAnimation(
                                        weakWindow.statusBarColor,
                                        Color.TRANSPARENT,
                                        collapsingToolbarLayout.scrimAnimationDuration,
                                        windowWeakReference
                                    )
                                }
                            }
                        }
                    })
                    collapsingToolbarLayout.getChildAt(0).fitsSystemWindows = false
                    collapsingToolbarLayout.setStatusBarScrimColor(statusColor)
                }
            }
        }
    }

    fun changeToLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        val window = activity.window ?: return
        val decorView = window.decorView
        decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun cancelLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        val window = activity.window ?: return
        val decorView = window.decorView
        decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

    /**
     * return statusBar's Height in pixels
     */
    private fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            result = context.resources.getDimensionPixelOffset(resId)
        }
        return result
    }


    /**
     * use ValueAnimator to change statusBarColor when using collapsingToolbarLayout
     */
    private fun startColorAnimation(
        startColor: Int,
        endColor: Int,
        duration: Long,
        windowWeakReference: WeakReference<Window>
    ) {
        if (sAnimator != null) {
            sAnimator.cancel()
        }
        sAnimator = ValueAnimator.ofArgb(startColor, endColor)
            .setDuration(duration)
        sAnimator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { valueAnimator ->
            val window = windowWeakReference.get()
            if (window != null) {
                window.statusBarColor = (valueAnimator.animatedValue as Int)
            }
        })
        sAnimator.start()
    }

    private var sAnimator = ValueAnimator()

}