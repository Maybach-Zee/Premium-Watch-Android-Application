package com.example.premiumwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AchievementPage : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var starterTextView: TextView
    private lateinit var collectorTextView: TextView
    private lateinit var packratTextView: TextView

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement_page)

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

        val buttonlist: Button = findViewById(R.id.backtolist)
        buttonlist.setOnClickListener {
            val intent = Intent(this, AddListPage::class.java)
            startActivity(intent)
        }

        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        starterTextView = findViewById(R.id.starterAchievement)
        collectorTextView = findViewById(R.id.collectorAchievement)
        packratTextView = findViewById(R.id.packratAchievement)

        database = FirebaseDatabase.getInstance().getReference("ItemCount")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.value as? Long ?: 0
                updateProgress(count)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        val achievementRef = FirebaseDatabase.getInstance().getReference("Achievements")
        achievementRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val achievements = snapshot.value as? Map<String, Boolean> ?: emptyMap()
                updateAchievements(achievements)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateProgress(count: Long) {
        val goal = 10
        val progress = (count.toDouble() / goal * 100).toInt()
        progressBar.progress = progress
        progressText.text = "$count / $goal"

        // Check if goal is reached
        if (count >= goal) {
            Toast.makeText(this, "Congratulations! You've achieved the goal!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAchievements(achievements: Map<String, Boolean>) {
        starterTextView.text = if (achievements["Starter"] == true) "Starter: Achieved" else "Starter: Not Achieved"
        collectorTextView.text = if (achievements["Collector"] == true) "Collector: Achieved" else "Collector: Not Achieved"
        packratTextView.text = if (achievements["Packrat"] == true) "Packrat: Achieved" else "Packrat: Not Achieved"
    }

    override fun onResume() {
        super.onResume()
        // Update achievements when this activity is resumed
        val achievementRef = FirebaseDatabase.getInstance().getReference("Achievements")
        achievementRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val achievements = snapshot.value as? Map<String, Boolean> ?: emptyMap()
                updateAchievements(achievements)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


}
