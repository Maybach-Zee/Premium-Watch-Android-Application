package com.example.premiumwatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class MaintenanceListPage : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var itemArrayList: ArrayList<Watch>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintenance_list_page)

        itemRecyclerView = findViewById(R.id.recyclerViewMaintenanceList)
        itemRecyclerView.layoutManager = LinearLayoutManager(this)
        itemRecyclerView.setHasFixedSize(true)

        itemArrayList = arrayListOf<Watch>()
        getItemData()

        val buttonMaint: Button = findViewById(R.id.buttonMaint)
        buttonMaint.setOnClickListener {
            val intent = Intent(this, MaintenancePage::class.java)
            startActivity(intent)
        }
    }

    private fun getItemData() {
        database = FirebaseDatabase.getInstance().getReference("Watch Maintenance List")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemArrayList.clear()
                if (snapshot.exists()) {
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
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MaintenanceListPage", "Failed to read data", error.toException())
            }
        })
    }

    private fun showDeleteDialog(watch: Watch) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this watch?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                deleteWatch(watch)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun deleteWatch(watch: Watch) {
        watch.id?.let {
            database.child(it).removeValue().addOnSuccessListener {
                // Optional: Add a success message or any additional logic after deletion.
            }.addOnFailureListener {
                Log.e("MaintenanceListPage", "Failed to delete item", it)
            }
        }
    }
}
