package com.example.taskapp

import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

class EditTaskActivity : AppCompatActivity() {
    private lateinit var taskId: String
    private lateinit var taskNameEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private lateinit var pointsEditText: EditText
    private lateinit var taskCompleteSwitch: Switch
    private lateinit var taskVerifiedSwitch: Switch

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        // Get the taskId from the previous activity
        taskId = intent.getStringExtra("taskId")!!
        var childId: String? = null
        var startDate = ""
        var endDate = ""
        var taskComplete = false

        // Initialize the UI elements
        taskNameEditText = findViewById(R.id.task_name_edittext)
        taskDescriptionEditText = findViewById(R.id.task_description_edittext)
        pointsEditText = findViewById(R.id.points_edittext)
        taskCompleteSwitch = findViewById(R.id.task_complete_switch)
        taskVerifiedSwitch = findViewById(R.id.task_verified_switch)
        val deleteButton = findViewById<Button>(R.id.delete_button)
        val saveButton = findViewById<Button>(R.id.save_button)
        val backButton = findViewById<Button>(R.id.back_button)

        // Load the task data from Firebase Realtime Database
        val taskRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/").getReference("tasks").child(taskId)
        taskRef.get().addOnSuccessListener {
            val task = it.getValue(Task::class.java)
            if (task != null) {
                taskNameEditText.setText(task.taskName)
                taskDescriptionEditText.setText(task.taskDescription)
                pointsEditText.setText(task.points.toString())
                taskCompleteSwitch.isChecked = task.taskComplete
                taskVerifiedSwitch.isChecked = task.verified
                childId = task.childId
                startDate = task.startDate
                endDate = task.endDate
                taskComplete = task.taskComplete
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load task data", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up the delete button
        deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Vai esi pārliecināts, ka vēlies izdzēst uzdevumu?")
                .setCancelable(false)
                .setPositiveButton("Jā") { dialog, id ->
                    taskRef.removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Uzdevums izdzēts", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Neizdevās dzēst uzdevumu", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Nē") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        // Set up the save button
        saveButton.setOnClickListener {
            val name = taskNameEditText.text.toString().trim()
            val description = taskDescriptionEditText.text.toString().trim()
            val pointsStr = pointsEditText.text.toString().trim()
            val points = if (pointsStr.isNotEmpty()) pointsStr.toIntOrNull() ?: 0 else 0
            val complete = taskCompleteSwitch.isChecked
            val verified = taskVerifiedSwitch.isChecked

            var isValid = true

            if (name.isEmpty()) {
                taskNameEditText.error = "Uzdevuma nosaukums ir nepieciešams"
                taskNameEditText.requestFocus()
                isValid = false
            }

            if (description.isEmpty()) {
                taskDescriptionEditText.error = "Uzdevuma apraksts ir nepieciešams"
                taskDescriptionEditText.requestFocus()
                isValid = false
            }

            if (pointsStr.isNotEmpty() && points <= 0) {
                pointsEditText.error = "Punktu skaitam jābūt pozitīvam"
                pointsEditText.requestFocus()
                isValid = false
            } else if (pointsStr.isEmpty()) {
                pointsEditText.error = "Punktu skaits ir nepieciešams"
                pointsEditText.requestFocus()
                isValid = false
            }


            if (isValid) {
                val task: Task
                if(taskComplete && !complete){
                    task = Task(
                        taskId,
                        name,
                        description,
                        points,
                        complete,
                        childId,
                        verified,
                        startDate,
                        "Nepabeigts"
                    )
                }
                else if(!taskComplete && complete){
                    task = Task(
                        taskId,
                        name,
                        description,
                        points,
                        complete,
                        childId,
                        verified,
                        startDate,
                        LocalDate.now().toString()
                    )
                }
                else {
                    task = Task(
                        taskId,
                        name,
                        description,
                        points,
                        complete,
                        childId,
                        verified,
                        startDate,
                        endDate
                    )
                }
                taskRef.setValue(task).addOnSuccessListener {
                    Toast.makeText(this, "Uzdevums saglabāts", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Neizdevās saglabāt uzdevumu", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up the back button
        backButton.setOnClickListener {
            finish()
        }
    }
}