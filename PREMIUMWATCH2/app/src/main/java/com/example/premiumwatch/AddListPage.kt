package com.example.premiumwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import android.app.AlertDialog
import android.widget.Toast

class AddListPage : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var itemArrayList: ArrayList<Watch>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_list_page)

        itemRecyclerView = findViewById(R.id.itemList)
        itemRecyclerView.layoutManager = LinearLayoutManager(this)
        itemRecyclerView.setHasFixedSize(true)

        itemArrayList = arrayListOf()
        getItemData()

        val achievementButton: Button = findViewById(R.id.achievementButton)
        achievementButton.setOnClickListener {
            val intent = Intent(this, AchievementPage::class.java)
            startActivity(intent)
        }

        val buttonAdd: Button = findViewById(R.id.buttonAdd)
        buttonAdd.setOnClickListener {
            val intent = Intent(this, AddPage::class.java)
            startActivity(intent)
        }
    }

    private fun getItemData() {
        database = FirebaseDatabase.getInstance().getReference("Watch List")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemArrayList.clear()
                for (itemSnapshot in snapshot.children) {
                    val watch = itemSnapshot.getValue(Watch::class.java)
                    watch?.let {
                        it.id = itemSnapshot.key
                        itemArrayList.add(it)
                    }
                }
                val adapter = ItemAdapter(itemArrayList) { watch ->
                    showDeleteDialog(watch)
                }
                itemRecyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddListPage", "Failed to read data", error.toException())
            }
        })
    }

    private fun showDeleteDialog(watch: Watch) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this item?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                deleteItem(watch)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }



    private fun deleteItem(watch: Watch) {
        watch.id?.let {
            database.child(it).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                // Update ItemCount
                updateItemCount(-1)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateItemCount(change: Long) {
        val countRef = FirebaseDatabase.getInstance().getReference("ItemCount")
        countRef.get().addOnSuccessListener {
            val count = it.value as? Long ?: 0
            countRef.setValue(count + change).addOnSuccessListener {
                // Check and update achievements
                checkAchievements(count + change)
            }
        }
    }

    private fun checkAchievements(count: Long) {
        val achievementRef = FirebaseDatabase.getInstance().getReference("Achievements")
        val achievements = mutableMapOf<String, Boolean>()
        achievements["Starter"] = count >= 1
        achievements["Collector"] = count >= 3
        achievements["Packrat"] = count >= 10
        achievementRef.setValue(achievements)
    }
}
