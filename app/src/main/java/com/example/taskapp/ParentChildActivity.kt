package com.example.taskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase

class ParentChildActivity : AppCompatActivity() {

    private lateinit var addTaskButton: Button
    private lateinit var backButton: Button
    private lateinit var removeChildButton: Button
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var pointsTextView: TextView
    private lateinit var tasksListView: ListView

    private lateinit var tasksAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_child)

        // Get the child object from the intent
        val child = intent.getParcelableExtra<Child>("child")
        addTaskButton = findViewById(R.id.addTaskButton)
        backButton = findViewById(R.id.backButton)
        removeChildButton = findViewById(R.id.removeChildButton)
        nameTextView = findViewById(R.id.childNameTextView)
        emailTextView = findViewById(R.id.childEmailTextView)
        pointsTextView= findViewById(R.id.childPointsTextView)
        tasksListView=findViewById(R.id.tasksListView)

        // Set the list of tasks in the ListView using the TaskListAdapter
        if (child != null) {
            tasksAdapter = child.childId?.let { TaskListAdapter(this, it) }!!
        }
        tasksListView.adapter = tasksAdapter

        // Set the child's name, email, and points
        if (child != null) {
            nameTextView.text = child.name
        }
        if (child != null) {
            emailTextView.text = child.email
        }
        if (child != null) {
            pointsTextView.text = "Punkti: " + child.currentPoints.toString()
        }

        // Set up click listeners for the buttons
        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra("child", child)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }

        removeChildButton.setOnClickListener {
            // Show a confirmation dialog to remove the child from the parent's list
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Apstiprināt dzēšanu")
            builder.setMessage("Vai esi pārliecināts, ka vēlies noņemt šo bērnu?")
            builder.setPositiveButton("Apstiprināt") { _, _ ->
                val childRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("children").child(child?.childId!!)
                childRef.removeValue()
                finish()
            }
            builder.setNegativeButton("Atcelt", null)
            builder.show()
        }
    }
}