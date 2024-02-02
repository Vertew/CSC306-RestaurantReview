package com.example.courseworkapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

// This activity handles the creation of user reviews
class ReviewActivity : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()
    private val user = mAuth.currentUser
    private lateinit var database: DatabaseReference
    private val storage = Firebase.storage

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val fromEdit = intent.getBooleanExtra("fromEdit", false)

        val ratingbar = findViewById<RatingBar>(R.id.reviewRating)
        val titleInput = findViewById<TextInputLayout>(R.id.reviewTitleInput)
        val reviewInput = findViewById<TextInputLayout>(R.id.reviewInput)
        val postButton = findViewById<Button>(R.id.postButton)
        val tagImageButton = findViewById<Button>(R.id.imageTagButton)
        val tagLocationButton = findViewById<Button>(R.id.locationTagButton)
        val taggedImage = findViewById<ImageView>(R.id.taggedImageView)
        var identifier: String
        var imageUrl: String? = null
        var tagLocation: String? = null

        // Checking if the user is arriving from the edit button or not, then updating values accordingly
        if (fromEdit) {
            ratingbar.rating = intent.getFloatExtra("ratingBar", 0F)
            var editable =
                Editable.Factory.getInstance().newEditable(intent.getStringExtra("reviewTitle"))
            titleInput.editText?.text = editable
            editable =
                Editable.Factory.getInstance().newEditable(intent.getStringExtra("reviewText"))
            reviewInput.editText?.text = editable
            if (intent.getStringExtra("locationText") != null) {
                findViewById<TextView>(R.id.locationInput).text =
                    intent.getStringExtra("locationText")
            }
            imageUrl = intent.getStringExtra("image")
            Picasso.get().load(imageUrl).into(taggedImage)
        }

        val restaurantNameEdit = intent.getStringExtra("restaurantNameEdit")


        val emptyFieldSnackbar = Snackbar.make(
            postButton,
            "Title or review fields are empty",
            Snackbar.LENGTH_SHORT
        )
        // Images URLs are stored in firebase storage, so a storage reference is required.
        val storageRef = storage.reference

        database =
            Firebase.database("https://coursework-application-e5ea8-default-rtdb.europe-west1.firebasedatabase.app").reference

        val mToolbar = findViewById<View>(R.id.toolbar6) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab!!.title = getString(R.string.review_activity_string)
        ab.setDisplayHomeAsUpEnabled(false)

        // Loading image from photo gallery and uploading it to cloud storage, then acquiring a download url
        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                taggedImage.setImageURI(it)
                val imageURI = it
                val imageRef = storageRef.child("images/${imageURI.lastPathSegment}")
                val uploadTask = imageRef.putFile(imageURI)
                // Registering listeners to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener {
                    Log.d(ContentValues.TAG, "On image upload: Upload failed")
                }.addOnSuccessListener { taskSnapshot ->
                    Log.d(
                        ContentValues.TAG,
                        "On image upload: Upload successful" + taskSnapshot.metadata.toString()
                    )
                }

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { exception ->
                            throw exception
                        }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageUrl = task.result.toString()
                        Log.d(ContentValues.TAG, "On URL download: Download successful $imageUrl")
                    } else {
                        Log.d(ContentValues.TAG, "On URL download: Download failed")
                    }
                }

            })

        postButton.setOnClickListener {
            when {
                TextUtils.isEmpty(
                    findViewById<TextInputLayout>(R.id.reviewInput).editText?.text.toString()
                        .trim { it <= ' ' }) -> {
                    emptyFieldSnackbar.show()
                }
                TextUtils.isEmpty(
                    findViewById<TextInputLayout>(R.id.reviewTitleInput).editText?.text.toString()
                        .trim { it <= ' ' }) -> {
                    emptyFieldSnackbar.show()
                }
                else -> {
                    val rating = ratingbar.rating
                    val reviewText =
                        findViewById<TextInputLayout>(R.id.reviewInput).editText?.text.toString()
                            .trim { it <= ' ' }
                    var reviewTitle =
                        findViewById<TextInputLayout>(R.id.reviewTitleInput).editText?.text.toString()
                            .trim { it <= ' ' }

                    // Passing parameters to either add review function or update review function based on context
                    if (fromEdit) {
                        identifier = intent.getStringExtra("identifier")
                        val likeCount = intent.getIntExtra("likeCount", 0)
                        updateReview(
                            reviewText,
                            rating,
                            tagLocation,
                            imageUrl,
                            reviewTitle,
                            identifier,
                            restaurantNameEdit,
                            likeCount
                        )
                    } else {
                        // Eliminating disallowed characters from review title since it acts as a database path
                        val re = "[^A-Za-z0-9 ]".toRegex()
                        reviewTitle = re.replace(reviewTitle, "")
                        identifier = reviewTitle
                        addReview(
                            reviewText,
                            rating,
                            tagLocation,
                            imageUrl,
                            reviewTitle,
                            identifier
                        )
                    }

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        tagImageButton.setOnClickListener {
            loadImage.launch("image/*")
        }

        // When the tag location button is pressed, the location of the restaurant being reviewed is retrieved
        tagLocationButton.setOnClickListener {
            database =
                Firebase.database("https://coursework-application-e5ea8-default-rtdb.europe-west1.firebasedatabase.app").reference
            val restaurantName: String
            if (fromEdit) {
                restaurantName = restaurantNameEdit
            } else {
                restaurantName = intent.getStringExtra("restaurantName")
            }
            val searchQuery =
                database.child("Restaurants").orderByChild("name").equalTo(restaurantName)
            searchQuery.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    Log.d(ContentValues.TAG, "tagLocation:" + dataSnapshot.key!!)
                    val restaurant: Restaurants = dataSnapshot.getValue(Restaurants::class.java)!!
                    tagLocation = restaurant.location
                    findViewById<TextView>(R.id.locationInput).text = restaurant.location
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
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
                startActivity(Intent(this, RestaurantPage::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // This function is used to add a new review to the database
    private fun addReview(
        reviewText: String,
        rating: Float,
        location: String?,
        imageURL: String?,
        reviewTitle: String,
        identifier: String
    ) {
        val userEmail = user?.email
        val restaurantName = intent.getStringExtra("restaurantName")

        val review = Reviews(
            userEmail,
            restaurantName,
            rating,
            reviewText,
            location,
            imageURL,
            reviewTitle,
            identifier,
            0
        )
        database.child("Reviews").child(identifier).setValue(review)
    }

    // This function is used to update an existing review
    private fun updateReview(
        reviewText: String,
        rating: Float,
        location: String?,
        imageURL: String?,
        reviewTitle: String,
        identifier: String,
        restaurantName: String,
        likeCount: Int
    ) {
        val userEmail = user?.email
        val review = Reviews(
            userEmail,
            restaurantName,
            rating,
            reviewText,
            location,
            imageURL,
            reviewTitle,
            identifier,
            likeCount
        )

        database.child("Reviews").child(identifier).setValue(review)

    }


}