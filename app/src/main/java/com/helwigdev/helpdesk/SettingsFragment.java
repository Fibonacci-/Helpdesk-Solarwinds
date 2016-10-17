package com.helwigdev.helpdesk;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by helwi on 10/15/2016.
 */

public class SettingsFragment extends PreferenceFragment {

    Preference removeAds;
    PreferenceCategory devInfo;

    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String fireId = FirebaseInstanceId.getInstance().getToken();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_settings);
        removeAds = findPreference("key_pref_action_remove_ads");
        devInfo = (PreferenceCategory) findPreference("key_pref_info");

        //set up billing
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        removeAds.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //TODO init billing
                return false;
            }
        });

        //get IDs and display
        Preference firebaseID = new Preference(getActivity());
        firebaseID.setTitle(getResources().getString(R.string.firebase_id_title));
        if(fireId != null){
            firebaseID.setSummary(fireId);
        } else {
            firebaseID.setSummary(getResources().getString(R.string.not_set));
        }

        firebaseID.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ClipboardManager myClipboard = (ClipboardManager)getActivity().getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip = ClipData.newPlainText(fireId,fireId);
                myClipboard.setPrimaryClip(myClip);
                Log.d("Firebase ID",fireId);
                Toast.makeText(getActivity(), "Sent Firebase ID to clipboard & logcat", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        //get:
        //version number, version ID
        PackageInfo pInfo = null;
        String version = "";
        int verCode = -1;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
            verCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        Preference prefVersionName = new Preference(getActivity());
        prefVersionName.setTitle(getResources().getString(R.string.version_name));
        prefVersionName.setSummary(version);

        //version ID
        Preference prefVersionId = new Preference(getActivity());
        prefVersionId.setTitle(getResources().getString(R.string.version_id));
        prefVersionId.setSummary(verCode + "");

        //admob ID
        String android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        final String deviceId = md5(android_id).toUpperCase();
        Preference prefAdmobId = new Preference(getActivity());
        prefAdmobId.setTitle(getResources().getString(R.string.admob_id));
        prefAdmobId.setSummary(deviceId);

        prefAdmobId.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ClipboardManager myClipboard = (ClipboardManager)getActivity().getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip = ClipData.newPlainText(deviceId,deviceId);
                myClipboard.setPrimaryClip(myClip);
                Log.d("AdMob Device ID",deviceId);
                Toast.makeText(getActivity(), "Sent AdMob Device ID to clipboard & logcat", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        devInfo.addPreference(prefVersionName);
        devInfo.addPreference(prefVersionId);
        devInfo.addPreference(prefAdmobId);
        devInfo.addPreference(firebaseID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            getActivity().unbindService(mServiceConn);
        }
    }

    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            FirebaseCrash.report(e);
        }
        return "";
    }
}
