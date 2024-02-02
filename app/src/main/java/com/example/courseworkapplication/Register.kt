package com.example.courseworkapplication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.example.courseworkapplication.MainActivity as MainActivity
import com.google.android.material.textfield.TextInputLayout

// This activity allows a user to register a new account or sign in as a guest
class Register : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.registerButton)
        val btnLogin =
            findViewById<com.google.android.material.textview.MaterialTextView>(R.id.loginText)
        val btnGuestLogin =
            findViewById<com.google.android.material.textview.MaterialTextView>(R.id.registerTextView1)

        val mToolbar = findViewById<View>(R.id.toolbar2) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab!!.title = getString(R.string.register_string2)
        ab.setDisplayHomeAsUpEnabled(false)

        /* The guest login uses the anonymous login feature provided by firebase that allows the user to sign in
        * without any credentials. Checking whether a user has credentials is how guests are identified and the app
        * responds accordingly. */
        btnGuestLogin.setOnClickListener {
            mAuth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInAnonymously:success")
                        val intent =
                            Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                        Snackbar.make(
                            btnLogin,
                            "Guest login failed",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

        }

        // Taking the user to the login activity if they already have an account
        btnLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Using firebase authentication to register a new account with the details entered by the user
        btnRegister.setOnClickListener {
            when {
                TextUtils.isEmpty(
                    findViewById<TextInputLayout>(R.id.emailTextField2).editText?.text.toString()
                        .trim { it <= ' ' }) -> {
                    Snackbar.make(
                        btnLogin,
                        "Please enter an email",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(
                    findViewById<TextInputLayout>(R.id.passwordTextField2).editText?.text.toString()
                        .trim { it <= ' ' }) -> {
                    Snackbar.make(
                        btnLogin,
                        "Please enter a password",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                else -> {

                    val emailText: String =
                        findViewById<TextInputLayout>(R.id.emailTextField2).editText?.text.toString()
                            .trim { it <= ' ' }
                    val passwordText: String =
                        findViewById<TextInputLayout>(R.id.passwordTextField2).editText?.text.toString()
                            .trim { it <= ' ' }

                    mAuth.createUserWithEmailAndPassword(
                        emailText,
                        passwordText
                    )
                        .addOnCompleteListener(
                            this
                        ) { task ->
                            if (task.isSuccessful) {
                                val intent =
                                    Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Snackbar.make(
                                    btnLogin,
                                    "Invalid email or password",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }


}