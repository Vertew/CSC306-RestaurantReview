package com.example.courseworkapplication

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

// This adapter is used for the MainActivity recycler view which displays reviews by the current user
class RecyclerAdapterUser(private val reviews: ArrayList<Reviews>) :
    RecyclerView.Adapter<RecyclerAdapterUser.ReviewHolder>() {

    class ReviewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val reviewTitle: TextView = v.findViewById(R.id.reviewTitle)
        val restaurantName: TextView = v.findViewById(R.id.userName)
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar3)
        val reviewText: TextView = v.findViewById(R.id.reviewText)
        val taggedImage: ImageView = v.findViewById(R.id.taggedImageView2)
        val locationText: TextView = v.findViewById(R.id.taggedLocationText)
        val likeCountView: TextView = v.findViewById(R.id.likeCount)
        val edit: TextView = v.findViewById(R.id.editButton)
        val view: View = v

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row_restaurant, false)
        return ReviewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        holder.reviewTitle.text = reviews[position].reviewTitle
        holder.restaurantName.text = reviews[position].restaurant
        holder.ratingBar.rating = reviews[position].rating!!
        holder.reviewText.text = reviews[position].reviewText
        holder.locationText.text = reviews[position].location
        holder.edit.text = holder.view.context.getString(R.string.edit)
        val likeCount = reviews[position].likeCount
        holder.likeCountView.text = likeCount.toString()

        val imageUri = reviews[position].imgUrl
        if (imageUri != null) {
            Picasso.get().load(imageUri).into(holder.taggedImage)
        } else {
            holder.taggedImage.setImageDrawable(null)
            Log.d("RecyclerAdapterRestaurant", "No image found")
        }
        /* The user can edit their own reviews from the homepage, as a result an edit button is
        * placed in this recycler view which sends the user over to the review activity along with
        * all of the information from the review they're editing. */
        holder.edit.setOnClickListener {
            val intent = Intent(holder.view.context, ReviewActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("reviewTitle", reviews[position].reviewTitle)
            intent.putExtra("restaurantNameEdit", reviews[position].restaurant)
            intent.putExtra("ratingBar", reviews[position].rating!!)
            intent.putExtra("reviewText", reviews[position].reviewText)
            intent.putExtra("locationText", reviews[position].location)
            intent.putExtra("image", reviews[position].imgUrl)
            intent.putExtra("identifier", reviews[position].identifier)
            intent.putExtra("likeCount", reviews[position].likeCount)
            intent.putExtra("fromEdit", true)
            holder.view.context.startActivity(intent)
        }
    }

    override fun getItemCount() = reviews.size
}
