package com.helwigdev.helpdesk.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.model.AuthModel
import com.helwigdev.helpdesk.model.NetResult
import com.helwigdev.helpdesk.view.Login
import org.json.JSONObject

class AuthController(private val context: Context, val parent: Login,
                     private val server: EditText, val username: EditText,
                     val password: EditText, val use_ssl: CheckBox) {

    private val prefs: SharedPreferences = context.getSharedPreferences(context.applicationInfo.packageName,0)


    val model = AuthModel(context,this)

    /*
    ************************
    INITIALIZATION FUNCTIONS
    ************************
     */


    fun initETVals(){
        server.setText(prefs.getString(AuthModel.PREF_SERVER, ""))
        username.setText(prefs.getString(AuthModel.PREF_USERNAME, ""))
    }

    fun disclaimer(){
        //show disclaimer
        if (!prefs.contains(AuthModel.PREF_DISCLAIMER)) {
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        val editor = prefs.edit()
                        editor.putString(AuthModel.PREF_DISCLAIMER, "seen")
                        editor.apply()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        Toast.makeText(context, "You have to read and agree.", Toast.LENGTH_LONG).show()
                        parent.finish()
                    }
                }
            }

            val builder = AlertDialog.Builder(parent)
            builder.setMessage(context.resources.getString(R.string.disclaimer))
                    .setPositiveButton("Gotcha", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
        }
    }

    /*
    *********
    DATA GETS
    *********
     */

    @SuppressLint("ApplySharedPref")
    fun attemptLogin(){
        val views = arrayListOf(server, username, password)
        if(server.text.toString().contains("http://", true) ||
                server.text.toString().contains("https://", true) ||
                server.text.toString().contains("/", true)){
            server.error = "Only type the FQDN of the server: don't include http or anything after the TLD"
            parent.setLoading(false)
            return
        }
        //make sure none of them are empty
        for(view in views){
            view.error = null
            view.setText(view.text.trim())
            if(view.text.toString() == ""){
                view.error = "Cannot be empty"
                parent.setLoading(false)
                return
            }
        }

        val editor = prefs.edit()
        editor.putString(AuthModel.PREF_SERVER, server.text.toString())
        editor.putString(AuthModel.PREF_USERNAME, username.text.toString())
        editor.putBoolean(AuthModel.PREF_USE_SSL, use_ssl.isChecked)
        editor.commit()//avoid race conditions
        model.getSessionKey(password.text.toString())

    }

    fun checkKey(){
        if(prefs.contains(AuthModel.PREF_SESSION_KEY) && prefs.getString(AuthModel.PREF_SESSION_KEY, "") != ""){
            //contains data
            model.checkSessionKey()
        } else {
            parent.sessionInvalid()
        }
    }

    /*
    ************
    DATA PROCESS
    ************
     */

    @SuppressLint("ApplySharedPref")
    fun sessionKeyResult(result: NetResult){
        if(result.error){
            parent.setLoading(false)
            if(result.responseCode == 401){
                password.error = "Username or password incorrect: 401 Unauthorized"
                password.setText("")
            } else {
                server.error = result.result.removePrefix("java.net.UnknownHostException: ")
            }
        } else {
            //handle login
            val o = JSONObject(result.result)
            val key = o.getString("sessionKey")
            if(key != null){
                prefs.edit()
                        .putString(AuthModel.PREF_SESSION_KEY, key)
                        .putString(AuthModel.PREF_COOKIE, result.cookie)
                        .commit()
                parent.login()
            } else {
                server.setText(context.getString(R.string.login_invalid_no_sessionkey))
                parent.setLoading(false)
            }
        }
    }

    fun checkKeyResult(result:NetResult){
        Log.d("AuthController","Session status code is " + result.responseCode)
        if(result.error){
            parent.sessionInvalid()
        } else {
            parent.login()
        }
    }

}