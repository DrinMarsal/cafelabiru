package com.example.cafelabiru

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.databinding.ActivityAddMenuBinding
import com.example.cafelabiru.model.FoodModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMenuBinding
    private lateinit var database: DatabaseReference
    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        binding.btnPickImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        binding.btnSaveMenu.setOnClickListener {
            val name = binding.etMenuName.text.toString().trim()
            val desc = binding.etMenuDesc.text.toString().trim()
            val categories = binding.etMenuCategories.text.toString().trim()
            val price = binding.etMenuPrice.text.toString().toDoubleOrNull()

            if (name.isBlank() || desc.isBlank() || price == null || imageUri == null) {
                Toast.makeText(this, "Lengkapi semua data dan pilih gambar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            database.child("menu").get().addOnSuccessListener { snapshot ->
                val lastId = snapshot.children.mapNotNull {
                    it.key?.removePrefix("M")?.toIntOrNull()
                }.maxOrNull() ?: 0

                val newIdNumber = lastId + 1
                val menuId = "M" + String.format("%03d", newIdNumber)

                val imageRef = storageRef.child("menu_images/$menuId.jpg")

                imageRef.putFile(imageUri!!)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val food = FoodModel(
                                menuId = menuId,
                                name = name,
                                description = desc,
                                price = price,
                                imageUrl = uri.toString()
                            )
                            database.child("menu").child(menuId).setValue(food)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Gagal menyimpan data menu", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Upload gambar gagal", Toast.LENGTH_SHORT).show()
                    }

            }.addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data untuk generate ID", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            imageUri = data?.data
            binding.imgPreview.setImageURI(imageUri)
        }
    }
}