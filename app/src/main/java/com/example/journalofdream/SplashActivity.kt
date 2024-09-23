package com.example.journalofdream

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Получение ссылки на ImageView
        val logo: ImageView = findViewById(R.id.logo)

        // Создание анимации
        val fadeOut = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f).apply {
            duration = 500 // Длительность анимации
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Запуск анимации с задержкой
        Handler(Looper.getMainLooper()).postDelayed({
            fadeOut.start()
        }, 1000)

        // Переход на MainActivity после завершения анимации
        fadeOut.doOnEnd {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Применение анимации перехода
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            finish()
        }
    }
}
