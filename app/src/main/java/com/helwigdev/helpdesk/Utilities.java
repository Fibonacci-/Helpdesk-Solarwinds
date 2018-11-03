package com.helwigdev.helpdesk;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.crashlytics.android.Crashlytics;


/**
 * Created by helwig on 10/20/2016.
 */

public class Utilities {

    public static void showAlert(String title, String message, Context context){
        try {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // close
                        }
                    })
                    .setIcon(R.drawable.ic_themed_error)
                    .show();
        } catch (Exception e){
            Crashlytics.logException(e);
        }
    }

    public static String getSubstring(String message, int endIndex){
        if(message.length() < endIndex){
            return message;
        } else {
            return message.substring(0,endIndex);
        }
    }

}
