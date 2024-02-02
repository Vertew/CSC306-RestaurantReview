package com.example.courseworkapplication

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

// This activity displays the results of the user query
class ResultsPage : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private var mAuth = FirebaseAuth.getInstance()
    val user = mAuth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_page)

        val queryResultsView = findViewById<RecyclerView>(R.id.queryResultsView)

        // Array list for storing restaurant names to be presented
        val names = ArrayList<String>()

        linearLayoutManager = LinearLayoutManager(this)
        queryResultsView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(names)
        queryResultsView.adapter = adapter

        val userInput = intent.getStringExtra("userInput")

        val mToolbar = findViewById<View>(R.id.toolbar4) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab!!.title = getString(R.string.resultsPageString)
        ab.setDisplayHomeAsUpEnabled(false)

        database =
            Firebase.database("https://coursework-application-e5ea8-default-rtdb.europe-west1.firebasedatabase.app").reference

        val searchQuery = database.child("Restaurants").orderByChild("name").startAt(userInput)
            .endAt(userInput + "\uf8ff")

        searchQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)
                // Adding the results of the query to the names array, which is fed to the recycler view
                names.add(dataSnapshot.key!!)
                adapter.notifyItemInserted(names.size - 1)
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