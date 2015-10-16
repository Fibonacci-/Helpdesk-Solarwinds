package com.helwigdev.helpdesk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by helwig on 10/14/2015.
 */
public class TicketListActivity extends SingleFragmentActivity implements TicketListFragment.Callbacks{
    @Override
    protected Fragment createFragment() {
        return new TicketListFragment();
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
