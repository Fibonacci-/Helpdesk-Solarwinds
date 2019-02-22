package com.helwigdev.helpdesk.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.SettingsActivity
import com.helwigdev.helpdesk.model.AuthModel
import com.helwigdev.helpdesk.model.NetResult
import com.helwigdev.helpdesk.view.Login
import org.json.JSONException
import org.json.JSONObject


class AuthController(private val context: Context, val parent: Login,
                     private val server: EditText, val username: EditText,
                     val password: EditText, val use_ssl: CheckBox) {


    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val model = AuthModel(context,this)

    /*
    ************************
    INITIALIZATION FUNCTIONS
    ************************
     */


    //grab the previously used values from preferences and insert them into the edittexts
    fun initETVals(){
        server.setText(prefs.getString(AuthModel.PREF_SERVER, ""))
        username.setText(prefs.getString(AuthModel.PREF_USERNAME, ""))
    }

    //set up the ads if need be
    fun initAdView(adView: AdView){
        MobileAds.initialize(context, "ca-app-pub-5637328886369714~1187638383")

        if (!prefs.getBoolean(SettingsActivity.PREF_ADS_REMOVED, false)) {
            val adRequest = AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("E498D046420E068963DD7607B804BA3D")
                    .addTestDevice("960E1155E3858B01540E73FBD53DB405")
                    .addTestDevice("4283C4B685567508FFF2525F7AB7B819")
                    .build()
            adView.loadAd(adRequest)
        } else {
            //remove ad
            adView.visibility = View.GONE
        }
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

        //tell the user if they've entered the server incorrectly
        if(server.text.toString().contains("http://", true) ||
                server.text.toString().contains("https://", true) ||
                server.text.toString().contains("/", true)){
            server.error = "Only type the FQDN of the server: don't include http or anything after the TLD"
            server.requestFocus()
            parent.setLoading(false)
            return
        }

        if(server.text.toString().contains(" ")){
            server.error = "Spaces are not supported in a DNS address"
            server.requestFocus()
            parent.setLoading(false)
            return
        }

        //make sure none of them are empty
        for(view in views){
            view.setText(view.text.trim())
            if(view.text.toString() == ""){
                view.error = "Cannot be empty"
                view.requestFocus()
                parent.setLoading(false)
                return
            }
        }

        //insert the data we've collected
        val editor = prefs.edit()
        editor.putString(AuthModel.PREF_SERVER, server.text.toString())
        editor.putString(AuthModel.PREF_USERNAME, username.text.toString())
        editor.putBoolean(AuthModel.PREF_USE_SSL, use_ssl.isChecked)
        editor.commit()//we need this data available immediately
        //try to get a session key
        model.getSessionKey(password.text.toString())

    }

    fun checkKey(){
        //if we have a session key saved
        if(prefs.contains(AuthModel.PREF_SESSION_KEY) && prefs.getString(AuthModel.PREF_SESSION_KEY, "") != ""){
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
            when {
                result.responseCode == 401 -> {
                    password.error = "Username or password incorrect: 401 Unauthorized"
                    password.setText("")
                    password.requestFocus()
                }
                result.responseCode == 404 -> {
                    server.error = "Can't find a compatible API: 404 Not Found"
                    server.requestFocus()
                }
                else -> {
                    val t = Throwable(result.result)
                    Log.e("AuthController","Unhandled net error",t)
                    Crashlytics.logException(t)
                    server.error = result.result.removePrefix("java.net.UnknownHostException: ")
                    server.requestFocus()
                }
            }
        } else {
            //handle login
            try{
            val o = JSONObject(result.result)
            val key = o.getString("sessionKey")
            if(key != null) {
                prefs.edit()
                        .putString(AuthModel.PREF_SESSION_KEY, key)
                        .putString(AuthModel.PREF_COOKIE, result.cookie)
                        .commit()
                parent.login()
                }
            } catch (e: JSONException){

                val t = Throwable("Expected JSON with sessionKey object but received: " + result.result + " ::: " + e.message)
                Crashlytics.logException(t)
                Log.e("AuthController","Server response did not return correct JSON",t)
                server.setText(context.getString(R.string.login_invalid_no_sessionkey))
                parent.setLoading(false)

            }

        }
    }

    fun checkKeyResult(result:NetResult){
        Log.d("AuthController","Session status code is " + result.responseCode)

        if(result.error){
            if(result.responseCode != 401){
                val t = Throwable("AuthController received unexpected response from model: code: " +
                        result.responseCode + " | data: " + result.result + " | error: " + result.error.toString())
                Log.e("AuthController","Unexpected response from model",t)
                Crashlytics.logException(t)
            }
            parent.sessionInvalid()
        } else {
            parent.login()
        }
    }

}