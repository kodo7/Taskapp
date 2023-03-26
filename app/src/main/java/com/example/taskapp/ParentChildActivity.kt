package com.example.taskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*

class ParentChildActivity : AppCompatActivity() {

    private lateinit var addTaskButton: Button
    private lateinit var backButton: Button
    private lateinit var removeChildButton: Button
    private lateinit var incompleteTasksButton: Button
    private lateinit var completedTasksButton: Button
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var pointsTextView: TextView
    private lateinit var tasksListView: ListView
    private lateinit var childRef: DatabaseReference

    private lateinit var tasksAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_child)

        val child = intent.getParcelableExtra<Child>("child")
        childRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("children").child(child?.childId!!)

        // Get the child object from the intent

        addTaskButton = findViewById(R.id.addTaskButton)
        backButton = findViewById(R.id.backButton)
        removeChildButton = findViewById(R.id.removeChildButton)
        nameTextView = findViewById(R.id.childNameTextView)
        emailTextView = findViewById(R.id.childEmailTextView)
        pointsTextView= findViewById(R.id.childPointsTextView)
        tasksListView=findViewById(R.id.tasksListView)
        incompleteTasksButton = findViewById(R.id.incompleteTasksButton)
        completedTasksButton = findViewById(R.id.completedTasksButton)

        // Set the list of tasks in the ListView using the TaskListAdapter
        tasksAdapter = child.childId?.let { TaskListAdapter(this, it, "completed") }!!
        tasksListView.adapter = tasksAdapter

        // Set the child's name, email, and points
        nameTextView.text = child.name
        emailTextView.text = child.email
        childRef.child("currentPoints").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val points = snapshot.value as? Long
                    if (points != null) pointsTextView.text = "Punkti: $points"
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle errors
                }
        })


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
                val childRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("children").child(child.childId!!)
                childRef.removeValue()
                finish()
            }
            builder.setNegativeButton("Atcelt", null)
            builder.show()
        }
        incompleteTasksButton.setOnClickListener {
            finish()
        }
        completedTasksButton.setOnClickListener {
            finish()
        }
    }
}