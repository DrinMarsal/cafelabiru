package com.example.cafelabiru

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.admin.AdminActivity
import com.example.cafelabiru.databinding.ActivityLoginBinding
import com.example.cafelabiru.home.HomeFragment
import com.example.cafelabiru.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: DatabaseReference

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        auth = FirebaseAuth.getInstance()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.btnGoogle.setOnClickListener {
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)
        }

        binding.btnLogin.setOnClickListener {
            email = binding.edLoginEmail.text.toString().trim()
            password = binding.edLoginPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        binding.tvRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Keluar Aplikasi")
        builder.setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
        builder.setPositiveButton("Ya") { _, _ ->
            finishAffinity()
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }



    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateUi(user)
            } else {
                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("Login", "Login failed", task.exception)
            }
        }
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

                            // Simpan ke Realtime Database setelah login sukses
                            val userModel = UserModel(
                                userName = name,
                                email = email,
                                password = null // Google login tidak menggunakan password
                            )

                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("user")
                            usersRef.child(userId ?: "").setValue(userModel)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Successfully signed in with Google", Toast.LENGTH_SHORT).show()
                                    updateUi(firebaseUser)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
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


    override fun onStart() {
        super.onStart()
        // Tidak perlu cek currentUser lagi, sudah dicek di SplashActivity
    }


    private fun updateUi(user: FirebaseUser?) {
        val userId = user?.uid ?: return
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("user").child(userId)

        usersRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("roleProfile").getValue(String::class.java)

            if (role == "admin") {
                val intent = Intent(this, AdminActivity::class.java) // ganti sesuai nama Activity admin kamu
                startActivity(intent)
            } else {
                val intent = Intent(this, MainActivity::class.java) // user biasa
                startActivity(intent)
            }
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal ambil data user", Toast.LENGTH_SHORT).show()
        }
    }

}
