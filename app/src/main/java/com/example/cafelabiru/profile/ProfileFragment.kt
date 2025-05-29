package com.example.cafelabiru.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cafelabiru.LoginActivity
import com.example.cafelabiru.MapsActivity
import com.example.cafelabiru.R
import com.example.cafelabiru.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var tvName: TextView
    private lateinit var etFullname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnEditName: ImageButton
    private lateinit var btnEditEmail: ImageButton
    private lateinit var btnEditAddress: ImageButton
    private lateinit var btnEditPhone: ImageButton

    private var currentUserKey: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Gunakan database reference yang sama seperti di HomeFragment
        database = FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app")
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        initViews(view)
        loadUserData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogout: Button = view.findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener {
            // Logout Firebase
            FirebaseAuth.getInstance().signOut()

            // Setelah logout, pindah ke activity login (ganti dengan activity kamu)
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnEditAddress.setOnClickListener {
            openMapsActivity()
        }

        btnEditName.setOnClickListener {
            etFullname.isEnabled = !etFullname.isEnabled
            if (!etFullname.isEnabled) {
                saveUserName(etFullname.text.toString())
            }
        }

        btnEditEmail.setOnClickListener {
            Toast.makeText(requireContext(), "Email tidak dapat diubah", Toast.LENGTH_SHORT).show()
            etEmail.isEnabled = false
        }

        btnEditPhone.setOnClickListener {
            etPhone.isEnabled = !etPhone.isEnabled
            if (!etPhone.isEnabled) {
                saveUserPhone(etPhone.text.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun initViews(view: View) {
        tvName = view.findViewById(R.id.tvName)
        etFullname = view.findViewById(R.id.etFullname)
        etEmail = view.findViewById(R.id.etEmail)
        etAddress = view.findViewById(R.id.etAddress)
        etPhone = view.findViewById(R.id.etPhone)
        btnEditName = view.findViewById(R.id.btnEditName)
        btnEditEmail = view.findViewById(R.id.btnEditEmail)
        btnEditAddress = view.findViewById(R.id.btnEditAddress)
        btnEditPhone = view.findViewById(R.id.btnEditPhone)
    }

    private fun openMapsActivity() {
        try {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            intent.putExtra("mode", "profile")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error opening MapsActivity: ${e.message}")
            Toast.makeText(requireContext(), "Error membuka peta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        // Gunakan pendekatan yang sama seperti HomeFragment
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            currentUserKey = userId // Set current user key
            loadUserDataByUserId(userId)
        } else {
            // Fallback ke method lama jika Firebase Auth tidak tersedia
            val userEmail = sharedPreferences.getString("email", null)
            if (userEmail != null) {
                loadUserDataByEmail(userEmail)
            } else {
                Toast.makeText(requireContext(), "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserDataByUserId(userId: String) {
        val userRef = database.getReference("user").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        updateUI(user)
                    } else {
                        // Jika UserModel null, coba ambil field individual
                        loadIndividualFields(snapshot)
                    }
                } else {
                    Toast.makeText(requireContext(), "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                    setDefaultValues()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
                Toast.makeText(requireContext(), "Gagal memuat data user", Toast.LENGTH_SHORT).show()
                setDefaultValues()
            }
        })
    }

    private fun loadIndividualFields(snapshot: DataSnapshot) {
        // Ambil field satu per satu jika UserModel tidak berhasil di-parse
        val userName = snapshot.child("userName").getValue(String::class.java)
        val email = snapshot.child("email").getValue(String::class.java)
        val location = snapshot.child("location").getValue(String::class.java)
        val phone = snapshot.child("phone").getValue(String::class.java)

        tvName.text = userName ?: "userName"
        etFullname.setText(userName ?: "userName")
        etEmail.setText(email ?: "email")
        etAddress.setText(location ?: "Lokasi belum diatur")
        etPhone.setText(phone ?: "phone")

        Log.d("ProfileFragment", "Loaded individual fields - Name: $userName, Email: $email")
    }

    private fun loadUserDataByEmail(email: String) {
        val userRef = database.getReference("user")
        val query = userRef.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        currentUserKey = userSnapshot.key
                        val user = userSnapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            updateUI(user)
                            break
                        } else {
                            loadIndividualFields(userSnapshot)
                            break
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                    setDefaultValues(email)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
                Toast.makeText(requireContext(), "Gagal memuat data user", Toast.LENGTH_SHORT).show()
                setDefaultValues(email)
            }
        })
    }

    private fun updateUI(user: UserModel) {
        tvName.text = user.userName ?: "User"
        etFullname.setText(user.userName ?: "")
        etEmail.setText(user.email ?: "")
        etAddress.setText(user.location ?: "Lokasi belum diatur")
        etPhone.setText(user.phone ?: "")

        Log.d("ProfileFragment", "Updated UI with user: ${user.userName}")
    }

    private fun setDefaultValues(email: String? = null) {
        tvName.text = "User"
        etFullname.setText("")
        etEmail.setText(email ?: "")
        etAddress.setText("Lokasi belum diatur")
        etPhone.setText("")
    }

    private fun saveUserName(name: String) {
        val key = currentUserKey ?: return
        val userRef = database.getReference("user").child(key)

        userRef.child("userName").setValue(name)
            .addOnSuccessListener {
                tvName.text = name
                Toast.makeText(requireContext(), "Nama berhasil diubah", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Failed to save name: ${exception.message}")
                Toast.makeText(requireContext(), "Gagal mengubah nama", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserPhone(phone: String) {
        val key = currentUserKey ?: return
        val userRef = database.getReference("user").child(key)

        userRef.child("phone").setValue(phone)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Nomor telepon berhasil diubah", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Failed to save phone: ${exception.message}")
                Toast.makeText(requireContext(), "Gagal mengubah nomor telepon", Toast.LENGTH_SHORT).show()
            }
    }
}