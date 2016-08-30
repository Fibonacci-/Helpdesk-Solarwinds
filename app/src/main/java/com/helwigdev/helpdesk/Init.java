package com.helwigdev.helpdesk;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Init extends AppCompatActivity implements RNInterface {
    public static final String PREF_SERVER = "server";
    public static final String PREF_API_KEY = "whd_api_key";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_SESSION_KEY = "whd_session_key";
    public static final String PREF_TECH_ID = "whd_tech_id";
    public static final String PREF_INSTANCE_ID = "whd_instance_id";
    public static final String PREF_COOKIE = "whd_cookie";
    public static final String PREF_DISCLAIMER = "seen_disclaimer";

    private static final String TAG = "Init";

    SharedPreferences preferences;
    EditText etPassword;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);



        /*
        TODO
        implement searching
        add progress indicator while logging in
        put ads in
        put ad removal charge in
         */

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5637328886369714~1187638383");

        AdView mAdView = (AdView) findViewById(R.id.av_init_bottom);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mAdView.loadAd(adRequest);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //show disclaimer
        if(!preferences.contains(PREF_DISCLAIMER)){
            //show disclaimer

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            //write preference
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(PREF_DISCLAIMER, "seen");
                            editor.apply();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            Toast.makeText(getApplicationContext(),"You have to read and agree.",Toast.LENGTH_LONG).show();
                            finish();

                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.disclaimer)).setPositiveButton("I read the whole paragraph", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

        pd = new ProgressDialog(this);

        final EditText etServer = (EditText) findViewById(R.id.et_init_server);
        final EditText etUsername = (EditText) findViewById(R.id.et_init_username);
        etPassword = (EditText) findViewById(R.id.et_init_password);
        final Button bLogin = (Button) findViewById(R.id.b_init_login);

        if (preferences.contains(PREF_SERVER)) {
            etServer.setText(preferences.getString(PREF_SERVER, ""));
        }
        if (preferences.contains(PREF_USERNAME)) {
            etUsername.setText(preferences.getString(PREF_USERNAME, ""));
        }

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //set done keyboard action
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    bLogin.performClick();
                }
                return false;
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get values for everything
                String servername = etServer.getText().toString();
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (servername.equals("") || username.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_fields_not_full), Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREF_USERNAME, username);
                editor.putString(PREF_SERVER, servername);
                editor.apply();


                //attempt login
                getSessionKey(password);
            }
        });

        //check if session key exists and is valid
        checkSessionKey();

    }

    private void checkSessionKey() {
        if (preferences.contains(PREF_SESSION_KEY)) {
            pd.setMessage(getResources().getString(R.string.session_check));
            pd.show();
            String sUrl = "http://" +
                    preferences.getString(Init.PREF_SERVER, "") +
                    "/helpdesk/WebObjects/Helpdesk.woa/ra/Tickets/mine" +
                    "?page=1&limit=1" +
                    "&sessionKey=" +
                    preferences.getString(Init.PREF_SESSION_KEY, "");
            //try to get list of tickets

            try {
                String cookie = preferences.getString(Init.PREF_COOKIE, "");
                new ReadNetwork(1, this, true, cookie).execute(new URL(sUrl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getSessionKey(String pass) {
        //build URL
        String s = "http://" + preferences.getString(PREF_SERVER, "") +
                "/helpdesk/WebObjects/Helpdesk.woa/ra/Session?username=" +
                preferences.getString(PREF_USERNAME, "") +
                "&password=" + pass;

        try {
            URL url = new URL(s);
            Log.d(TAG, "Sending net request");
            new ReadNetwork(0, this, false, null).execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResult(String output, int taskID) {
        if (taskID == 0) {//initial login check
            //should pop up a progress dialog

            pd.setMessage(getResources().getString(R.string.loading));
            pd.show();

            Log.d(TAG, "Recv net request");
            Log.d(TAG, output);
            try {
                JSONObject o = new JSONObject(output);
                if (o.getString("type").equals("Session")) {
                    //OK to continue
                    String sessionKey = o.getString("sessionKey");
                    String techId = o.getString("currentTechId");
                    String instanceId = o.getString("instanceId");

                    Log.d(TAG, sessionKey);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PREF_SESSION_KEY, sessionKey);
                    editor.putString(PREF_TECH_ID, techId);
                    editor.putString(PREF_INSTANCE_ID, instanceId);
                    editor.apply();

                    pd.dismiss();

                    Intent i = new Intent(this, TicketListActivity.class);
                    startActivity(i);
                    finish();

                } else if (o.getString("type").equals("Error")) {
                    String message = o.getString("message");
                    pd.dismiss();
                    Toast.makeText(this, "Error: server returned message: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                pd.dismiss();
                e.printStackTrace();
                Toast.makeText(this, "Server exists, but did not respond correctly. Is the URL correct?", Toast.LENGTH_LONG).show();
            }
        } else if (taskID == 1) {
            //session succeeded
            Log.d(TAG, output);
            pd.dismiss();

            Intent i = new Intent(this, TicketListActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void authErr(int type, int taskId) {
        if (taskId == 0) {
            switch (type) {
                case 401:
                    //deauth
                    Toast.makeText(this, "Username or password incorrect", Toast.LENGTH_SHORT).show();
                    etPassword.setText("");
                    break;
                case 403:
                    //not allowed
                    Toast.makeText(this, "You are not allowed to do that.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Something broke: " + type, Toast.LENGTH_SHORT).show();
            }
        } else if (taskId == 1) {
            //session attempt
            //no action necessary
            if(pd.isShowing()) {
                pd.dismiss();
            }
        }

    }

    @Override
    public void setCookie(String cookie) {
        preferences.edit().putString(PREF_COOKIE, cookie).apply();
    }
}
