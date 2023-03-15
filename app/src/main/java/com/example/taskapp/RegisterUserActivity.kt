package com.example.taskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterUserActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var banner: TextView
    private lateinit var registerUser: TextView
    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var radioParent: RadioGroup
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        // Write a message to the database
        val database = Firebase.database("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
        val myRef = database.getReference("message")

        //myRef.setValue("Hello, Worlds!")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        auth = FirebaseAuth.getInstance()

        banner = findViewById(R.id.banner) as TextView
        banner.setOnClickListener(this)

        registerUser = findViewById(R.id.registerUser) as Button
        registerUser.setOnClickListener(this)

        editTextFullName = findViewById(R.id.fullname) as EditText
        editTextEmail = findViewById(R.id.email) as EditText
        editTextPassword = findViewById(R.id.password) as EditText
        editTextConfirmPassword = findViewById(R.id.confirmPassword) as EditText

        radioParent = findViewById(R.id.radioGroup) as RadioGroup

        progressBar = findViewById(R.id.progressBar) as ProgressBar
    }

    override fun onClick(p0: View?) {
        if (p0 != null) {
            when (p0.id){
                R.id.banner -> startActivity(Intent(applicationContext, LoginActivity::class.java))
                R.id.registerUser -> RegisterUser()
            }
        }
    }

    fun RegisterUser(){
        var email:String = editTextEmail.text.toString().trim()
        var password:String = editTextPassword.text.toString().trim()
        var confirmPassword:String = editTextConfirmPassword.text.toString().trim()
        var fullName:String = editTextFullName.text.toString().trim()
        var parent:Boolean? = null

        var selectedRadioButtonId = radioParent.checkedRadioButtonId


        if (selectedRadioButtonId == -1) {
            // No radio button is selected
            // Show an error message to the user
            Toast.makeText(this,"Neveiksmīga reģistrācija!",Toast.LENGTH_LONG).show()
            return
        } else {
            // Find the selected radio button by the ID
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)

            // Get the text of the selected radio button
            val selectedRadioButtonText = selectedRadioButton.text.toString()

            if(selectedRadioButtonText.equals("Vecāks"))
            {
                parent = true
            }
            if(selectedRadioButtonText.equals("Bērns"))
            {
                parent = false
            }
        }
        if(fullName.isEmpty())
        {
            editTextFullName.setError("Nepieciešams pilns vārds!")
            editTextFullName.requestFocus()
            return
        }
        if(email.isEmpty())
        {
            editTextEmail.setError("Nepieciešama e-pasta adrese!")
            editTextEmail.requestFocus()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Ievadi pareizu e-pasta adresi!")
            editTextEmail.requestFocus()
            return
        }
        if(password.isEmpty())
        {
            editTextPassword.setError("Nepieciešama parole!")
            editTextPassword.requestFocus()
            return
        }
        if(password.length < 6){
            editTextPassword.setError("Paroles garums nedrīkst būt mazāks par 6 simboliem!")
            editTextPassword.requestFocus()
            return
        }
        if(password != confirmPassword){
            editTextConfirmPassword.setError("Paroles nesakrīt!")
            editTextConfirmPassword.requestFocus()
            return
        }


        progressBar.isVisible = true
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                var user: User = User(fullName,email,parent,null)
                Firebase.database("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(user).addOnCompleteListener { task2 ->
                        if(task2.isSuccessful){
                            Toast.makeText(this,"Lietotājs veiksmīgi reģistrēts!",Toast.LENGTH_LONG).show()
                            progressBar.isVisible = false
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                        }
                        else{
                            Toast.makeText(this,"Neveiksmīga reģistrācija!",Toast.LENGTH_LONG).show()
                            progressBar.isVisible = false
                        }
                    }
            }else{
                Toast.makeText(this,"Neveiksmīga reģistrācija!",Toast.LENGTH_LONG).show()
                progressBar.isVisible = false
            }
        }
    }
}