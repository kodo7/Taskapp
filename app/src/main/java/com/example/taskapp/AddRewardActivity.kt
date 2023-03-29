package com.example.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddRewardActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var pointsInput: EditText
    private lateinit var qtyInput: EditText
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reward)

        titleInput = findViewById(R.id.titleInput)
        pointsInput = findViewById(R.id.pointsInput)
        qtyInput = findViewById(R.id.qtyInput)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        val addButton: Button = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val points = pointsInput.text.toString().trim().toIntOrNull()
            val qty = qtyInput.text.toString().trim().toIntOrNull()
            val child = intent.getParcelableExtra<Child>("child")

            if (title.isEmpty() || points == null || qty == null) {
                Toast.makeText(this, "Lūdzu, aizpildiet visus laukus", Toast.LENGTH_SHORT).show()
            } else {
                val rewardId = UUID.randomUUID().toString()
                val reward = Reward(rewardId, child?.childId, title, points, qty, 0)
                val ref = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("rewards")
                ref.child(rewardId).setValue(reward).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Atalgojums pievienots veiksmīgi", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Neizdevās pievienot atalgojumu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}