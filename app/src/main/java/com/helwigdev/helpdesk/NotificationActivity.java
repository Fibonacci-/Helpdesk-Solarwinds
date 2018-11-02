package com.helwigdev.helpdesk;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by helwi on 10/20/2016.
 */
public class NotificationActivity extends AppCompatActivity implements RNInterface{

    public static final String PREF_USERPASS_IS_ENABLED = "pref_userpass_is_enabled";
    public static final String PREF_API_KEY = "pref_api_key";
    public static final String PREF_NOTIFY_USERNAME = "pref_notify_username";
    public static final String PREF_NOTIFY_PASSWORD = "pref_notify_password";

    EditText etApiKey,etUsername,etPassword;
    Button bEnableNotifications, bEnableUPAuth;
    View vSeparator;
    CheckBox cbEnableUPAuth;
    SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //setup UI
        setContentView(R.layout.activity_notification);
        etApiKey = findViewById(R.id.et_api_key);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        bEnableNotifications = findViewById(R.id.b_enable_notifications);
        bEnableUPAuth = findViewById(R.id.b_enable_userpass_auth);
        vSeparator = findViewById(R.id.v_separator);
        cbEnableUPAuth = findViewById(R.id.cb_userpass_auth);

        //hide elements if necessary
        Boolean upIsEnabled = preferences.getBoolean(PREF_USERPASS_IS_ENABLED, false);
        cbEnableUPAuth.setChecked(upIsEnabled);
        //set up initial state
        if(!upIsEnabled){
            //if up auth is not enabled, hide the relevant views
            toggleVisibility(View.INVISIBLE, vSeparator, etUsername, etPassword, bEnableUPAuth);
        }
        cbEnableUPAuth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {
                if(b){
                    //show warning

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    compoundButton.setChecked(false);

                                    break;
                            }
                        }
                    };


                    AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
                    builder.setMessage(getResources().getString(R.string.user_pass_warning)).setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                    //then make view changes
                    toggleVisibility(View.VISIBLE, vSeparator, etUsername, etPassword, bEnableUPAuth);
                    setEnabled(false, etApiKey, bEnableNotifications);
                } else {
                    toggleVisibility(View.INVISIBLE, vSeparator, etUsername, etPassword, bEnableUPAuth);
                    setEnabled(true, etApiKey, bEnableNotifications);
                }
            }
        });

        //TODO check that entered tech info matches current
        bEnableNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //verify key and save
                String apiKey = etApiKey.getText().toString();
                if(apiKey.equals("")){
                    Toast.makeText(NotificationActivity.this,"Field must not be empty",Toast.LENGTH_LONG).show();
                    return;
                }

                String sUrl = "http://" +
                        preferences.getString(Init.PREF_SERVER, "") +
                        "/helpdesk/WebObjects/Helpdesk.woa/ra/Techs/currentTech" +
                        "?apiKey=" +
                        apiKey;

                try {
                    String cookie = preferences.getString(Init.PREF_COOKIE, "");
                    new ReadNetwork(task_api_validate, NotificationActivity.this, true, cookie).execute(new URL(sUrl));

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }


            }
        });
        bEnableUPAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("NA","Got click");
                //verify combo and save
                String username = etUsername.getText().toString();
                String pass = etPassword.getText().toString();
                if(username.equals("") || pass.equals("")){
                    Toast.makeText(NotificationActivity.this,"Field must not be empty",Toast.LENGTH_LONG).show();
                    return;
                }

                String sUrl = "http://" +
                        preferences.getString(Init.PREF_SERVER, "") +
                        "/helpdesk/WebObjects/Helpdesk.woa/ra/Techs/currentTech" +
                        "?username=" +
                        username +
                        "&password=" +
                        pass;

                try {
                    String cookie = preferences.getString(Init.PREF_COOKIE, "");
                    new ReadNetwork(task_up_validate, NotificationActivity.this, false, cookie).execute(new URL(sUrl));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void toggleVisibility(int state, View... views){
        for(View v: views){
            v.setVisibility(state);
        }
    }

    private void setEnabled(Boolean enabled, View... views){
        for(View v: views){
            v.setEnabled(enabled);
        }
    }

    private static final int task_api_validate = 50;
    private static final int task_up_validate = 51;

    @Override
    public void processResult(String output, int taskID) {
        Log.d("NotificationActivity", "Got result: task: " + taskID + " output: " + output);

    }

    @Override
    public void authErr(int type, int taskId) {
        switch (type) {
            case 401:
                //bad login
                Utilities.showAlert("401 Unauthorized","Bad API key, username or password",this);
                break;
            case 403:
                //not allowed
                Utilities.showAlert("403 Forbidden","You are not allowed to do that",this);
                break;
            case 404:
                //not allowed
                Utilities.showAlert("404 Not Found","The server returned a 404 error. That's all we know.",this);
                break;
            case 444:
                Utilities.showAlert("Network error","Timeout or connection interrupted, try again",this);
                break;
            case 503:
                Utilities.showAlert("503 Service Unavailable","The server may be overloaded. Try again in a few minutes.",this);
                break;
            default:
                Toast.makeText(this, "Server returned error: " + type, Toast.LENGTH_SHORT).show();
                Crashlytics.logException(new Exception("TLF: Server returned error: Task: " + taskId + " Type: " + type));
        }
    }

    @Override
    public void setCookie(String cookie) {
        //not used
    }
}
