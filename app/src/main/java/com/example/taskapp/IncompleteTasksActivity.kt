package com.example.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView

class IncompleteTasksActivity : AppCompatActivity() {

    private lateinit var tasksListView: ListView
    private lateinit var backButton: Button
    private lateinit var tasksAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomplete_tasks)

        tasksListView=findViewById(R.id.taskListView)
        backButton = findViewById(R.id.backButton)
        val child = intent.getParcelableExtra<Child>("child")

        tasksAdapter = child?.childId?.let { TaskListAdapter(this, it, "incomplete", true) }!!
        tasksListView.adapter = tasksAdapter

        backButton.setOnClickListener {
            finish()
        }
    }
}