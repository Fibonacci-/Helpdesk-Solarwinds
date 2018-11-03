package com.helwigdev.helpdesk;

import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by helwig on 10/13/2015.
 */
public class ReadNetwork extends AsyncTask<URL, Void, String> {

    private static final String TAG = "ReadNetwork";
    private final boolean useCookie;
    private final String customCookie;


    ReadNetwork(int TaskID, RNInterface rnInterface, boolean useCookie, String cookie) {
        this.taskID = TaskID;
        this.rn = rnInterface;
        this.useCookie = useCookie;
        this.customCookie = cookie;
    }

    private int taskID;
    private RNInterface rn;
    private int errType = 200;
    private String cookie = null;

    @Override
    protected String doInBackground(URL... params) {

        for (URL url : params) {

            try {

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                client.followSslRedirects();
                Request request;
                Log.d("OKHTTP", url.toString());
                if (useCookie) {
                    Log.d("OKHTTP", "Using cookie!");
                    request = new Request.Builder()
                            .url(url)
                            .get()
                            .addHeader("Cookie", customCookie + "")
                            .build();
                } else {
                    request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                }

                //get cookie - because the REST api also requires a consistent cookie
                //this feature is undocumented.
                Response response = client.newCall(request).execute();
                String body = response.body() != null ? response.body().string() : null;
                List<String> headerList = response.headers("Set-Cookie");
                for (String header : headerList) {
                    cookie = header;
                }
                Log.d("OKHTTP response body", body);


                errType = response.code();
                Log.d("OKHTTP response code", errType + "");
                return body;
            } catch (IOException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                errType = 0;
                return "Network error: " + e.getMessage();
            }

        }
        errType = 444;
        return "Network timeout";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (cookie != null) rn.setCookie(cookie);
        //detect failure conditions
        if (s.equals("error") || s.equals("Authentication Required.") || errType < 199 || errType > 300) {
            Log.d(TAG, "Caught response error: data: " + s + " :: response code: " + errType);
            rn.authErr(errType, taskID, s);
        }
        else {
            rn.processResult(s, taskID);
        }
    }
}
