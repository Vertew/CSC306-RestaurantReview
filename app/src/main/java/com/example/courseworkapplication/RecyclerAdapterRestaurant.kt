package com.example.courseworkapplication

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

// This recycler adapter is used for displaying the list of reviews for each restaurant in the restaurant activity
class RecyclerAdapterRestaurant(private val reviews: ArrayList<Reviews>) :
    RecyclerView.Adapter<RecyclerAdapterRestaurant.ReviewHolder>() {

    private lateinit var database: DatabaseReference

    class ReviewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val reviewTitle: TextView = v.findViewById(R.id.reviewTitle)
        val userName: TextView = v.findViewById(R.id.userName)
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar3)
        val reviewText: TextView = v.findViewById(R.id.reviewText)
        val taggedImage: ImageView = v.findViewById(R.id.taggedImageView2)
        val locationText: TextView = v.findViewById(R.id.taggedLocationText)
        val likeButton: ImageView = v.findViewById(R.id.likeButton)
        val likeCountView: TextView = v.findViewById(R.id.likeCount)
        val view: View = v
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row_restaurant, false)
        return ReviewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        // Assigning each view to it's respective data value
        holder.reviewTitle.text = reviews[position].reviewTitle
        holder.userName.text = reviews[position].user
        holder.ratingBar.rating = reviews[position].rating!!
        holder.reviewText.text = reviews[position].reviewText
        holder.locationText.text = reviews[position].location
        val identifier: String = reviews[position].identifier.toString()
        var likeCount = reviews[position].likeCount
        holder.likeCountView.text = likeCount.toString()

        //Connecting to the database to update the like count value when the like button is pressed
        database =
            Firebase.database("https://coursework-application-e5ea8-default-rtdb.europe-west1.firebasedatabase.app").reference
        holder.likeButton.setOnClickListener {
            likeCount = likeCount?.plus(1)
            val likeUpdate: HashMap<String, Any> = HashMap()
            likeUpdate.put("likeCount", likeCount!!)
            database.child("Reviews").child(identifier).updateChildren(likeUpdate)

            //Updating the local value for visual confirmation
            holder.likeCountView.text = likeCount.toString()
        }

        // If an image Url is found in the review, it is loaded with Picasso
        val imageUri = reviews[position].imgUrl
        if (imageUri != null) {
            Picasso.get().load(imageUri).into(holder.taggedImage)
        } else {
            holder.taggedImage.setImageDrawable(null)
            Log.d("RecyclerAdapterRestaurant", "No image found")
        }
    }

    override fun getItemCount() = reviews.size
}