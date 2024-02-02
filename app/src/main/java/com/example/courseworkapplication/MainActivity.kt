package com.example.courseworkapplication

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList
import com.google.android.material.textview.MaterialTextView

// The home page of the app
class MainActivity : AppCompatActivity() {

    /* I use the firebase realtime database for storing reviews and restaurants,
    firebase custom authentication for authenticating users and firebase storage for storing images
     that are tagged in reviews. I also use recycler views when displaying lists of results and
     a toolbar at the top containing buttons relevant to each activity. These values
     are often defined at the start of my activities. All data used in this app is dummy data
     and is purely demonstrative in nature. */

    private lateinit var database: DatabaseReference
    private var mAuth = FirebaseAuth.getInstance()
    val user = mAuth.currentUser
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapterUser
    lateinit var recommendation: Restaurants
    val reviews = ArrayList<Reviews>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Assigning the database reference to my database
        database =
            Firebase.database("https://coursework-application-e5ea8-default-rtdb.europe-west1.firebasedatabase.app").reference

        // Identifying if the user is a guest or not when displaying welcome message
        val emailId = if (user?.email == null) {
            "Guest"
        } else {
            user.email
        }

        // Uncomment to use the below function, it can be found at the bottom of this file.
        // populateRestaurants()

        // Initialising various values and variables such as views from the xml file
        val userTextView = findViewById<TextView>(R.id.userTextView)
        val restaurantDescView = findViewById<MaterialTextView>(R.id.info_text)
        val restaurantName = findViewById<MaterialTextView>(R.id.textView3)
        val restaurantLocation = findViewById<MaterialTextView>(R.id.location_text)
        val restaurantStyle = findViewById<MaterialTextView>(R.id.styleView)
        val searchButton = findViewById<Button>(R.id.searchButton)
        var recommendRestaurant: String
        var recommendStyle: String
        val userReviewsView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyFieldSnackbar = Snackbar.make(
            searchButton,
            "Please fill this field",
            Snackbar.LENGTH_SHORT
        )

        // Setting up the recycler view for this activity
        linearLayoutManager = LinearLayoutManager(this)
        userReviewsView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapterUser(reviews)
        userReviewsView.adapter = adapter

        // Setting up the app bar
        val mToolbar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab!!.title = getString(R.string.homeString)
        ab.setDisplayHomeAsUpEnabled(false)

        // Welcome message
        userTextView.text = "Welcome $emailId !"

        /* A series of queries used to retrieve a personalised recommendation are featured below.
        * Essentially, reviews by the user with a score of 4 or higher are selected, and the last one located is examined. The restaurant name from
        * the review is then taken and a query on this restaurant is done to locate the style. Then a third query is performed to find restaurants
        * with matching styles while also making sure this restaurant has not already been reviewed by the user. This is for my personalised
        * recommendations bonus feature. This query also provides the values for the Recycler View in the home page displaying user reviews.*/
        if (user?.email != null) {
            // Assigning database reference to my database
            database =
                Firebase.database("https://coursework-application-e5ea8-default-rtdb.europe-west1.firebasedatabase.app").reference

            // The reviews of each user are ordered by the number of likes they have
            val searchQuery = database.child("Reviews").orderByChild("likeCount")
            val searchQuery2 = database.child("Restaurants")

            searchQuery.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)
                    val review: Reviews = dataSnapshot.getValue(Reviews::class.java)!!
                    if (review.user == user.email) {
                        // Adding reviews that belong to the current user
                        reviews.add(review)
                        adapter.notifyItemInserted(reviews.size - 1)
                        if (review.rating!! >= 4) {
                            recommendRestaurant = review.restaurant.toString()
                            Log.d(ContentValues.TAG, "recommendRestaurant: $recommendRestaurant")
                            searchQuery2.addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                                    val restaurant: Restaurants =
                                        dataSnapshot.getValue(Restaurants::class.java)!!
                                    if (restaurant.name == recommendRestaurant) {
                                        recommendStyle = restaurant.style.toString()
                                        Log.d(ContentValues.TAG, "recommendStyle: $recommendStyle")
                                        val searchQuery3 =
                                            database.child("Restaurants").orderByChild("style")
                                                .equalTo(recommendStyle)
                                        searchQuery3.addChildEventListener(object :
                                            ChildEventListener {
                                            override fun onChildAdded(
                                                dataSnapshot: DataSnapshot,
                                                s: String?
                                            ) {
                                                var alreadyReviewed = false
                                                val restaurantVal2: Restaurants =
                                                    dataSnapshot.getValue(Restaurants::class.java)!!
                                                for (i in 0 until reviews.size) {
                                                    if (reviews[i].restaurant == restaurantVal2.name) {
                                                        alreadyReviewed = true
                                                        break
                                                    }
                                                }
                                                if (!alreadyReviewed) {
                                                    Log.d(
                                                        ContentValues.TAG,
                                                        "restaurantName: " + restaurantVal2.name
                                                    )
                                                    recommendation = restaurantVal2
                                                    restaurantDescView.text =
                                                        recommendation.description
                                                    restaurantName.text = recommendation.name
                                                    restaurantLocation.text =
                                                        recommendation.location
                                                    restaurantStyle.text = recommendation.style
                                                }
                                            }

                                            override fun onChildChanged(
                                                dataSnapshot: DataSnapshot,
                                                s: String?
                                            ) {
                                            }

                                            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                                            override fun onChildMoved(
                                                dataSnapshot: DataSnapshot,
                                                s: String?
                                            ) {
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {}
                                        })
                                    }
                                }

                                override fun onChildChanged(
                                    dataSnapshot: DataSnapshot,
                                    s: String?
                                ) {
                                }

                                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } else {
            // In the case that the user is a guest, a simple message is displayed instead
            val introText = findViewById<MaterialTextView>(R.id.textView)
            introText.text = getString(R.string.main_guest_intro)
        }

        // A search button the takes the user to the results page once they've entered a query
        searchButton.setOnClickListener {
            when {
                TextUtils.isEmpty(
                    findViewById<TextInputLayout>(R.id.outlinedTextField).editText?.text.toString()
                        .trim { it <= ' ' }) -> {
                    emptyFieldSnackbar.show()
                }
                else -> {
                    val userInput: String =
                        findViewById<TextInputLayout>(R.id.outlinedTextField).editText?.text.toString()
                            .trim { it <= ' ' }
                    val intent = Intent(this@MainActivity, ResultsPage::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("userInput", userInput)
                    startActivity(intent)
                }
            }
        }
    }

    // Setting up the app bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // The logout button logs the user out and also deletes their temporary account if they are a guest
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
        return super.onOptionsItemSelected(item)
    }

    /* This function can be used to quickly add new restaurants to the database for testing. Adding
    * a restaurant with a duplicate name will overwrite the original. I use name as the child
    * value in the database to make querying more straightforward. */
    private fun populateRestaurants() {
        val name = "GoodFishAndChips"
        val description = "The best fish and chips you'll find in town."
        val style = "Fish and Chips"
        val location = "34 Food Drive"
        val restaurant = Restaurants(name, description, style, location)
        database.child("Restaurants").child(name).setValue(restaurant)
        val name2 = "SeasideFood"
        val description2 = "Authentic fish and chips with fresh fish from local sources."
        val style2 = "Fish and Chips"
        val location2 = "23 Seaside Road"
        val restaurant2 = Restaurants(name2, description2, style2, location2)
        database.child("Restaurants").child(name2).setValue(restaurant2)
    }

}