package com.example.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class IncompleteTasksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomplete_tasks)
    }
}