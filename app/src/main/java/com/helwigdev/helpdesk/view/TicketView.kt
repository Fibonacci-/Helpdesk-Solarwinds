package com.helwigdev.helpdesk.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.TextView
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.controller.TicketController
import com.helwigdev.helpdesk.controller.TicketInterface
import com.helwigdev.helpdesk.model.Ticket
import kotlinx.android.synthetic.main.a_ticket_view.*
import kotlinx.android.synthetic.main.fragment_ticket_list.*
import org.jetbrains.anko.doAsync

class TicketView : AppCompatActivity(), TicketInterface {

    lateinit var ticketController: TicketController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_ticket_view)

        val ticketId = intent.getIntExtra(ARG_TICKET_ID, -1)
        ticketController = TicketController(PreferenceManager.getDefaultSharedPreferences(this),this)
        doAsync {
            ticketController.loadFullTicket(ticketId)
        }


    }

    override fun ticketRefreshResult(result: ArrayList<Ticket>) {
        //nothing here
    }

    override fun ticketError(error: String) {
        this.runOnUiThread {
            Log.d("TicketView", "Failed to load ticket: $error")

            val snackbar: Snackbar = Snackbar.make(ticket_view_parent, error, Snackbar.LENGTH_LONG)
            val snackbarLayout = snackbar.view
            val sbTv: TextView = snackbarLayout.findViewById(android.support.design.R.id.snackbar_text)
            sbTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0)
            sbTv.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
            snackbar.show()
        }
    }

    override fun ticketLoadResult(result: Ticket) {
        Log.d("TicketView", "Got ticket result: $result")
    }


    companion object {
        const val ARG_TICKET_ID = "ticket_id"
    }
}
