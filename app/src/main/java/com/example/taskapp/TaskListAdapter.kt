package com.example.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class TaskListAdapter(private val context: Context, private val taskList: ArrayList<Task>) : BaseAdapter() {

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

        viewHolder.taskNameTextView?.text = task.taskName as CharSequence?
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

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return taskList.size
    }
}