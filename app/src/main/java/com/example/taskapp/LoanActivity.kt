package com.example.taskapp

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

class LoanActivity : AppCompatActivity() {

    private lateinit var loanAmountEditText: EditText
    private lateinit var loanStatusTextView: TextView
    private lateinit var loanStartDateTextView: TextView
    private lateinit var loanEndDateTextView: TextView
    private lateinit var backButton: Button
    private lateinit var takeLoanButton: Button

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

        val child = intent.getParcelableExtra<Child>("child")
        val database = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Set click listener for back button
        backButton.setOnClickListener {
            finish()
        }

        // Set click listener for take loan button
        takeLoanButton.setOnClickListener {
            // Get input from user
            val amount = loanAmountEditText.text.toString().toInt()

            // Retrieve current points of the child from Firebase Realtime Database
                    val currentPoints = child?.currentPoints

                    // Check if the loan amount exceeds 10% of the current points
                    if (amount > currentPoints!! * 0.1) {
                        // Display an error message
                        loanStatusTextView.text = "Loan amount cannot exceed 10% of current points"
                    } else {
                        // Create a new loan object
                        val loan = Loan(
                            borrowId = database.push().key!!,
                            childId = child?.childId.toString(),
                            amount = amount,
                            interestRate = 2,
                            startDate = LocalDate.now().toString(),
                            endDate = LocalDate.now().plusWeeks(1).toString(),
                            status = "active"
                        )

                        // Save loan object to Firebase Realtime Database
                        database.child("loans").push().setValue(loan)

                        // Display loan status message
                        loanStatusTextView.text = "Loan request successful"
                    }

        }
    }
}