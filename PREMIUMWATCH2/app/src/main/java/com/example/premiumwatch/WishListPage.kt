package com.example.premiumwatch

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.premiumwatch.databinding.ActivityWishListPageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.lang.Exception

class WishListPage : AppCompatActivity() {

    private var selectedImage: String? = ""
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityWishListPageBinding
    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishListPageBinding.inflate(layoutInflater)
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


        binding.buttonWList.setOnClickListener {
            val intent = Intent(this,WishListListPage::class.java)
            startActivity(intent)
        }

    }

    // saves to firebase database

    fun save_data(view: View) {
        val price = binding.editTextWish3.text.toString()
        val name = binding.editTextWish.text.toString()
        val year = binding.editTextWish2.text.toString()

        if (name.isEmpty() || year.isEmpty() || price.isEmpty() || selectedImage.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Watch Wish List")
        val watch = Watch(name, year, price, selectedImage,)
        val id = database.push().key

        id?.let {
            database.child(it).setValue(watch).addOnSuccessListener {
                binding.editTextWish3.text.clear()
                binding.editTextWish.text.clear()
                binding.editTextWish2.text.clear()
                selectedImage = ""

                Toast.makeText(this, "Hooray! Watch Data Stored", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WishListListPage::class.java)
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

    private val CameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            binding.imageViewUpload.setImageBitmap(imageBitmap)
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            selectedImage = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        }
    }

    fun captureImage(view: View) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        CameraActivityResultLauncher.launch(cameraIntent)
    }
}