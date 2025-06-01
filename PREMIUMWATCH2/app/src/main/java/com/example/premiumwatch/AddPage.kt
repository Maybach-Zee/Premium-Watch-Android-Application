package com.example.premiumwatch

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.premiumwatch.databinding.ActivityAddPageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class AddPage : AppCompatActivity() {
    private var selectedImage: String? = ""
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityAddPageBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.buttonList.setOnClickListener {
            val intent = Intent(this, AddListPage::class.java)
            startActivity(intent)
        }

        binding.buttonLogOut.setOnClickListener {
            val intent = Intent(this, LandingLoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.buttonWish.setOnClickListener {
            val intent = Intent(this, WishListPage::class.java)
            startActivity(intent)
        }

        binding.buttonMaintP.setOnClickListener {
            val intent = Intent(this, MaintenancePage::class.java)
            startActivity(intent)
        }

        binding.buttonCamera.setOnClickListener {
            captureImage()
        }
    }

    fun save_data(view: View) {
        val name = binding.editTextName.text.toString()
        val year = binding.editTextYear.text.toString()
        val price = binding.editTextPrice.text.toString()

        if (name.isEmpty() || year.isEmpty() || price.isEmpty() || selectedImage.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Watch List")
        val watch = Watch(name, year, price, selectedImage)
        val id = database.push().key

        id?.let {
            database.child(it).setValue(watch).addOnSuccessListener {
                updateItemCount()
                binding.editTextName.text.clear()
                binding.editTextYear.text.clear()
                binding.editTextPrice.text.clear()
                selectedImage = ""

                Toast.makeText(this, "Hooray! Watch Data Stored", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AddListPage::class.java)
                startActivity(intent)
            }.addOnFailureListener {
                Toast.makeText(this, "Watch Data Not Stored!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateItemCount() {
        val countRef = FirebaseDatabase.getInstance().getReference("ItemCount")
        countRef.get().addOnSuccessListener {
            val count = it.value as? Long ?: 0
            countRef.setValue(count + 1).addOnSuccessListener {
                checkAchievements(count + 1)
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

    fun insert_image(view: View) {
        val myFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        myFileIntent.type = "image/*"
        ActivityResultLauncher.launch(myFileIntent)
    }

    private val ActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                val bytes = stream.toByteArray()
                selectedImage = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                binding.imageViewUpload.setImageBitmap(myBitmap)
                inputStream!!.close()
                Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(this, ex.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun captureImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        CameraResultLauncher.launch(cameraIntent)
    }

    private val CameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            val stream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            val bytes = stream.toByteArray()
            selectedImage = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            binding.imageViewUpload.setImageBitmap(imageBitmap)
            Toast.makeText(this, "Image Captured!", Toast.LENGTH_SHORT).show()
        }
    }
}
