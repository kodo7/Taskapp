package com.example.taskapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.taskapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var register: TextView
    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginBtn: Button
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Check if the user is already authenticated
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(auth.currentUser)
        }

        //Configure Google Signin
        gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("410247991388-1h98f5o81gjn76s88rp4v8vim8h3tr4s.apps.googleusercontent.com")
            .requestEmail()
            .build()
        gsc=GoogleSignIn.getClient(this,gso)


        binding.google.setOnClickListener{
                signInGoogle()
        }
        auth = FirebaseAuth.getInstance()
        register = findViewById<TextView>(R.id.register)
        register.setOnClickListener(this)

        loginBtn = findViewById(R.id.login)
        loginBtn.setOnClickListener(this)

        forgotPassword = findViewById(R.id.forgotpassword)
        forgotPassword.setOnClickListener(this)
    }

    fun signInGoogle(){
        val intent = gsc.signInIntent
        startActivityForResult(intent,RC_SIGN_IN)
    }
    private fun signIn() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
            }
    }

    companion object{
        const val RC_SIGN_IN = 1001
        const val EXTRA_EMAIL = "EXTRA EMAIL"
    }
    override fun onClick(v:View)
    {
        when(v.id){
            R.id.register -> startActivity(Intent(this, RegisterUserActivity::class.java))
            R.id.login -> {
                signIn()
            }
            R.id.forgotpassword -> startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    var dialog: AlertDialog? = null
                    var userRole : Boolean? = null
                    var userNew: User
                    //if new google sign in, create user in realtime database
                    if(task.result.additionalUserInfo?.isNewUser == true)
                    {
                        // Create a new dialog builder
                        val builder = AlertDialog.Builder(this)

                        // Set the dialog title
                        builder.setTitle("Reģistrēties kā vecāks vai bērns")

                        // Create a list of roles to display in the dialog
                        val roles = arrayOf("Vecāks", "Bērns")

                        // Set the default selected role
                        var selectedRole = "Vecāks"

                        // Set the single choice items for the dialog
                        builder.setSingleChoiceItems(roles, 0) { dialog, which ->
                            // Update the selected role when the user selects an option
                            selectedRole = roles[which]
                        }

                        // Set the positive button for the dialog
                        builder.setPositiveButton("OK") { dialog, which ->
                            if(selectedRole == "Vecāks")
                            {
                                userRole = true
                            }
                            if(selectedRole == "Bērns")
                            {
                                userRole = false
                            }
                            if(userRole != null)
                            {
                                userNew = User(user?.displayName,user?.email, userRole!!)
                                Firebase.database("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .setValue(userNew)
                                updateUI(user)
                            }
                            else
                            {
                                Firebase.auth.signOut()
                                dialog.dismiss()
                                updateUI(null)
                            }
                        }

                            // Set the negative button for the dialog
                        builder.setNegativeButton("Atcelt") { dialog, which ->
                            // Dismiss the dialog if the user cancels
                            Firebase.auth.signOut()
                            dialog.dismiss()
                            updateUI(null)
                        }

                        // Show the dialog
                        dialog = builder.create()
                        dialog.show()
                    }
                    else {
                        updateUI(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun updateUI(user: FirebaseUser?) {
        Firebase.database("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("parent")
            .get()
            .addOnSuccessListener { dataSnapshot ->
            val role = dataSnapshot.value as Boolean
                if (user != null && !role) {
                    val intent = Intent(applicationContext, ChildActivity::class.java)
                    intent.putExtra(EXTRA_EMAIL, user.email)
                    startActivity(intent)
                }
                if (user != null && role)
                {
                    val intent = Intent(applicationContext, ParentActivity::class.java)
                    intent.putExtra(EXTRA_EMAIL, user.email)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
            }

    }
    /*fun RegisterActivity(){
        finish();
        val intent = Intent(applicationContext,GoogleRegisterActivity::class.java)
        startActivity(intent)
    }*/
}