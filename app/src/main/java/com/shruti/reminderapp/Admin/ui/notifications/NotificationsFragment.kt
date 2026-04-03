package com.shruti.reminderapp.Admin.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shruti.reminderapp.activity.LoginActivity
import com.shruti.reminderapp.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        loadCurrentUserDetails()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return binding.root
    }

    private fun loadCurrentUserDetails() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Path must match your HomeFragment: "Users" (Capital U)
        db.child("Users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 2. Lifecycle Check: Don't update if user closed the screen
                if (_binding == null || !isAdded) return

                if (snapshot.exists()) {
                    val name = snapshot.child("name").value?.toString() ?: "No Name"
                    val email = snapshot.child("email").value?.toString() ?: "No Email"

                    // 3. Double-check this key in Firebase: is it "number" or "phone"?
                    val phone = snapshot.child("number").value?.toString() ?: "No Number"

                    binding.tvAdminName.text = name
                    binding.tvAdminEmail.text = email
                    binding.tvAdminPhone.text = phone
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors (like permission denied)
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}