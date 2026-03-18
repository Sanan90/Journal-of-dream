package com.dreamjournal.journalofdream

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.dreamjournal.journalofdream.R
import kotlinx.coroutines.launch

import com.dreamjournal.journalofdream.util.LocaleHelper

class SplashActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.logo)

        lifecycleScope.launch {
            // Запускаем анимацию затухания сразу
            val fadeOut = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f).apply {
                duration = 600
                startDelay = 400 // небольшая пауза чтобы лого было видно
                interpolator = AccelerateDecelerateInterpolator()
            }
            fadeOut.start()

            // Ждём ровно столько сколько длится анимация
            android.animation.AnimatorSet().apply {
                play(fadeOut)
                start()
            }

            // Переходим сразу после анимации (400мс пауза + 600мс анимация = 1000мс суммарно)
            fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }
            })
        }
    }
}
