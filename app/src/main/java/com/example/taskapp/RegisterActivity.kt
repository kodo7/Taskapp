package com.example.taskapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class RegisterActivity : AppCompatActivity() {
    lateinit var name: TextView
    lateinit var mail: TextView
    lateinit var gso: GoogleSignInOptions
    lateinit var gsc: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        name=findViewById(R.id.name)
        mail=findViewById(R.id.mail)

        gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        gsc= GoogleSignIn.getClient(this,gso)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account!=null){
            val Name=account.displayName
            val Mail=account.email

            name.setText(Name)
            mail.setText(Mail)
        }
    }
}