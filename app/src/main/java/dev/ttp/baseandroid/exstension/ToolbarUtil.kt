package dev.ttp.baseandroid.exstension

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ToolbarUtil {

    /**
     * Set up the toolbar for the activity and set it as the action bar.
     */
    fun setupToolbar(activity: AppCompatActivity, toolbar: Toolbar) {
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
    }

    /**
     * Set the toolbar background color.
     */
    fun setToolbarBackgroundColor(toolbar: Toolbar, color: Int) {
        toolbar.setBackgroundColor(color)
    }

    /**
     * Show the toolbar
     */
    fun showToolbar(toolbar: Toolbar) {
        toolbar.visibility = View.VISIBLE
    }

    /**
     * Hide the toolbar
     */
    fun hideToolbar(toolbar: Toolbar) {
        toolbar.visibility = View.GONE
    }

}
