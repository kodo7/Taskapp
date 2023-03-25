package com.example.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskListAdapter(private val context: Context, private val childId: String) : BaseAdapter() {

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
                    if (task != null) {
                        taskList.add(task)
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

        viewHolder.taskNameTextView?.text = task.taskName
        viewHolder.taskPointsTextView?.text = "${task.points} punkti"
        viewHolder.taskDescriptionTextView?.text = task.taskDescription
        viewHolder.markCompleteButton?.apply {
            text = if (task.taskComplete) "Task Completed" else "Mark Task as Complete"
            setOnClickListener {
                // TODO: handle button click
            }
        }

        return view
    }
}