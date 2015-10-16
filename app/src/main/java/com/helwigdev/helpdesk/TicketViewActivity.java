package com.helwigdev.helpdesk;

import android.support.v4.app.Fragment;

/**
 * Created by helwig on 10/15/2015.
 */
public class TicketViewActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new TicketViewFragment();
    }
}
