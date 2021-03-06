package com.helwigdev.helpdesk.model

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.helwigdev.helpdesk.controller.AuthController
import java.net.URISyntaxException
import java.net.UnknownHostException

class AuthModel(context: Context, private val parent: AuthController){
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)


    /*
    *********
    DATA GETS
    *********
     */

    fun getSessionKey(password:String){

        val prefix = if (prefs.getBoolean(PREF_USE_SSL, true)) "https://" else "http://"
        val s = "/helpdesk/WebObjects/Helpdesk.woa/ra/Session?username=" +
                prefs.getString(PREF_USERNAME, "") +
                "&password=" + password

        FuelManager.instance.basePath = prefix + prefs.getString(PREF_SERVER, "")
        FuelManager.instance.timeoutReadInMillisecond = 30000
        try {
            s.httpGet().responseString { _, response, result ->
                var (data, error) = result

                val cookieHeader = response.headers["Set-Cookie"]
                val cookie = if (cookieHeader != null) cookieHeader[0] else ""

                if (data == null) data = ""

                if (error == null) {
                    parent.sessionKeyResult(NetResult(data, response.statusCode, false, cookie))
                } else {
                    if(error.exception is UnknownHostException){
                        parent.sessionKeyResult(NetResult("Invalid host: no special characters allowed", response.statusCode, true, cookie))
                    } else {
                        parent.sessionKeyResult(NetResult(error.cause.toString(), response.statusCode, true, cookie))
                    }
                }

            }
        } catch(e: URISyntaxException){
            e.printStackTrace()
            parent.sessionKeyResult(NetResult("URL is malformed - include only the url, no special characters", -5, true, cookie = ""))
        }
        catch (e: Exception){
            e.printStackTrace()
            Crashlytics.logException(e)
            parent.sessionKeyResult(NetResult("Error connecting to server - is the URL correct?", -5, true, cookie = ""))
        }

    }


    fun checkSessionKey(){
        val s = "/helpdesk/WebObjects/Helpdesk.woa/ra/Tickets/mine" +
                "?page=1&limit=1" +
                "&sessionKey=" + prefs.getString(PREF_SESSION_KEY, "")
        val prefix = if (prefs.getBoolean(PREF_USE_SSL, true)) "https://" else "http://"
        FuelManager.instance.basePath = prefix + prefs.getString(PREF_SERVER, "")
        FuelManager.instance.timeoutReadInMillisecond = 30000
        s.httpGet()
            .header(Pair("Cookie",prefs.getString(PREF_COOKIE,"")))
            .responseString { _, response, result ->
                var (data, error) = result
                if(data == null) data = ""

                val cookieHeader = response.headers["Set-Cookie"]
                val cookie = if(cookieHeader != null) cookieHeader[0] else ""

                if(error == null){
                    parent.checkKeyResult(NetResult(data,response.statusCode, false, cookie))
                } else {
                    parent.checkKeyResult(NetResult(error.cause.toString(),response.statusCode, true, cookie))
                }
            }

    }

    companion object {
        const val PREF_SERVER: String = "server"
        const val PREF_USERNAME = "username"
        const val PREF_SESSION_KEY = "whd_session_key"
        const val PREF_USE_SSL = "should_use_ssl"
        const val PREF_COOKIE = "whd_cookie"
        const val PREF_DISCLAIMER = "seen_disclaimer"
        const val PREF_TECH_ID = "current_tech_id"
    }

}