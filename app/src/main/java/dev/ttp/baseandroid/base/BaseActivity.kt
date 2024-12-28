package dev.ttp.baseandroid.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import dev.ttp.baseandroid.exstension.StatusBarUtil

abstract class BaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) :
    AppCompatActivity() {

    open lateinit var binding: B

    protected abstract fun setupData()

    protected abstract fun setupObserver()

    protected open fun setupToolbar(
        toolbar: Toolbar,
        title: String? = null,
        isBackButtonEnabled: Boolean = false
    ) {
        setSupportActionBar(toolbar)
        title?.let {
            supportActionBar?.title = it
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(isBackButtonEnabled)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusBarTranslucent(this)
        setupData()
        setupObserver()
    }
}