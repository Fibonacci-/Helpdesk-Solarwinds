package com.helwigdev.helpdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.method.CharacterPickerDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by helwig on 10/14/2015.
 */
public class TicketListActivity extends SingleFragmentActivity implements TicketListFragment.Callbacks{
    //disable menu until settings || search are finished
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ticketlist, menu);
        return true;
    }*/

    @Override
    protected Fragment createFragment() {
        return new TicketListFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5637328886369714~1187638383");

        AdView mAdView = (AdView) findViewById(R.id.av_tickets_bottom);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search:
                //todo search things
                Toast.makeText(TicketListActivity.this, "Not yet implemented", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_settings:
                //todo settings things
                startActivity(new Intent(this,SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.ticket_list_activity;

    }

    @Override
    public void onTicketSelected(Ticket ticket) {
        Intent i = new Intent(this, TicketViewActivity.class);
        i.putExtra(Ticket.KEY_TICKET_ID, ticket.getTicketId());

        startActivity(i);

    }
}
