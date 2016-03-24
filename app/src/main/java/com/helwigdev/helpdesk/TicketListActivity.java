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

/**
 * Created by helwig on 10/14/2015.
 */
public class TicketListActivity extends SingleFragmentActivity implements TicketListFragment.Callbacks{
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ticketlist, menu);
        return true;
    }

    @Override
    protected Fragment createFragment() {
        return new TicketListFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search:
                //todo search things
                Toast.makeText(TicketListActivity.this, "Searching for something la de da", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_settings:
                //todo settings things
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected int getLayoutResId() {
        return super.getLayoutResId();

    }

    @Override
    public void onTicketSelected(Ticket ticket) {
        Intent i = new Intent(this, TicketViewActivity.class);
        i.putExtra(Ticket.KEY_TICKET_ID, ticket.getTicketId());

        startActivity(i);

    }
}
