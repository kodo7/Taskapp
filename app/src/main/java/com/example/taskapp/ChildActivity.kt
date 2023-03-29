package com.example.taskapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.taskapp.databinding.ActivityChildBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.taskapp.LoginActivity.Companion.EXTRA_EMAIL
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate

class ChildActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildBinding
    private lateinit var taskListView: ListView
    private lateinit var taskListAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val childEmail = intent.getStringExtra(EXTRA_EMAIL)
        super.onCreate(savedInstanceState)

        binding = ActivityChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val childrenRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("children")
        val query = childrenRef.orderByChild("email").equalTo(childEmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val child = snapshot.children.firstOrNull()?.getValue(Child::class.java)
                if (child?.childId != null) {
                    // Use the childId to add a task
                    taskListAdapter = TaskListAdapter(this@ChildActivity, child.childId,"incomplete", false)
                    taskListView.adapter = taskListAdapter
                    val childRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("children").child(child.childId!!)
                    childRef.child("currentPoints").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val childPoints = snapshot.getValue(Int::class.java)
                            // Set the text of the total score TextView
                            val totalScoreTextView: TextView = binding.totalScoreTextview
                            totalScoreTextView.text = "Punkti: " + childPoints.toString()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })

                    val loansQuery = database.child("loans")
                        .orderByChild("childId")
                        .equalTo(child?.childId)

                    loansQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val activeLoanSnapshot = snapshot.children.firstOrNull {
                                it.child("status").getValue(String::class.java) == "active"
                            }
                            val activeLoanId = activeLoanSnapshot?.key
                            val activeLoan = activeLoanSnapshot?.getValue(Loan::class.java)
                            if (activeLoan != null && LocalDate.now().toString() >= activeLoan.endDate) {
                                // update the loan status to "inactive"
                                activeLoan.status = "inactive"
                                activeLoanSnapshot?.ref?.setValue(activeLoan)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })

                    binding.completedTasksButton.setOnClickListener{
                        val intent = Intent(this@ChildActivity, VerifiedTasksActivity::class.java)
                        intent.putExtra("child", child)
                        intent.putExtra("viewChild", "viewChild")
                        startActivity(intent)
                    }
                    binding.redeemRewardsButton.setOnClickListener{
                        val intent = Intent(this@ChildActivity, RewardsChildActivity::class.java)
                        intent.putExtra("child", child)
                        startActivity(intent)
                    }
                    binding.borrowPointsButton.setOnClickListener{
                        val intent = Intent(this@ChildActivity, LoanActivity::class.java)
                        intent.putExtra("child", child)
                        startActivity(intent)
                    }
                    binding.depositPointsButton.setOnClickListener{
                        val intent = Intent(this@ChildActivity, DepositActivity::class.java)
                        intent.putExtra("child", child)
                        startActivity(intent)
                    }

                } else {
                    Toast.makeText(this@ChildActivity,"Vecākam ir nepieciešams pievienot tevi savam kontam",
                        Toast.LENGTH_SHORT).show()
                    Firebase.auth.signOut()
                    finish()
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