package com.example.premiumwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.example.premiumwatch.databinding.ActivityHomePageBinding
import com.example.premiumwatch.databinding.ActivityLogInPageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.net.URL

class HomePage : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                    true
                }

                R.id.achievement -> {
                    val intent = Intent(this, AchievementPage::class.java)
                    startActivity(intent)
                    true
                }

                R.id.collection -> {
                    val intent = Intent(this, AddPage::class.java)
                    startActivity(intent)
                    true
                }



                else -> false
            }

        }


    }
}