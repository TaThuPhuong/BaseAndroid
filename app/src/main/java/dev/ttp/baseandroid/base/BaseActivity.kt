package dev.ttp.baseandroid.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import dev.ttp.baseandroid.exstension.StatusBarCompat

abstract class BaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) :
    AppCompatActivity() {

    open lateinit var binding: B

    protected abstract fun setupData()

    protected abstract fun setupObserver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
        StatusBarCompat.translucentStatusBar(this, true)
        setupData()
        setupObserver()
    }
}