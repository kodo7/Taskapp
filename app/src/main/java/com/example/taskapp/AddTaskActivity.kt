package com.example.taskapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddTaskActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var pointsInput: EditText
    private lateinit var addChildButton: Button
    private lateinit var backButton: Button

    private val database = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("tasks")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        pointsInput = findViewById(R.id.pointsInput)
        addChildButton = findViewById(R.id.addButton)
        backButton = findViewById(R.id.backButton)

        addChildButton.setOnClickListener {
            addTask()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun addTask() {
        // Get the child object from the intent
        val child = intent.getParcelableExtra<Child>("child")
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val points = pointsInput.text.toString().toIntOrNull()

        if (title.isEmpty() || description.isEmpty() || points == null) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val taskId = database.push().key
        val task = Task(taskId.toString(), title, description, points, false, child?.childId)
        database.child(task.taskId).setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add task: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}