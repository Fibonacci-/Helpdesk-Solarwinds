package com.helwigdev.helpdesk.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.TabActivity
import com.helwigdev.helpdesk.controller.AuthController
import kotlinx.android.synthetic.main.a_login.*



class Login : AppCompatActivity() {

    private lateinit var auth: AuthController
    //todo ads


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_login)

        this.title = getString(R.string.login)

        //initialize controller
        auth = AuthController(
                applicationContext,
                this,
                et_server,
                et_username,
                et_password,
                cb_use_ssl)
        //startup functions
        auth.disclaimer()
        auth.initETVals()
        
        //check session key
        et_password.hint = "Validating session..."
        auth.checkKey()

        et_password.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_GO) {
                setLoading(true)
                auth.attemptLogin()
                handled = true
            }
            handled
        }

        //the login button
        b_login.setOnClickListener {
            setLoading(true)
            auth.attemptLogin()
        }


    }

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
        setLoading(true)
        b_login.text = getString(R.string.logging_in)
        val i = Intent(this, TabActivity::class.java)
        startActivity(i)
        finish()
    }

    //reset view to default
    fun sessionInvalid(){
        et_password.hint = resources.getString(R.string.password)
    }


}