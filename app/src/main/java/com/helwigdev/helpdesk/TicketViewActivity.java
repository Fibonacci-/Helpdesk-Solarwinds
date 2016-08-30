package com.helwigdev.helpdesk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by helwig on 10/15/2015.
 */
public class TicketViewActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new TicketViewFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5637328886369714~1187638383");

        AdView mAdView = (AdView) findViewById(R.id.av_ticket_view_bottom);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mAdView.loadAd(adRequest);
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
