package com.helwigdev.helpdesk.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.TabActivity
import com.helwigdev.helpdesk.controller.AuthController
import kotlinx.android.synthetic.main.a_login.*


class Login : AppCompatActivity() {


    private lateinit var auth: AuthController
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_login)

        this.title = getString(R.string.login)

        //initialize method variables
        auth = AuthController(
                applicationContext,
                this,
                et_server,
                et_username,
                et_password,
                cb_use_ssl)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        //startup functions
        auth.disclaimer()
        auth.initETVals()
        auth.initAdView(av_init_bottom)

        //start session key check process
        et_password.hint = getString(R.string.session_check)
        auth.checkKey()

        //start login process if enter key is pressed in password field
        et_password.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_GO) {
                setLoading(true)
                auth.attemptLogin()
                handled = true
            }
            handled
        }

        //start login process if login button is clicked
        b_login.setOnClickListener {
            setLoading(true)
            auth.attemptLogin()
        }


    }

    //toggles views between loading state
    fun setLoading(loading: Boolean){
        if(loading){
            b_login.setText(R.string.loading)
            b_login.isEnabled = false
        } else {
            b_login.setText(R.string.login)
            b_login.isEnabled = true
        }
    }

    //called after session key has been verified
    fun login(){
        //for some reason starting intents takes a second or two these days
        //so we disable the login views and start the transition process
        setLoading(true)
        b_login.text = getString(R.string.logging_in)
        et_password.setOnEditorActionListener(null)

        //also log dat ish
        val params = Bundle()
        params.putBoolean("success", true)
        mFirebaseAnalytics.logEvent("login", params)

        val i = Intent(this, TabActivity::class.java)
        startActivity(i)

        //quit this activity so it doesn't show up in the back stack
        finish()
    }

    //reset view to default
    fun sessionInvalid(){
        et_password.hint = resources.getString(R.string.password)
    }


}