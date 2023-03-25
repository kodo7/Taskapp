package com.example.taskapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.databinding.ActivityChildBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChildActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildBinding
    private lateinit var taskListView: ListView
    private lateinit var taskListAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        val totalScoreTextView: TextView = binding.totalScoreTextview
        taskListView = binding.taskListview
        val completedTasksButton: Button = binding.completedTasksButton
        val redeemRewardsButton: Button = binding.redeemRewardsButton


        // Set the list of tasks in the ListView using the TaskListAdapter
        taskListAdapter = TaskListAdapter(this, "child.childId")
        taskListView.adapter = taskListAdapter

        // Set the text of the total score TextView
        totalScoreTextView.text = "Punktu skaits: 0"

        // Logout button
        binding.logout.setOnClickListener {
            Firebase.auth.signOut()

            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}