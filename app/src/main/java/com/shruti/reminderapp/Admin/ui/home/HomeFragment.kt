package com.shruti.reminderapp.Admin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shruti.reminderapp.activity.User
import com.shruti.reminderapp.adapter.UserAdapter
import com.shruti.reminderapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // Use the safe call '?.' inside listeners instead of the '!!' from 'binding'
    private val binding get() = _binding

    private lateinit var userAdapter: UserAdapter
    private var userList = ArrayList<User>()
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Store listener in a variable so we can remove it to prevent memory leaks/crashes
    private var usersListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        // Ensure "Users" matches exactly with your Firebase Console node name
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        setupRecyclerView()
        fetchUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(userList)
        _binding?.rvUsers?.layoutManager = LinearLayoutManager(requireContext())
        _binding?.rvUsers?.adapter = userAdapter
    }

    private fun fetchUsers() {
        val currentUserId = auth.currentUser?.uid
        _binding?.progressBarHome?.visibility = View.VISIBLE

        usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // CRITICAL SAFETY CHECK: If fragment is gone, stop executing
                if (_binding == null || !isAdded) return

                userList.clear()
                for (postSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java)

                    if (user != null && user.id != currentUserId) {
                        userList.add(user)
                    }
                }

                userAdapter.notifyDataSetChanged()
                _binding?.progressBarHome?.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Use safe calls here too
                _binding?.progressBarHome?.visibility = View.GONE
                if (isAdded) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Attach the listener
        dbRef.addValueEventListener(usersListener!!)
    }

    override fun onDestroyView() {
        // Stop the listener from firing once the view is gone
        usersListener?.let {
            dbRef.removeEventListener(it)
        }
        super.onDestroyView()
        _binding = null
    }
}