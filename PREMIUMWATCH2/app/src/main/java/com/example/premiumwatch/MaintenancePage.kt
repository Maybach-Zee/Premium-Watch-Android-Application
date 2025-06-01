package com.example.premiumwatch

import android.app.Activity
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
import com.example.premiumwatch.databinding.ActivityMaintenancePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class MaintenancePage : AppCompatActivity() {

    private var selectedImage: String? = ""
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityMaintenancePageBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenancePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, HomePage::class.java))
                    true
                }
                R.id.achievement -> {
                    startActivity(Intent(this, AchievementPage::class.java))
                    true
                }
                R.id.collection -> {
                    startActivity(Intent(this, AddPage::class.java))
                    true
                }
                else -> false
            }
        }

        binding.buttonMList.setOnClickListener {
            startActivity(Intent(this, MaintenanceListPage::class.java))
        }

        binding.buttonChooseImage.setOnClickListener {
            val myFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            ActivityResultLauncher.launch(myFileIntent)
        }

        binding.buttonCaptureImage.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureImageResultLauncher.launch(takePictureIntent)
        }
    }

    fun save_data(view: View) {
        val price = binding.editTextDes.text.toString()
        val name = binding.editTextN.text.toString()
        val year = binding.editTextY.text.toString()

        if (name.isEmpty() || year.isEmpty() || price.isEmpty() || selectedImage.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Watch Maintenance List")
        val watch = Watch(name, year, price, selectedImage)
        val id = database.push().key

        id?.let {
            database.child(it).setValue(watch).addOnSuccessListener {
                binding.editTextDes.text.clear()
                binding.editTextN.text.clear()
                binding.editTextY.text.clear()
                selectedImage = ""

                Toast.makeText(this, "Hooray! Watch Data Stored", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MaintenanceListPage::class.java)
                startActivity(intent)
            }.addOnFailureListener {
                Toast.makeText(this, "Watch Data Not Stored!", Toast.LENGTH_SHORT).show()
            }
        }
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

    private val captureImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            val stream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bytes = stream.toByteArray()
            selectedImage = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            binding.imageViewUpload.setImageBitmap(imageBitmap)
            Toast.makeText(this, "Image Captured!", Toast.LENGTH_SHORT).show()
        }
    }
}
