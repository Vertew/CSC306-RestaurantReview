package com.example.courseworkapplication

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

// This activity displays the results of a users' search
class RestaurantPage : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()
    private val user = mAuth.currentUser
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapterRestaurant
    private lateinit var database: DatabaseReference
    val reviews = ArrayList<Reviews>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_page)

        val restaurantReviewsView = findViewById<RecyclerView>(R.id.restaurantReviewsView)
        linearLayoutManager = LinearLayoutManager(this)
        restaurantReviewsView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapterRestaurant(reviews)
        restaurantReviewsView.adapter = adapter

        val nameTextView = findViewById<TextView>(R.id.nameTextView)

        val mToolbar = findViewById<View>(R.id.toolbar5) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab!!.title = getString(R.string.restaurantPageString)
        ab.setDisplayHomeAsUpEnabled(false)

        val reviewButton = findViewById<Button>(R.id.reviewButton)

        val mySnackbar = make(reviewButton, "Register an account to leave a review", LENGTH_SHORT)

        // This button takes the user to the review activity where they can write a review
        reviewButton.setOnClickListener {
            if (user?.email != null) {
                val intent = Intent(this, ReviewActivity::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("restaurantName", nameTextView.text.toString())
                startActivity(intent)
                finish()
            } else {
                mySnackbar.show()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        val name = findViewById<TextView>(R.id.nameTextView).text.toString()
        val desc = findViewById<TextView>(R.id.descTextView).text.toString()
        val location = findViewById<TextView>(R.id.locationTextView).text.toString()
        val style = findViewById<TextView>(R.id.styleTextView).text.toString()
        /* Here I use shared preferences to store some data locally so it can be used again
        to cut down on database queries */
        val sharedPreferences = getSharedPreferences("MyUserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("desc", desc)
        editor.putString("style", style)
        editor.putString("location", location)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()

        database =
            Firebase.database("https://coursework-application-e5ea8-default-rtdb.europe-west1.firebasedatabase.app").reference
        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        val descTextView = findViewById<TextView>(R.id.descTextView)
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        val styleTextView = findViewById<TextView>(R.id.styleTextView)

        /* Using shared preferences to restore values in the case that the intent is null
        which it will be every time the user cancels writing a review via the back button */

        val sharedPreferences = getSharedPreferences("MyUserPrefs", MODE_PRIVATE)
        if (intent.getStringExtra("name") == null) {
            nameTextView.text = sharedPreferences.getString("name", "default")
            descTextView.text = sharedPreferences.getString("desc", "default")
            locationTextView.text = sharedPreferences.getString("location", "default")
            styleTextView.text = sharedPreferences.getString("style", "default")
        } else {

            // Before values have been locally stored, they must be searched in the database.
            nameTextView.text = intent.getStringExtra("name")
            val searchQuery = database.child("Restaurants").orderByChild("name")
                .equalTo(nameTextView.text.toString())

            searchQuery.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)
                    val restaurant: Restaurants = dataSnapshot.getValue(Restaurants::class.java)!!
                    descTextView.text = restaurant.description
                    locationTextView.text = restaurant.location
                    styleTextView.text = restaurant.style
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
        // Querying database to find relevant reviews for this restaurant
        val searchQuery = database.child("Reviews").orderByChild("restaurant")
            .equalTo(nameTextView.text.toString())

        searchQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)
                val review: Reviews = dataSnapshot.getValue(Reviews::class.java)!!
                reviews.add(review)
                adapter.notifyItemInserted(reviews.size - 1)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.logoutButton -> {
                if (user?.email == null) {
                    FirebaseAuth.getInstance().currentUser?.delete()
                    startActivity(Intent(this, Register::class.java))
                    finish()
                } else {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }
            }
        }
        when (item.itemId) {
            R.id.backButton -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}