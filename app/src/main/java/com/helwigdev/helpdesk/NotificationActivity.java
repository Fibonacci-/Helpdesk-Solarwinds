package com.helwigdev.helpdesk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by helwi on 10/20/2016.
 */
public class NotificationActivity extends AppCompatActivity {

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
        etApiKey = (EditText) findViewById(R.id.et_api_key);
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        bEnableNotifications = (Button) findViewById(R.id.b_enable_notifications);
        bEnableUPAuth = (Button) findViewById(R.id.b_enable_userpass_auth);
        vSeparator = findViewById(R.id.v_separator);
        cbEnableUPAuth = (CheckBox) findViewById(R.id.cb_userpass_auth);

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
}
