package com.example.cafelabiru.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.cafelabiru.BookingActivity
import com.example.cafelabiru.DetailActivity
import com.example.cafelabiru.DineInActivity
import com.example.cafelabiru.MenuActivity
import com.example.cafelabiru.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var scrollView: HorizontalScrollView
    private val handler = Handler(Looper.getMainLooper())
    private var scrollDirection = 1
    private val scrollStep = 1
    private val delay: Long = 10
    private var isUserScrolling = false
    private val resumeDelay: Long = 3000

    private val runnable = object : Runnable {
        override fun run() {
            if (!isUserScrolling) {
                scrollView.scrollBy(scrollStep * scrollDirection, 0)

                val maxScroll = scrollView.getChildAt(0).measuredWidth - scrollView.width
                val currentScroll = scrollView.scrollX

                if (currentScroll >= maxScroll) {
                    scrollDirection = -1
                } else if (currentScroll <= 0) {
                    scrollDirection = 1
                }

                handler.postDelayed(this, delay)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Ganti dengan nama layout kamu, misal fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName: TextView = view.findViewById(R.id.tv_name)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        scrollView = view.findViewById(R.id.recommended_scroll)

        // Deteksi jika user scroll manual → pause auto-scroll
        scrollView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    isUserScrolling = true
                    handler.removeCallbacks(runnable)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.postDelayed({
                        isUserScrolling = false
                        handler.post(runnable)
                    }, resumeDelay)
                }
            }
            false
        }
        handler.post(runnable)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Keluar Aplikasi")
                        .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                        .setPositiveButton("Ya") { _, _ ->
                            requireActivity().finishAffinity()
                        }
                        .setNegativeButton("Tidak") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        )



        // Ubah status bar lewat activity (karena fragment nggak punya window sendiri)
        activity?.window?.statusBarColor = android.graphics.Color.WHITE
        val windowInsetsController =
            activity?.window?.let { WindowInsetsControllerCompat(it, view) }
        windowInsetsController?.isAppearanceLightStatusBars = true

        // Pakai 'view.findViewById' untuk ambil elemen UI

        val recom2 = view.findViewById<LinearLayout>(R.id.recom2)
        recom2.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("menuIdFilter", "M026") // kirim data menuId
            startActivity(intent)
        }

        val recom3 = view.findViewById<LinearLayout>(R.id.recom3)
        recom3.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("menuIdFilter", "M036") // kirim data menuId
            startActivity(intent)
        }

        val recom4 = view.findViewById<LinearLayout>(R.id.recom4)
        recom4.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("menuIdFilter", "M027") // kirim data menuId
            startActivity(intent)
        }

        val recom5 = view.findViewById<LinearLayout>(R.id.recom5)
        recom5.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("menuIdFilter", "M022") // kirim data menuId
            startActivity(intent)
        }

        val recom6 = view.findViewById<LinearLayout>(R.id.recom6)
        recom6.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("menuIdFilter", "M029") // kirim data menuId
            startActivity(intent)
        }

        val recom1 = view.findViewById<LinearLayout>(R.id.recom1)
        recom1.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("menuIdFilter", "M024") // kirim data menuId
            startActivity(intent)
        }

        val imageCard1 = view.findViewById<ImageView>(R.id.imageCard1)
        imageCard1.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Local")
            startActivity(intent)
        }

        val imageCard2 = view.findViewById<ImageView>(R.id.imageCard2)
        imageCard2.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Bowl")
            startActivity(intent)
        }

        val imageCard3 = view.findViewById<ImageView>(R.id.imageCard3)
        imageCard3.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Pizza")
            startActivity(intent)
        }

        val imageCard4 = view.findViewById<ImageView>(R.id.imageCard4)
        imageCard4.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Snack")
            startActivity(intent)
        }

        val imageCard5 = view.findViewById<ImageView>(R.id.imageCard5)
        imageCard5.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Pasta")
            startActivity(intent)
        }

        val imageCard6 = view.findViewById<ImageView>(R.id.imageCard6)
        imageCard6.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Dessert")
            startActivity(intent)
        }

        val imageCard7 = view.findViewById<ImageView>(R.id.imageCard7)
        imageCard7.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Steaks")
            startActivity(intent)
        }

        val imageCard8 = view.findViewById<ImageView>(R.id.imageCard8)
        imageCard8.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            intent.putExtra("categoryFilter", "Drinks")
            startActivity(intent)
        }

        val deliverySection = view.findViewById<ConstraintLayout>(R.id.deliverySection)
        deliverySection.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
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
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }
}