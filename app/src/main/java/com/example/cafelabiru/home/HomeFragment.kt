package com.example.cafelabiru.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.cafelabiru.AddMenuActivity
import com.example.cafelabiru.BookingActivity
import com.example.cafelabiru.DeliveryActivity
import com.example.cafelabiru.DineInActivity
import com.example.cafelabiru.MenuActivity
import com.example.cafelabiru.R
import com.example.cafelabiru.TakeawayActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ganti dengan nama layout kamu, misal fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName: TextView = view.findViewById(R.id.tv_name)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Ubah status bar lewat activity (karena fragment nggak punya window sendiri)
        activity?.window?.statusBarColor = android.graphics.Color.WHITE
        val windowInsetsController =
            activity?.window?.let { WindowInsetsControllerCompat(it, view) }
        windowInsetsController?.isAppearanceLightStatusBars = true

        // Pakai 'view.findViewById' untuk ambil elemen UI
        val imageCard1 = view.findViewById<ImageView>(R.id.imageCard1)
        imageCard1.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            startActivity(intent)
        }

        val imageCard2 = view.findViewById<ImageView>(R.id.imageCard2)
        imageCard2.setOnClickListener {
            val intent = Intent(requireContext(), AddMenuActivity::class.java)
            startActivity(intent)
        }

        val deliverySection = view.findViewById<ConstraintLayout>(R.id.deliverySection)
        deliverySection.setOnClickListener {
            val intent = Intent(requireContext(), DeliveryActivity::class.java)
            startActivity(intent)
        }

        val takeawaySection = view.findViewById<ConstraintLayout>(R.id.takeawaySection)
        takeawaySection.setOnClickListener {
            val intent = Intent(requireContext(), TakeawayActivity::class.java)
            startActivity(intent)
        }

        val dineInSection = view.findViewById<ConstraintLayout>(R.id.dineInSection)
        dineInSection.setOnClickListener {
            val intent = Intent(requireContext(), DineInActivity::class.java)
            startActivity(intent)
        }

        val bookingSection = view.findViewById<ConstraintLayout>(R.id.bookingSection)
        bookingSection.setOnClickListener {
            val intent = Intent(requireContext(), BookingActivity::class.java)
            startActivity(intent)
        }

        // Inset padding untuk sistem bar
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load nama user dari Firebase
        if (userId != null) {
            val database =
                FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app")
            val userRef = database.getReference("user").child(userId)

            userRef.child("userName").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java)
                    tvName.text = name ?: "No name"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load name", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}