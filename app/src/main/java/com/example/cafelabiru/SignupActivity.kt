package com.example.cafelabiru

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cafelabiru.databinding.ActivityLoginBinding
import com.example.cafelabiru.databinding.ActivitySignupBinding
import com.example.cafelabiru.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var userName: String
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialization Firebase
        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Tombol Google Sign-In
        binding.btnGoogle.setOnClickListener {
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)
        }

        binding.tvRedirect2.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btnSignup.setOnClickListener {
            userName = binding.edSignupName.text.toString().trim()
            password = binding.edSignupPassword.text.toString().trim()
            email = binding.edSignupEmail.text.toString().trim()

            if (userName.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else if (!binding.termsCheck.isChecked) {
                Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
        }
        binding.termsLink.setOnClickListener {
            val termsText = """
        1. Penggunaan Aplikasi
        Aplikasi ini ditujukan untuk melakukan pemesanan makanan dan minuman secara online dari Cafe Labiru.

        2. Akurasi Informasi
        Kami berusaha memberikan informasi menu, harga, dan ketersediaan produk seakurat mungkin. Namun, perubahan sewaktu-waktu dapat terjadi tanpa pemberitahuan sebelumnya.

        3. Privasi Pengguna
        Data pribadi yang Anda masukkan akan dijaga kerahasiaannya dan hanya digunakan untuk keperluan pemesanan dan layanan pelanggan.

        4. Pembayaran
        Transaksi pembayaran dilakukan sesuai dengan metode yang tersedia. Pastikan informasi pembayaran Anda benar dan sah.

        5. Tanggung Jawab
        Cafe Labiru tidak bertanggung jawab atas kesalahan pemesanan yang disebabkan oleh pengguna, seperti alamat pengantaran yang salah atau kesalahan input data.

        Dengan menggunakan aplikasi ini, Anda dianggap telah membaca dan menyetujui semua syarat dan ketentuan yang berlaku.
    """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage(termsText)
                .setPositiveButton("Saya Mengerti", null)
                .show()
        }


//        binding.termsLink.setOnClickListener {
//            AlertDialog.Builder(this)
//                .setTitle("Terms and Conditions")
//                .setMessage("Here are the terms and conditions...\n\n1. You agree not to misuse this app.\n2. We don't store personal data for unauthorized use.\n3. Use the app responsibly.")
//                .setPositiveButton("I Understand", null)
//                .show()
//        }

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount = task.result
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            val userId = firebaseUser?.uid
                            val name = firebaseUser?.displayName
                            val email = firebaseUser?.email

                            val userModel = UserModel(
                                userName = name,
                                email = email,
                                password = null // Google login tidak pakai password
                            )

                            userId?.let {
                                database.child("user").child(it).setValue(userModel)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Successfully signed in with Google",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        updateUi(firebaseUser)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Failed to save user data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Google Sign-in Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Google Sign-in Failed", Toast.LENGTH_SHORT).show()
            }
        }

    private fun updateUi(user: FirebaseUser?) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    saveUserData(firebaseUser.uid)
                }
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Account Creation Failed", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure", task.exception)
            }
        }
    }

    private fun saveUserData(userId: String) {
        val user = UserModel(
            roleProfile = "user", // Tambahkan ini
            userName = userName,
            password = password,
            email = email
        )
        database.child("user").child(userId).setValue(user)
            .addOnSuccessListener {
                Log.d("Firebase", "User data saved")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Failed to save user data", it)
            }
    }
}