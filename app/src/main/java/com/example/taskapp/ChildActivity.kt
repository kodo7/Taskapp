package com.example.taskapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.databinding.ActivityChildBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.taskapp.LoginActivity.Companion.EXTRA_EMAIL
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChildActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildBinding
    private lateinit var taskListView: ListView
    private lateinit var taskListAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val childEmail = intent.getStringExtra(EXTRA_EMAIL)
        super.onCreate(savedInstanceState)

        binding = ActivityChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val childrenRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("children")
        val query = childrenRef.orderByChild("email").equalTo(childEmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val childId = snapshot.children.firstOrNull()?.key
                val childPoints = snapshot.children.firstOrNull()?.child("currentPoints")?.getValue(Int::class.java)
                if (childId != null) {
                    // Use the childId to add a task
                    taskListAdapter = TaskListAdapter(this@ChildActivity, childId,"incomplete")
                    taskListView.adapter = taskListAdapter
                    // Set the text of the total score TextView
                    val totalScoreTextView: TextView = binding.totalScoreTextview
                    totalScoreTextView.text = "Punkti: " + childPoints.toString()
                } else {
                    // Handle case where no child matches the email
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Initialize views
        taskListView = binding.taskListview
        val completedTasksButton: Button = binding.completedTasksButton
        val redeemRewardsButton: Button = binding.redeemRewardsButton


        // Logout button
        binding.logout.setOnClickListener {
            Firebase.auth.signOut()

            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}