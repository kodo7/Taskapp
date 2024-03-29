package com.example.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView

class VerifiedTasksActivity : AppCompatActivity() {
    private lateinit var tasksListView: ListView
    private lateinit var backButton: Button
    private lateinit var tasksAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verified_tasks)

        tasksListView=findViewById(R.id.taskListView)
        backButton = findViewById(R.id.backButton)
        val child = intent.getParcelableExtra<Child>("child")
        val viewChild = intent.getStringExtra("viewChild")
        var parentView = false

        if(viewChild == null)
        {
            parentView = true
        }

        tasksAdapter = child?.childId?.let { TaskListAdapter(this, it, "verified", parentView) }!!
        tasksListView.adapter = tasksAdapter

        backButton.setOnClickListener {
            finish()
        }
    }
}