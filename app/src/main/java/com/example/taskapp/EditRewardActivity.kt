package com.example.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase

class EditRewardActivity : AppCompatActivity() {
    private lateinit var rewardId: String
    private lateinit var rewardNameEditText: EditText
    private lateinit var costEditText: EditText
    private lateinit var qtyEditText: EditText
    private lateinit var boughtQtyEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reward)

        // Get the rewardId from the previous view
        rewardId = intent.getStringExtra("rewardId")!!
        var childId: String? = null

        // Initialize the UI elements
        rewardNameEditText = findViewById(R.id.reward_name_edittext)
        costEditText = findViewById(R.id.cost_edittext)
        qtyEditText = findViewById(R.id.qty_edittext)
        boughtQtyEditText = findViewById(R.id.bought_qty_edittext)

        // Retrieve the existing reward data from Firebase Realtime Database
        val database = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
        val rewardsRef = database.getReference("rewards")
        val rewardRef = rewardsRef.child(rewardId)
        rewardRef.get().addOnSuccessListener { snapshot ->
            val reward = snapshot.getValue(Reward::class.java)
            if (reward != null) {
                // Fill in the existing reward data in the edit fields
                rewardNameEditText.setText(reward.rewardName)
                costEditText.setText(reward.cost.toString())
                qtyEditText.setText(reward.qty.toString())
                boughtQtyEditText.setText(reward.boughtQty.toString())
                childId = reward.childId
            }
        }

        // Set up the delete button
        val deleteButton: Button = findViewById(R.id.delete_button)
        deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Vai esi pārliecināts, ka vēlies dzēst šo balvu?")
                .setCancelable(false)
                .setPositiveButton("Jā") { dialog, id ->
                    rewardRef.removeValue()
                    finish()
                }
                .setNegativeButton("Nē") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        // Set up the save button
        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            val rewardName = rewardNameEditText.text.toString().trim()
            val costStr = costEditText.text.toString().trim()
            val qtyStr = qtyEditText.text.toString().trim()
            val boughtQtyStr = boughtQtyEditText.text.toString().trim()

            // Validate input fields
            if (rewardName.isEmpty()) {
                rewardNameEditText.error = "Nepieciešams balvas nosaukums"
                return@setOnClickListener
            }
            if (costStr.isEmpty()) {
                costEditText.error = "Maksa ir nepieciešama"
                return@setOnClickListener
            }
            if (qtyStr.isEmpty()) {
                qtyEditText.error = "Daudzums ir nepieciešams"
                return@setOnClickListener
            }
            if (boughtQtyStr.isEmpty()) {
                boughtQtyEditText.error = "Nopirktais daudzums ir nepieciešams"
                return@setOnClickListener
            }

            val cost = costStr.toIntOrNull()
            if (cost == null || cost <= 0) {
                costEditText.error = "Nepareiza maksa"
                return@setOnClickListener
            }

            val qty = qtyStr.toIntOrNull()
            if (qty == null || qty < 0) {
                qtyEditText.error = "Nepareizs daudzums"
                return@setOnClickListener
            }

            val boughtQty = boughtQtyStr.toIntOrNull()
            if (boughtQty == null || boughtQty < 0) {
                boughtQtyEditText.error = "Nepareizs nopirktais daudzums"
                return@setOnClickListener
            }

            val newReward = Reward(rewardId, childId, rewardName, cost, qty, boughtQty)
            rewardRef.setValue(newReward)
            finish()
        }

        // Set up the back button
        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}