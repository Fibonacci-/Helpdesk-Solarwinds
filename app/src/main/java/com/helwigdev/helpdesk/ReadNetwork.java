package com.helwigdev.helpdesk;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by helwig on 10/13/2015.
 */
public class ReadNetwork extends AsyncTask<URL, Void, String> {

    private static final String TAG = "ReadNetwork";
    private final boolean useCookie;
    private final String customCookie;


    public ReadNetwork(int TaskID, RNInterface rnInterface, boolean useCookie, String cookie) {
        this.taskID = TaskID;
        this.rn = rnInterface;
        this.useCookie = useCookie;
        this.customCookie = cookie;
    }

    int taskID;
    RNInterface rn;
    int errType = 0;
    String cookie = null;

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
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            }  catch (UnknownHostException e){
                return "{\"type\":\"Error\", \"message\":\"Can't find server\"}";
            }catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                assert urlConnection != null;
                urlConnection.disconnect();

            }
        }
        return "error";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(cookie != null) rn.setCookie(cookie);
        if(s.equals("error")){
            rn.authErr(errType, taskID);
        } else {
            rn.processResult(s, taskID);
        }
    }
}
