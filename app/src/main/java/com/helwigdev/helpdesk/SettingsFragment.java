package com.helwigdev.helpdesk;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by helwi on 10/15/2016.
 */

public class SettingsFragment extends PreferenceFragment {

    Preference removeAds;
    PreferenceCategory devInfo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String fireId = FirebaseInstanceId.getInstance().getToken();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_settings);
        removeAds = findPreference("key_pref_action_remove_ads");
        devInfo = (PreferenceCategory) findPreference("key_pref_info");

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

        devInfo.addPreference(firebaseID);
    }
}
