package com.helwigdev.helpdesk;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

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

                if (redirect) {
                    // get redirect url from "location" header field

                    String newUrl = urlConnection.getHeaderField("Location");
                    // get the cookie if need, for login
                    String cookies = urlConnection.getHeaderField("Set-Cookie");

                    // open the new connnection again
                    urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();

                    //urlConnection.setRequestProperty("Cookie", cookies);


                }
                if(useCookie){
                    urlConnection.setRequestProperty("Cookie", customCookie);
                }

                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();
                errType = urlConnection.getResponseCode();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                cookie = urlConnection.getHeaderField("Set-Cookie");
                Log.d(TAG, urlConnection.getResponseCode() + "");

                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);//read data stream
                }
                out.close();
                errType = urlConnection.getResponseCode();
                return out.toString();


                //OKHTTP start
//                final MediaType JSON
//                        = MediaType.parse("application/octet-stream");
//                Log.d(TAG, data);
//                Log.d(TAG, url.toString());
//                OkHttpClient client = new OkHttpClient();
//                //make request
//                RequestBody body = RequestBody.create(JSON, data);
//                Request request;
//                if(useCookie){
//                    request = new Request.Builder()
//                            .url(url)
//                            .post(body)
//                            .addHeader("Cookie", customCookie + "")
//                            .build();
//                } else {
//                    request = new Request.Builder()
//                            .url(url)
//                            .post(body)
//                            .build();
//                }
//
//
//                //post
//                Response response = client.newCall(request).execute();
//
//                //get response code & cookie
//                String responseBody = response.body().string();
//                List<String> headerList = response.headers("Set-Cookie");
//                for(String header : headerList) {
//                    cookie = header;
//                }
//                Log.d("OKHTTP",responseBody);
//
//                errType = response.code();
//
//                return responseBody;

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
        if(s.equals("error") || s.equals("Authentication Required.")){
            rn.authErr(errType, taskID);
        } else {
            rn.processResult(s, taskID);
        }
    }
}
