package com.helwigdev.helpdesk;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by helwig on 10/16/2015.
 */
public class WriteNetwork extends AsyncTask<URL, Void, String>{

    private static final String TAG = "WriteNetwork";
    private final int taskID;
    private final RNInterface rn;
    private final boolean useCookie;
    private final String customCookie;
    int errType = 200;
    private final String data;
    String cookie = null;

    public WriteNetwork(int TaskID, String data, RNInterface rnInterface, boolean useCookie, String cookie) {
        this.taskID = TaskID;
        this.rn = rnInterface;
        this.useCookie = useCookie;
        this.customCookie = cookie;
        this.data = data;
    }

    @Override
    protected String doInBackground(URL... params) {
        for (URL url : params) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();

                //check for redirects - handles http -> https headers
                Boolean redirect = false;
                int status = urlConnection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER)
                        redirect = true;
                }

                Log.d(TAG, "Response Code ... " + status);
                String newUrl = url.toString();

                if (redirect) {

                    // get redirect url from "location" header field
                    newUrl = urlConnection.getHeaderField("Location");
                    Log.d(TAG, "Redirecting: " + url.toString() + " || to: " + newUrl);
                }
                urlConnection.disconnect();



                //OKHTTP start
                final MediaType JSON
                        = MediaType.parse("application/json");
                Log.d(TAG, data);
                Log.d(TAG, newUrl);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();
                //make request
                RequestBody body = RequestBody.create(JSON, data);
                Request request;
                if(useCookie){
                    request = new Request.Builder()
                            .url(newUrl)
                            .addHeader("content-type", "application/json")
                            .addHeader("Cookie", customCookie + "")
                            .post(body)
                            .build();
                } else {
                    request = new Request.Builder()
                            .url(newUrl)
                            .addHeader("content-type", "application/json")
                            .post(body)
                            .build();
                }


                //post
                Response response = client.newCall(request).execute();

                //get response code & cookie
                String responseBody = response.body().string();
                List<String> headerList = response.headers("Set-Cookie");
                for(String header : headerList) {
                    cookie = header;
                }
                Log.d("OKHTTP Response",responseBody);

                errType = response.code();

                return responseBody;

            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        return "error";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(cookie != null) rn.setCookie(cookie);
        if (s.equals("error") || s.equals("Authentication Required.") || errType < 199 || errType > 300) {
            Log.d(TAG, "Caught response error: data: " + s + " :: response code: " + errType);
            rn.authErr(errType, taskID, s);
        } else {
            rn.processResult(s, taskID);
        }
    }
}
