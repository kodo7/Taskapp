package com.example.taskapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class RewardsParentActivity : AppCompatActivity() {

    private lateinit var rewardsListView: ListView
    private lateinit var rewardsAdapter: RewardsAdapter
    private lateinit var rewardsRef: DatabaseReference
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards_parent)

        // Get the child ID from the intent
        val child = intent.getParcelableExtra<Child>("child")

        // Set up the ListView and adapter
        rewardsListView = findViewById(R.id.rewards_list)
        rewardsAdapter = RewardsAdapter(this, child?.childId, mutableListOf(),"parent")
        rewardsListView.adapter = rewardsAdapter

        // Set up the database reference to the "rewards" node
        rewardsRef = FirebaseDatabase.getInstance().reference.child("rewards")

        // Add a listener to populate the adapter with rewards
        rewardsRef.orderByChild("childId").equalTo(child?.childId).addValueEventListener(object : ValueEventListener {
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

        // Set up the button to add a reward
        val addRewardButton: Button = findViewById(R.id.add_reward_button)
        addRewardButton.setOnClickListener {
            // Start the AddRewardActivity
            val intent = Intent(this, AddRewardActivity::class.java)
            intent.putExtra("child", child)
            startActivity(intent)
        }

        // Set up the back button
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

    }

}