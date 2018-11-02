package com.helwigdev.helpdesk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by helwig on 10/15/2015.
 */
public class TicketViewActivity extends SingleFragmentActivity {
    SharedPreferences preferences;

    @Override
    protected Fragment createFragment() {
        return new TicketViewFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5637328886369714~1187638383");

        AdView mAdView = (AdView) findViewById(R.id.av_ticket_view_bottom);
        if(!preferences.getBoolean(SettingsActivity.PREF_ADS_REMOVED, false)) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("E498D046420E068963DD7607B804BA3D")
                    .addTestDevice("960E1155E3858B01540E73FBD53DB405")
                    .build();

            mAdView.loadAd(adRequest);
        } else {
            //hide them
            mAdView.setVisibility(View.GONE);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_full_ticket, menu);
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_ticket_view;

    }
}
