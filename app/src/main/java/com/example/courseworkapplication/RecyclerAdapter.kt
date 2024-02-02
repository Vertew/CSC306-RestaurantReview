package com.example.courseworkapplication

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

// This adapter is used in the recycler view that displays search results when the user searches for a restaurant
class RecyclerAdapter(private val names: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerAdapter.Holder>() {


    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val textView: TextView
        val parentLayout: ConstraintLayout
        val view: View = v

        init {
            textView = v.findViewById(R.id.itemName)
            parentLayout = v.findViewById(R.id.parent_layout)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return Holder(inflatedView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.textView.text = names[position]

        /* An on click listener is placed on each element of the recycler view so that the user is
        * taken to the restaurant page of whichever restaurant they select. */
        holder.parentLayout.setOnClickListener {
            val intent = Intent(holder.view.context, RestaurantPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("name", names[position])
            holder.view.context.startActivity(intent)
        }
    }

    override fun getItemCount() = names.size
}