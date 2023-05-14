package com.example.taskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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
        val ratesButton = findViewById<Button>(R.id.ratesButton)

        ratesButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Bērna likmes")

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 0, 50, 0)

            val loanRateLabel = TextView(this)
            loanRateLabel.text = "Aizdevuma 1 nedēļas likme (%)"
            layout.addView(loanRateLabel)

            val loanRateEditText = EditText(this)
            loanRateEditText.inputType = InputType.TYPE_CLASS_NUMBER
            val loanRate = child?.loanRate ?: 0
            loanRateEditText.setText(loanRate.toString())
            layout.addView(loanRateEditText)

            val depositRateLabel = TextView(this)
            depositRateLabel.text = "Ieguldījuma 1 nedēļas likme (%)"
            layout.addView(depositRateLabel)

            val depositRateEditText = EditText(this)
            depositRateEditText.inputType = InputType.TYPE_CLASS_NUMBER
            val depositRate = child?.depositRate ?: 0
            depositRateEditText.setText(depositRate.toString())
            layout.addView(depositRateEditText)

            val maxLoanPercentageLabel = TextView(this)
            maxLoanPercentageLabel.text = "Maksimālais aizdevuma daudzums (%)"
            layout.addView(maxLoanPercentageLabel)

            val maxLoanPercentageEditText = EditText(this)
            maxLoanPercentageEditText.inputType = InputType.TYPE_CLASS_NUMBER
            val maxLoanPercentage = child?.maxLoanPercentage ?: 0
            maxLoanPercentageEditText.setText(maxLoanPercentage.toString())
            layout.addView(maxLoanPercentageEditText)

            val maxDepositPercentageLabel = TextView(this)
            maxDepositPercentageLabel.text = "Maksimālais ieguldījuma daudzums (%)"
            layout.addView(maxDepositPercentageLabel)

            val maxDepositPercentageEditText = EditText(this)
            maxDepositPercentageEditText.inputType = InputType.TYPE_CLASS_NUMBER
            val maxDepositPercentage = child?.maxDepositPercentage ?: 0
            maxDepositPercentageEditText.setText(maxDepositPercentage.toString())
            layout.addView(maxDepositPercentageEditText)

            builder.setView(layout)

            builder.setPositiveButton("Labi") { dialog, which ->
                val updatedLoanRate = loanRateEditText.text.toString().toIntOrNull()
                val updatedDepositRate = depositRateEditText.text.toString().toIntOrNull()
                val updatedMaxLoanPercentage = maxLoanPercentageEditText.text.toString().toIntOrNull()
                val updatedMaxDepositPercentage = maxDepositPercentageEditText.text.toString().toIntOrNull()

                if (updatedLoanRate in 0..100 &&
                    updatedDepositRate in 0..100 &&
                    updatedMaxLoanPercentage in 0..100 &&
                    updatedMaxDepositPercentage in 0..100) {

                    child?.loanRate = updatedLoanRate ?: 0
                    child?.depositRate = updatedDepositRate ?: 0
                    child?.maxLoanPercentage = updatedMaxLoanPercentage ?: 0
                    child?.maxDepositPercentage = updatedMaxDepositPercentage ?: 0

                    // Save the updated child object to the Realtime Database
                    val database = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").reference
                    val childRef = database.child("children").child(child?.childId ?: "")
                    childRef.setValue(child)

                    Toast.makeText(this, "Likmes atjauninātas!", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Nepareizas vērtības likmēm, mēģini vēlreiz!", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Atcelt") { dialog, which ->
                dialog.cancel()
            }

            builder.show()
        }
    }
}