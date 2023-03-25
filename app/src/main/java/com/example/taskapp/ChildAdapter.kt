package com.example.taskapp

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChildAdapter(private val context: Activity, private val databaseReference: DatabaseReference, private val parentID: String) : BaseAdapter() {

    private val childList = ArrayList<Child>()

    init {
        // Add listener for child added to database
        databaseReference.orderByChild("userId").equalTo(parentID)
            .addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val child = snapshot.getValue(Child::class.java)
                if (child != null) {
                    childList.add(child)
                    notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val child = snapshot.getValue(Child::class.java)
                if (child != null) {
                    for (i in childList.indices) {
                        if (childList[i].childId == child.childId) {
                            childList[i] = child
                            notifyDataSetChanged()
                            break
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val child = snapshot.getValue(Child::class.java)
                if (child != null) {
                    childList.remove(child)
                    notifyDataSetChanged()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getCount(): Int {
        return childList.size
    }

    override fun getItem(position: Int): Child {
        return childList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if (view == null) {
            val layoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(R.layout.child_list_item, parent, false)
            view.setOnClickListener {
                val child = getItem(position)
                val intent = Intent(context, ParentChildActivity::class.java)
                intent.putExtra("child", child)
                context.startActivity(intent)
            }
        }

        val nameTextView = view!!.findViewById<TextView>(R.id.nameTextView)
        val pointsTextView = view.findViewById<TextView>(R.id.pointsTextView)
        val emailTextView = view.findViewById<TextView>(R.id.emailTextView)

        val child = getItem(position)

        if (nameTextView != null) {
            nameTextView.text = child.name
        }

        pointsTextView.text = "Punkti: " + child.currentPoints.toString()
        emailTextView.text = child.email

        return view
    }
}