package com.example.treasurehunt.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.treasurehunt.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Authentication Vars
    private var mAuth = FirebaseAuth.getInstance()

    // login deets
    private val emailPattern = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+"
    private lateinit var emailText : EditText
    private lateinit var passText : EditText
    private lateinit var loginBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        emailText = findViewById(R.id.login_email)
        passText = findViewById(R.id.login_password)
        loginBtn = findViewById(R.id.login)
    }

    /**
     * checked to see if login details are valid. If so, logs in the user
     */
    fun loginClick (view: View) {
        val email = emailText.text.toString().trim()
        val pass = passText.text.toString()

        val isEmailEmpty = emailText.text.isEmpty()
        val isPassEmpty = passText.text.isEmpty()
        val isEmailValid = email.matches(emailPattern.toRegex())


        if ((isEmailEmpty) || (isPassEmpty)) {
            displayMessage(loginBtn, getString(R.string.invalid_missing))
        }
        else if (!isEmailValid) {
            displayMessage(loginBtn, getString(R.string.invalid_email))
        }
        else {
            login(email, pass)
        }
    }

    private fun login(email : String, password : String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(getString(R.string.log_login), getString(R.string.log_login_success))
                    closeKeyBoard()
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Log.w(getString(R.string.log_login),
                        getString(R.string.log_login_failure),
                        task.exception)

                    closeKeyBoard()
                    displayMessage(loginBtn, getString(R.string.login_failed))
                }
            }
    }

    fun showPasswordBtnClicked(view:View) {
        val passTxtBx = findViewById<TextView>(R.id.login_password)
        if (passTxtBx.inputType == InputType.TYPE_CLASS_TEXT) {
            passTxtBx.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        else{
            passTxtBx.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    private fun displayMessage(view : View, msg : String) {
        val snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    /**
     * helper function to close keyboard
     */
    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}