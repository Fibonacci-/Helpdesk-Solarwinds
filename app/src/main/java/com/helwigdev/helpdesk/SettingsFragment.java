package com.helwigdev.helpdesk;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by helwi on 10/15/2016.
 */

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    protected static final String SKU_REMOVE_ADS = "iap_whd_remove_ads";
    protected static final int PURCHASE_ADS_REQUEST_CODE = 1000;
    Preference removeAds;
    Preference restorePurchase;
    Preference configureNotifications;
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
            Log.d("TEST", "mService ready to go!");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        final String fireId = FirebaseInstanceId.getInstance().getToken();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_settings);
        removeAds = findPreference("key_pref_action_remove_ads");
        restorePurchase = findPreference("key_pref_action_restore_purchase");
        devInfo = (PreferenceCategory) findPreference("key_pref_info");
        configureNotifications = findPreference("key_pref_configure_notifications");

        Preference prefLegal = findPreference("key_pref_legal");
        prefLegal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), AttributionActivity.class));
                return true;
            }
        });

        configureNotifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), NotificationActivity.class));
                return true;
            }
        });


        removeAds.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String serial = Build.SERIAL;

                if (mService != null) {
                    try {
                        Bundle buyIntentBundle = mService.getBuyIntent(3, getActivity().getPackageName(),
                                SKU_REMOVE_ADS, "inapp", serial);
                        Log.d(TAG, "Ad removal: response code: " + buyIntentBundle.getInt("RESPONSE_CODE"));

                        FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, new Bundle());
                        switch (buyIntentBundle.getInt("RESPONSE_CODE")) {
                            case 0:
                                //Success
                                Log.i("Billing start", "Got billing intent OK");
                                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(),
                                        PURCHASE_ADS_REQUEST_CODE, new Intent(), 0, 0, 0);
                                break;
                            case 1:
                                //user cancelled
                                Toast.makeText(getActivity(), getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                                break;
                            case 2:
                                //service unavailable
                                showAlert("Service unavailable","Can't reach Google at the moment. Please connect to a network and try again.");
                                break;
                            case 3:
                                //Billing unavailable for requested type
                                showAlert("Billing error","Billing API is not supported for the requested type");
                                break;
                            case 4:
                                //requested item not available
                                showAlert("Unavailable","The requested item is not available. Please try again in a few minutes. An error has been logged.");
                                FirebaseCrash.log("Item unavailable code received (4) during billing attempt.");
                                break;
                            case 5:
                                //developer error
                                showAlert("Oops!","Something went wrong during the billing attempt. Please try again in a few minutes. An error has been logged.");
                                FirebaseCrash.log("Developer error (5) during billing.");
                                //log firebase error
                                break;
                            case 6:
                                //general fatal error
                                showAlert("Oops!","Fatal error during the API call. This could mean Google is having " +
                                        "trouble accessing your payment info, or that the API call took too long. Please " +
                                        "try again in a few minutes, or email the developer at helwigdev@gmail.com if " +
                                        "you have seen this error more than once.");
                                break;
                            case 7:
                                //item already owned
                                Toast.makeText(getActivity(), getResources().getString(R.string.already_owned), Toast.LENGTH_LONG).show();
                                actionRestorePurchase();
                                break;
                            default:
                                //doesn't happen
                                Toast.makeText(getActivity(), "How did you get here? Logging error...", Toast.LENGTH_LONG).show();
                                FirebaseCrash.log("Default case selected during billing. Response code: " + buyIntentBundle.getInt("RESPONSE_CODE"));
                        }


                    } catch (RemoteException | IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    Toast.makeText(getActivity(), "Could not initialize billing service. :-(", Toast.LENGTH_SHORT).show();
                    return false;
                }


            }
        });

        restorePurchase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return actionRestorePurchase();
            }
        });

        //get IDs and display
        Preference firebaseID = new Preference(getActivity());
        firebaseID.setTitle(getResources().getString(R.string.firebase_id_title));
        if (fireId != null) {
            firebaseID.setSummary(fireId);
        } else {
            firebaseID.setSummary(getResources().getString(R.string.not_set));
        }

        firebaseID.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip = ClipData.newPlainText(fireId, fireId);
                myClipboard.setPrimaryClip(myClip);
                Log.d("Firebase ID", fireId);
                Toast.makeText(getActivity(), "Sent Firebase ID to clipboard & logcat", Toast.LENGTH_LONG).show();
                return true;
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
                ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip = ClipData.newPlainText(deviceId, deviceId);
                myClipboard.setPrimaryClip(myClip);
                Log.d("AdMob Device ID", deviceId);
                Toast.makeText(getActivity(), "Sent AdMob Device ID to clipboard & logcat", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        devInfo.addPreference(prefVersionName);
        devInfo.addPreference(prefVersionId);
        devInfo.addPreference(prefAdmobId);
        devInfo.addPreference(firebaseID);
    }

    private Boolean actionRestorePurchase() {
        if (mService != null) {
            try {
                //check purchases
                Bundle ownedItems = mService.getPurchases(3, getActivity().getPackageName(), "inapp", null);

                int response = ownedItems.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList<String> purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList<String> signatureList =
                            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    Boolean owned = false;
                    if (ownedSkus != null && purchaseDataList != null) {
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseData = purchaseDataList.get(i);
                            String sku = ownedSkus.get(i);
                            if (sku.equals(SKU_REMOVE_ADS)) {
                                //mark as purchased
                                //purchaseData is JSON data
                                //same as other response
                                Toast.makeText(getActivity(), "Restoring purchase!", Toast.LENGTH_LONG).show();
                                owned = true;
                                SettingsActivity parent = (SettingsActivity) getActivity();
                                parent.registerPurchase(purchaseData,true);

                            }

                        }
                    }
                    if (!owned) {
                        Toast.makeText(getActivity(), "No purchases found.", Toast.LENGTH_LONG).show();
                    }
                }


                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void showAlert(String title, String message){
        try {
            new AlertDialog.Builder(getActivity())
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
            FirebaseCrash.report(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mServiceConn != null) {
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
