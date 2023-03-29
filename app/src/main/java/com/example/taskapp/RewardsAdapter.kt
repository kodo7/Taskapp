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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*

class RewardsAdapter(private val context: Context, private val childId: String?, private var rewardsList: MutableList<Reward>, private val selectedView: String) : BaseAdapter() {
    private val databaseRef = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("rewards").orderByChild("childId").equalTo(childId)
    private val rewardsRef: DatabaseReference = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("rewards")
    private val childRef: DatabaseReference = FirebaseDatabase.getInstance("https://taskapp-b088b-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("children").child(childId!!)
    private var childPoints = 0

    init {
        // Retrieve the child's current points from the database
        childRef.child("currentPoints").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the child's current points from the dataSnapshot
                val points = dataSnapshot.getValue(Int::class.java)

                // Update the childPoints variable with the retrieved value
                if (points != null) {
                    childPoints = points
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if necessary
            }
        })

        // Listen for changes to the rewards list in the database
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                rewardsList.clear()
                for (rewardSnapshot in dataSnapshot.children) {
                    val reward = rewardSnapshot.getValue(Reward::class.java)
                    when(selectedView) {
                        "bought" -> {
                            if (reward?.boughtQty!! > 0) {
                                rewardsList.add(reward)
                            }
                        }
                        "all" -> {
                            rewardsList.add(reward!!)
                        }
                        "parent" -> {
                            rewardsList.add(reward!!)
                        }
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Fail", "Failed to read value.", error.toException())
            }
        })
    }

    override fun getCount(): Int {
        return rewardsList.size
    }

    override fun getItem(position: Int): Any {
        return rewardsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            // Inflate the layout for each item of the ListView
            view = LayoutInflater.from(context).inflate(R.layout.reward_list_item, parent, false)

            // Set up the ViewHolder
            holder = ViewHolder()
            holder.rewardNameTextView = view.findViewById(R.id.reward_name)
            holder.rewardCostTextView = view.findViewById(R.id.reward_cost)
            holder.rewardQuantityTextView = view.findViewById(R.id.reward_quantity)
            holder.redeemButton = view.findViewById(R.id.redeem_button)
            holder.boughtQuantityTextView = view.findViewById(R.id.bought_quantity)

            if(selectedView == "parent") {
                view.setOnClickListener {
                    val reward = getItem(position) as Reward
                    val intent = Intent(context, EditRewardActivity::class.java)
                    intent.putExtra("rewardId", reward.rewardId)
                    context.startActivity(intent)
                }
            }

            // Retrieve the child's current points from the database
            childRef.child("currentPoints").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get the child's current points from the dataSnapshot
                    val points = dataSnapshot.getValue(Int::class.java)

                    // Update the childPoints variable with the retrieved value
                    if (points != null) {
                        childPoints = points
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error if necessary
                }
            })

            // Set up the button to redeem a reward
            holder.redeemButton.setOnClickListener {
                // Get the selected reward from the list
                val selectedReward = rewardsList[position]

                // Calculate the remaining points for the child after buying the reward
                val remainingPoints = childPoints - selectedReward.cost

                // Check if the child has enough points to buy the reward
                if (remainingPoints >= 0) {
                    // Create a confirmation dialog
                    val alertDialogBuilder = AlertDialog.Builder(context)
                    alertDialogBuilder.setMessage("Vai esi pārliecināts, ka vēlies pirkt šo balvu?")
                        .setCancelable(false)
                        .setPositiveButton("Jā") { _, _ ->
                            // Update the quantity of the selected reward and the child's points
                            val rewardRef = rewardsRef.child(selectedReward.rewardId.toString())
                            if (selectedReward.qty > 0) {
                                rewardRef.child("qty").setValue(selectedReward.qty - 1)
                                rewardRef.child("boughtQty").setValue(selectedReward.boughtQty + 1)
                                childRef.child("currentPoints").setValue(remainingPoints)

                                // Update the rewardsList and the adapter
                                rewardsList[position] = selectedReward.copy(qty = selectedReward.qty - 1)
                                rewardsList[position] = selectedReward.copy(boughtQty = selectedReward.boughtQty + 1)
                                notifyDataSetChanged()
                            }
                            else{
                                Toast.makeText(context, "Balva nav pieejama", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Atcelt") { dialog, _ -> dialog.cancel() }

                    // Show the confirmation dialog
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else {
                    // Show an error message if the child doesn't have enough points
                    Toast.makeText(context, "Tev nepietiek punkti, lai pirktu šo balvu", Toast.LENGTH_SHORT).show()
                }

            }

            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        // Bind the data to the ViewHolder
        val reward = rewardsList[position]
        holder.rewardNameTextView.text = reward.rewardName
        holder.rewardCostTextView.text = "Cena: ${reward.cost}"
        holder.rewardQuantityTextView.text = "Daudzums: ${reward.qty}"
        holder.boughtQuantityTextView.text = "Nopirktais daudzums: ${reward.boughtQty}"

        // Hide the redeem button if the view is "parent"
        if (selectedView == "parent" || reward.qty <= 0) {
            holder.redeemButton.visibility = View.GONE
        } else {
            holder.redeemButton.visibility = View.VISIBLE
        }

        return view!!
    }

    fun updateRewards(newRewardsList: MutableList<Reward>) {
        rewardsList = newRewardsList
        notifyDataSetChanged()
    }

    private class ViewHolder {
        lateinit var rewardNameTextView: TextView
        lateinit var rewardCostTextView: TextView
        lateinit var rewardQuantityTextView: TextView
        lateinit var redeemButton: Button
        lateinit var boughtQuantityTextView: TextView
    }
}