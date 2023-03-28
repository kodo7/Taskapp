package com.example.taskapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class RewardsChildActivity : AppCompatActivity() {
    private lateinit var rewardsListView: ListView
    private lateinit var rewardsAdapter: RewardsAdapter
    private lateinit var rewardsRef: DatabaseReference
    private lateinit var backButton: Button
    private lateinit var totalScore: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards_child)

        // Get the child ID from the intent
        val child = intent.getParcelableExtra<Child>("child")

        // Set up the ListView and adapter
        rewardsListView = findViewById(R.id.rewards_list)
        rewardsAdapter = RewardsAdapter(this, child?.childId, mutableListOf(),"all")
        rewardsListView.adapter = rewardsAdapter
        backButton = findViewById(R.id.backButton)
        totalScore = findViewById(R.id.total_score_textview)

        //totalScore.text = "Punkti: " + childPoints.toString()

        backButton.setOnClickListener {
            finish()
        }

        // Set up the database reference to the "rewards" node
        rewardsRef = FirebaseDatabase.getInstance().reference.child("rewards")

        // Add a listener to populate the adapter with rewards
        rewardsRef.orderByChild("childId").equalTo(child?.childId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rewardsList = mutableListOf<Reward>()
                for (rewardSnapshot in snapshot.children) {
                    val reward = rewardSnapshot.getValue(Reward::class.java)
                    reward?.let { rewardsList.add(it) }
                }
                rewardsAdapter.updateRewards(rewardsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Add a listener to update the total score TextView when the child's points change
        val childRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("children").child(
            child?.childId ?: ""
        )

        childRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val childPoints = snapshot.child("currentPoints").getValue(Int::class.java)
                totalScore.text = "Punkti: " + childPoints.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}