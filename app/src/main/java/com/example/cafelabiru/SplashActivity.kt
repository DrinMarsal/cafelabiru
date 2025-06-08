package com.example.cafelabiru

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.admin.AdminActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                checkUserRoleAndNavigate(user)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 2000) // splash 2 detik
    }

    private fun checkUserRoleAndNavigate(user: FirebaseUser) {
        val userId = user.uid
        val usersRef = FirebaseDatabase.getInstance().getReference("user").child(userId)

        usersRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("roleProfile").getValue(String::class.java) ?: "user"
            if (role == "admin") {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal ambil data user", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}

