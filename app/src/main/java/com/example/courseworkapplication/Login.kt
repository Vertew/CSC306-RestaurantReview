package com.example.courseworkapplication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.example.courseworkapplication.MainActivity as MainActivity
import com.google.android.material.textfield.TextInputLayout

/* This activity handles user logins, it is similar to the register activity but uses firebase sign-in
* as opposed to firebase createUser. */
class Login : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val btnLogin = findViewById<Button>(R.id.loginButton)
        val btnRegister =
            findViewById<com.google.android.material.textview.MaterialTextView>(R.id.registerText)

        val mToolbar = findViewById<View>(R.id.toolbar3) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab!!.title = getString(R.string.login)
        ab.setDisplayHomeAsUpEnabled(false)

        btnRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }


        btnLogin.setOnClickListener {
            when {
                TextUtils.isEmpty(
                    findViewById<TextInputLayout>(R.id.emailTextField3).editText?.text.toString()
                        .trim { it <= ' ' }) -> {
                    Snackbar.make(
                        btnLogin,
                        "Please enter an email",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(
                    findViewById<TextInputLayout>(R.id.passwordTextField3).editText?.text.toString()
                        .trim { it <= ' ' }) -> {
                    Snackbar.make(
                        btnLogin,
                        "Please enter a password",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                else -> {

                    val emailText: String =
                        findViewById<TextInputLayout>(R.id.emailTextField3).editText?.text.toString()
                            .trim { it <= ' ' }
                    val passwordText: String =
                        findViewById<TextInputLayout>(R.id.passwordTextField3).editText?.text.toString()
                            .trim { it <= ' ' }

                    mAuth.signInWithEmailAndPassword(
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
                                    "Incorrect email or password",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }


}