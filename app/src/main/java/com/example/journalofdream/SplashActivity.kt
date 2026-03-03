package com.example.journalofdream

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.logo)

        lifecycleScope.launch {
            // Ждём 1 секунду перед началом анимации
            delay(1000)

            // Запускаем анимацию затухания
            val fadeOut = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
            }
            fadeOut.start()

            // Ждём пока анимация закончится
            delay(500)

            // Переходим на MainActivity
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
}
