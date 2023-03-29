package com.example.taskapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*

class TaskListAdapter(private val context: Context, private val childId: String?, private val view: String, private val parentView: Boolean) : BaseAdapter() {

    private val databaseRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("tasks").orderByChild("childId").equalTo(childId)

    private val taskList = ArrayList<Task>()

    private class ViewHolder(row: View?) {
        var taskNameTextView: TextView? = null
        var taskPointsTextView: TextView? = null
        var taskDescriptionTextView: TextView? = null
        var markCompleteButton: Button? = null

        init {
            this.taskNameTextView = row?.findViewById(R.id.task_name_textview)
            this.taskPointsTextView = row?.findViewById(R.id.task_points_textview)
            this.taskDescriptionTextView = row?.findViewById(R.id.task_description_textview)
            this.markCompleteButton = row?.findViewById(R.id.mark_complete_button)
        }
    }

    init {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskList.clear()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    when(view) {
                        "incomplete" -> {
                            if (task?.taskComplete == false) {
                                taskList.add(task)
                            }
                        }
                        "completed" -> {
                            if (task?.taskComplete == true && task?.verified == false) {
                                taskList.add(task)
                            }
                        }
                        "verified" -> {
                            if (task?.verified == true) {
                                taskList.add(task)
                            }
                        }
                        "all" -> {
                            taskList.add(task!!)
                        }
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: Handle database error
            }
        })
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return taskList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_item_task, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val task = getItem(position) as Task

        if(parentView)
        {
            view.setOnClickListener {
                val intent = Intent(context, EditTaskActivity::class.java)
                intent.putExtra("taskId", task.taskId)
                context.startActivity(intent)
            }
        }

        viewHolder.taskNameTextView?.text = task.taskName
        viewHolder.taskPointsTextView?.text = "${task.points} punkti"
        viewHolder.taskDescriptionTextView?.text = task.taskDescription
        viewHolder.markCompleteButton?.apply {
            if(task.verified) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = if (task.taskComplete) "Apstiprināt" else "Atzīmēt kā pabeigtu"
                setOnClickListener {
                    val taskRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("tasks").child(task.taskId)
                    val childRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("children").child(task.childId.toString())
                    if(!task.taskComplete){
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Pabeigt uzdevumu")
                        builder.setMessage("Vai esi pārliecināts, ka vēlies atzīmēt šo uzdevumu kā pabeigtu?")
                        builder.setPositiveButton("Apstiprināt") { _, _ ->
                            taskRef.child("taskComplete").setValue(!task.taskComplete)
                        }
                        builder.setNegativeButton("Atcelt", null)
                        builder.show()
                    }
                    if(task.taskComplete)
                    {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Verificēt uzdevumu")
                        builder.setMessage("Vai bērns ir pabeidzis uzdevumu?")
                        builder.setPositiveButton("Apstiprināt") { _, _ ->
                            taskRef.child("verified").setValue(!task.verified)
                            childRef.child("currentPoints")
                                .runTransaction(object : Transaction.Handler {
                                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                        val currentPoints = mutableData.getValue(Int::class.java) ?: 0
                                        mutableData.value = currentPoints + task.points!!
                                        return Transaction.success(mutableData)
                                    }

                                    override fun onComplete(
                                        databaseError: DatabaseError?,
                                        committed: Boolean,
                                        dataSnapshot: DataSnapshot?
                                    ) {
                                        if (databaseError != null) {
                                            // TODO: Handle the error
                                        } else {
                                            // The value has been successfully added
                                        }
                                    }
                                })
                        }
                        builder.setNegativeButton("Atcelt", null)
                        builder.show()
                    }
                }
            }
        }

        return view
    }
}