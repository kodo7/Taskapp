package com.example.taskapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import kotlin.math.roundToInt

class DepositActivity : AppCompatActivity() {
    private lateinit var depositAmountEditText: EditText
    private lateinit var depositStatusTextView: TextView
    private lateinit var depositStartDateTextView: TextView
    private lateinit var depositEndDateTextView: TextView
    private lateinit var backButton: Button
    private lateinit var depositButton: Button
    private lateinit var depositStatusLabel: TextView
    private lateinit var depositStartDateLabel: TextView
    private lateinit var depositEndDateLabel: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)

        // Initialize views
        depositAmountEditText = findViewById(R.id.depositAmountEditText)
        depositStatusTextView = findViewById(R.id.depositStatusTextView)
        depositStartDateTextView = findViewById(R.id.depositStartDateTextView)
        depositEndDateTextView = findViewById(R.id.depositEndDateTextView)
        backButton = findViewById(R.id.backButton)
        depositButton = findViewById(R.id.depositButton)
        depositStatusLabel = findViewById(R.id.depositStatusLabel)
        depositStartDateLabel = findViewById(R.id.depositStartDateLabel)
        depositEndDateLabel = findViewById(R.id.depositEndDateLabel)

        val depositAmountTextView = TextView(this)
        depositAmountTextView.layoutParams = depositAmountEditText.layoutParams
        depositAmountTextView.id = depositAmountEditText.id
        depositAmountTextView.textSize = 24F
        depositStatusLabel.visibility = View.GONE
        depositStartDateLabel.visibility = View.GONE
        depositEndDateLabel.visibility = View.GONE

        val child = intent.getParcelableExtra<Child>("child")
        val database = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Query the database to check if there is an active deposit for the child
        database.child("deposits")
            .orderByChild("childId")
            .equalTo(child?.childId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if there is an active deposit for the child
                    val activeDeposit = snapshot.children
                        .mapNotNull { it.getValue(Deposit::class.java) }
                        .firstOrNull { it.status == "active" }

                    if (activeDeposit != null) {
                        // Display deposit properties and disable take deposit button
                        depositAmountTextView.text = activeDeposit.amount.toString()
                        depositStatusTextView.text = "Aktīvs"
                        depositStartDateTextView.text = activeDeposit.startDate
                        depositEndDateTextView.text = activeDeposit.endDate
                        depositButton.isEnabled = false
                        depositStatusLabel.visibility = View.VISIBLE
                        depositStartDateLabel.visibility = View.VISIBLE
                        depositEndDateLabel.visibility = View.VISIBLE
                        val parent = depositAmountEditText.parent as ViewGroup
                        val index = parent.indexOfChild(depositAmountEditText)
                        parent.removeView(depositAmountEditText)
                        parent.addView(depositAmountTextView, index)
                    } else {
                        // Enable take deposit button
                        depositButton.isEnabled = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Display an error message
                    depositStatusTextView.text = "Error retrieving deposit information"
                }
            })

        // Set click listener for back button
        backButton.setOnClickListener {
            finish()
        }

        // Set click listener for take deposit button
        depositButton.setOnClickListener {
            // Get input from user
            val amount = depositAmountEditText.text.toString().toInt()
            val interestRate = 5

            // Retrieve current points of the child from Firebase Realtime Database
            val currentPoints = child?.currentPoints
            val depositMax = currentPoints!! * 0.3

            // Check if the deposit amount exceeds 10% of the current points
            if (amount > depositMax) {
                val message = "Ieguldījuma daudzums nedrīkst pārsniegt 30% no taviem punktiem (${depositMax.roundToInt()} punkti ir maksimālais daudzums)"
                AlertDialog.Builder(this)
                    .setTitle("Ieguldījuma kļūda")
                    .setMessage(message)
                    .setPositiveButton("Labi") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                // Create a new deposit object
                val deposit = Deposit(
                    depositId = database.push().key!!,
                    childId = child?.childId.toString(),
                    amount = amount,
                    interestRate = interestRate,
                    startDate = LocalDate.now().toString(),
                    endDate = LocalDate.now().plusWeeks(1).toString(),
                    status = "active"
                )

                // Show confirmation dialog before proceeding with the deposit
                val builder = AlertDialog.Builder(this@DepositActivity)
                builder.setTitle("Apstiprināt Ieguldījumu")
                val paybackAmount = amount*(interestRate.toDouble()/100+1)
                builder.setMessage("Vai esi pārliecināts, ka vēlies ieguldīt $amount punktus? Pēc nedeļas tas tiek atmaksāts ar $interestRate% likmi (${paybackAmount.toInt()} punkti)")

                // Add the buttons
                builder.setPositiveButton("Apstiprināt") { dialog, which ->
                    // Save deposit object to Firebase Realtime Database
                    database.child("deposits").push().setValue(deposit).addOnCompleteListener {
                        //Add points to the child
                        val newPoints = currentPoints - amount
                        database.child("children/${child?.childId}/currentPoints").setValue(newPoints).addOnCompleteListener {

                            // Display deposit properties and disable take deposit button
                            depositAmountTextView.text = amount.toString()
                            depositStatusTextView.text = "Aktīvs"
                            depositStartDateTextView.text = deposit.startDate
                            depositEndDateTextView.text = deposit.endDate
                            depositButton.isEnabled = false

                            // Replace the deposit amount EditText with a TextView
                            val parent = depositAmountEditText.parent as ViewGroup
                            val index = parent.indexOfChild(depositAmountEditText)
                            parent.removeView(depositAmountEditText)
                            parent.addView(depositAmountTextView, index)

                            depositStatusLabel.visibility = View.VISIBLE
                            depositStartDateLabel.visibility = View.VISIBLE
                            depositEndDateLabel.visibility = View.VISIBLE
                        }
                    }.addOnFailureListener {
                        // Display an error message
                        depositStatusTextView.text = "Error saving deposit information"
                    }
                }
                builder.setNegativeButton("Atcelt") { dialog, which ->
                    // Do nothing
                }

                // Display the dialog
                builder.show()
            }
        }
    }
}