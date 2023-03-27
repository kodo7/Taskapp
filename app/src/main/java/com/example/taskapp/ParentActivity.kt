package com.example.taskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.*
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseUser


class ParentActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var childListView: ListView
    private lateinit var emailInput: EditText
    private lateinit var addChildButton: Button
    private lateinit var addChildPopup: LinearLayout
    private lateinit var addChildFormButton: Button
    private lateinit var cancelButton: Button
    private lateinit var currentUser: FirebaseUser
    private lateinit var userId: String

    private lateinit var childAdapter: ChildAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUser = FirebaseAuth.getInstance().currentUser!!
        userId = currentUser.uid

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent)

        logoutButton = findViewById(R.id.logout)
        childListView = findViewById(R.id.childList)
        addChildButton = findViewById(R.id.addChildButton)
        emailInput = findViewById(R.id.emailInput)
        addChildFormButton = findViewById(R.id.addChildFormButton)
        cancelButton = findViewById(R.id.cancelButton)
        addChildPopup = findViewById(R.id.addChildPopup)

        // Initialize Firebase database reference
        databaseReference =
            FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("children")

        // Initialize child adapter
        childAdapter = ChildAdapter(this, databaseReference, userId)

        // Set child adapter to list view
        childListView.adapter = childAdapter

        addChildFormButton.setOnClickListener {
            addChild()
        }

        addChildButton.setOnClickListener {
            addChildPopup.visibility = View.VISIBLE
        }

        cancelButton.setOnClickListener {
            addChildPopup.visibility = View.GONE
        }

        logoutButton.setOnClickListener {
            logout()
        }
       /* childListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // This code will be executed when an item in the list is clicked
            val child = childAdapter.getItem(position)
            Log.d("test", "Item clicked: $child")
            // You can also perform other actions here, such as starting a new activity or updating the UI
        }*/

    }

    private fun addChild() {
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            emailInput.error = "Nepieciešams e-pasts"
            return
        }

        if (currentUser == null) {
            // User is not authenticated, redirect to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Check if child already exists with provided email
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Child with provided email already exists
                        Toast.makeText(
                            this@ParentActivity,
                            "Bērns jau ir pievienots kādam vecākam",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Child does not exist, check if the email exists in the Users table and isParent is false
                        val userRef =
                            FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference("Users")
                        userRef.orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    if (userSnapshot.exists()) {
                                        val user =
                                            userSnapshot.children.first().getValue(User::class.java)
                                        if (user != null && user.parent == false) {
                                            // Email exists in Users table and isParent is false, add new child to database
                                            val name = user.fullName
                                            val childId = databaseReference.push().key
                                            val child = Child(
                                                childId,
                                                userId,
                                                email,
                                                0,
                                                name
                                            )
                                            if (childId != null) {
                                                databaseReference.child(childId).setValue(child)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            this@ParentActivity,
                                                            "Bērns pievienots veiksmīgi",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        addChildPopup.visibility = View.GONE
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(
                                                            this@ParentActivity,
                                                            "Neizdevās pievienot bērnu",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        } else {
                                            // Email exists in Users table but isParent is true
                                            Toast.makeText(
                                                this@ParentActivity,
                                                "E-pasts ir reģistrēts kā vecāks",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        // Email does not exist in Users table
                                        Toast.makeText(
                                            this@ParentActivity,
                                            "E-pasts nav atrasts",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(
                                        this@ParentActivity,
                                        "Error: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ParentActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }



    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}