package com.example.taskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
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
    private lateinit var addRewardButton: Button
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
        addRewardButton = findViewById(R.id.addRewardButton)

        // Set the list of tasks in the ListView using the TaskListAdapter
        tasksAdapter = child.childId?.let { TaskListAdapter(this, it, "completed", true) }!!
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
            // Show a dialog to input the child email
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Apstiprināt dzēšanu")
            builder.setMessage("Vai esi pārliecināts, ka vēlies noņemt šo bērnu? Ievadi bērna e-pastu, lai apstiprinātu")
            val input = EditText(this)
            input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            builder.setView(input)

            // Set up the confirmation button
            builder.setPositiveButton("Apstiprināt") { _, _ ->
                val email = input.text.toString().trim()
                val childRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("children").child(child.childId!!)
                childRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val child = dataSnapshot.getValue(Child::class.java)
                        if (child != null && child.email == email) {
                            childRef.removeValue()
                            finish()
                        } else {
                            Toast.makeText(applicationContext, "Nepareizs e-pasts", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Getting Child failed, log a message
                        Log.w("Fail", "loadPost:onCancelled", databaseError.toException())
                    }
                })
            }
            builder.setNegativeButton("Atcelt", null)
            builder.show()
        }
        incompleteTasksButton.setOnClickListener {
            val intent = Intent(this, IncompleteTasksActivity::class.java)
            intent.putExtra("child", child)
            startActivity(intent)
        }
        completedTasksButton.setOnClickListener {
            val intent = Intent(this, VerifiedTasksActivity::class.java)
            intent.putExtra("child", child)
            startActivity(intent)
        }
        addRewardButton.setOnClickListener{
            val intent = Intent(this, RewardsParentActivity::class.java)
            intent.putExtra("child", child)
            startActivity(intent)
        }
    }
}