package com.justanotherdeveloper.listhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val handler = Handler()

        handler.postDelayed({
            beginTransition(logoParent)
            logo.visibility = View.VISIBLE
            handler.postDelayed({
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }, SPLASH_SCREEN_DELAY)
        }, LOGO_FADE_DELAY)

    }
}
