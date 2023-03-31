package com.example.taskapp

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import android.view.View
import android.widget.*
import androidx.core.view.isVisible

class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var banner: TextView
    private lateinit var resetEmailInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        resetEmailInput = findViewById(R.id.email)
        val resetButton = findViewById<Button>(R.id.forgotPassword)

        banner = findViewById(R.id.banner) as TextView
        banner.setOnClickListener(this)

        resetButton.setOnClickListener {
            val email = resetEmailInput.text.toString().trim()
            if (email.isEmpty()) {
                resetEmailInput.error = "Nepieciešams e-pasts"
                resetEmailInput.requestFocus()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Paroles atjaunošanas e-pasts izsūtīts", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Kļūda", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

    override fun onClick(p0: View?) {
        if (p0 != null) {
            when (p0.id){
                R.id.banner -> startActivity(Intent(applicationContext, LoginActivity::class.java))
            }
        }
    }
}