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

class LoanActivity : AppCompatActivity() {

    private lateinit var loanAmountEditText: EditText
    private lateinit var loanStatusTextView: TextView
    private lateinit var loanStartDateTextView: TextView
    private lateinit var loanEndDateTextView: TextView
    private lateinit var backButton: Button
    private lateinit var takeLoanButton: Button
    private lateinit var loanStatusLabel: TextView
    private lateinit var loanStartDateLabel: TextView
    private lateinit var loanEndDateLabel: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        // Initialize views
        loanAmountEditText = findViewById(R.id.loanAmountEditText)
        loanStatusTextView = findViewById(R.id.loanStatusTextView)
        loanStartDateTextView = findViewById(R.id.loanStartDateTextView)
        loanEndDateTextView = findViewById(R.id.loanEndDateTextView)
        backButton = findViewById(R.id.backButton)
        takeLoanButton = findViewById(R.id.takeLoanButton)
        loanStatusLabel = findViewById(R.id.loanStatusLabel)
        loanStartDateLabel = findViewById(R.id.loanStartDateLabel)
        loanEndDateLabel = findViewById(R.id.loanEndDateLabel)

        val loanAmountTextView = TextView(this)
        loanAmountTextView.layoutParams = loanAmountEditText.layoutParams
        loanAmountTextView.id = loanAmountEditText.id
        loanAmountTextView.textSize = 24F
        loanStatusLabel.visibility = View.GONE
        loanStartDateLabel.visibility = View.GONE
        loanEndDateLabel.visibility = View.GONE

        val child = intent.getParcelableExtra<Child>("child")
        val database = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Query the database to check if there is an active loan for the child
        database.child("loans")
            .orderByChild("childId")
            .equalTo(child?.childId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if there is an active loan for the child
                    val activeLoan = snapshot.children
                        .mapNotNull { it.getValue(Loan::class.java) }
                        .firstOrNull { it.status == "active" }

                    if (activeLoan != null) {
                        // Display loan properties and disable take loan button
                        loanAmountTextView.text = activeLoan.amount.toString()
                        loanStatusTextView.text = "Aktīvs"
                        loanStartDateTextView.text = activeLoan.startDate
                        loanEndDateTextView.text = activeLoan.endDate
                        takeLoanButton.isEnabled = false
                        loanStatusLabel.visibility = View.VISIBLE
                        loanStartDateLabel.visibility = View.VISIBLE
                        loanEndDateLabel.visibility = View.VISIBLE
                        val parent = loanAmountEditText.parent as ViewGroup
                        val index = parent.indexOfChild(loanAmountEditText)
                        parent.removeView(loanAmountEditText)
                        parent.addView(loanAmountTextView, index)
                    } else {
                        // Enable take loan button
                        takeLoanButton.isEnabled = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Display an error message
                    loanStatusTextView.text = "Error retrieving loan information"
                }
            })

        // Set click listener for back button
        backButton.setOnClickListener {
            finish()
        }

        // Set click listener for take loan button
        takeLoanButton.setOnClickListener {
            // Get input from user
            val amount = loanAmountEditText.text.toString().toInt()
            val interestRate = 10

            // Retrieve current points of the child from Firebase Realtime Database
            val currentPoints = child?.currentPoints
            val loanMax = currentPoints!! * 0.3

            // Check if the loan amount exceeds 10% of the current points
            if (amount > loanMax) {
                val message = "Aizņēmuma daudzums nedrīkst pārsniegt 30% no taviem punktiem (${loanMax.roundToInt()} punkti ir maksimālais daudzums)"
                AlertDialog.Builder(this)
                    .setTitle("Aizņēmuma kļūda")
                    .setMessage(message)
                    .setPositiveButton("Labi") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                // Create a new loan object
                val loan = Loan(
                    borrowId = database.push().key!!,
                    childId = child?.childId.toString(),
                    amount = amount,
                    interestRate = interestRate,
                    startDate = LocalDate.now().toString(),
                    endDate = LocalDate.now().plusWeeks(1).toString(),
                    status = "active"
                )

                // Show confirmation dialog before proceeding with the loan
                val builder = AlertDialog.Builder(this@LoanActivity)
                builder.setTitle("Apstiprināt aizņēmumu")
                val paybackAmount = amount*(interestRate.toDouble()/100+1)
                builder.setMessage("Vai esi pārliecināts, ka vēlies aizņemties $amount punktus? Pēc nedeļas tas tiek atmaksāts ar $interestRate% likmi (${paybackAmount.toInt()} punkti)")

                // Add the buttons
                builder.setPositiveButton("Apstiprināt") { dialog, which ->
                    // Save loan object to Firebase Realtime Database
                    database.child("loans").push().setValue(loan).addOnCompleteListener {
                        //Add points to the child
                        val newPoints = currentPoints + amount
                        database.child("children/${child?.childId}/currentPoints").setValue(newPoints).addOnCompleteListener {

                            // Display loan properties and disable take loan button
                            loanAmountTextView.text = amount.toString()
                            loanStatusTextView.text = "Aktīvs"
                            loanStartDateTextView.text = loan.startDate
                            loanEndDateTextView.text = loan.endDate
                            takeLoanButton.isEnabled = false

                            // Replace the loan amount EditText with a TextView
                            val parent = loanAmountEditText.parent as ViewGroup
                            val index = parent.indexOfChild(loanAmountEditText)
                            parent.removeView(loanAmountEditText)
                            parent.addView(loanAmountTextView, index)

                            loanStatusLabel.visibility = View.VISIBLE
                            loanStartDateLabel.visibility = View.VISIBLE
                            loanEndDateLabel.visibility = View.VISIBLE
                        }
                    }.addOnFailureListener {
                        // Display an error message
                        loanStatusTextView.text = "Error saving loan information"
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