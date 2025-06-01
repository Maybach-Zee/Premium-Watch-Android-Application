package com.example.premiumwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LandingLoginPage : AppCompatActivity() {

    lateinit var LogIn: Button
    lateinit var SignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_login_page)

        LogIn = findViewById(R.id.LogIn)
        LogIn.setOnClickListener {

            val intent = Intent(this, LogInPage::class.java)
            startActivity(intent)
        }

        SignUp = findViewById(R.id.SignUp)
        SignUp.setOnClickListener {
            val intent = Intent(this, SignUpPage::class.java)
            startActivity(intent)
        }
    }
}
