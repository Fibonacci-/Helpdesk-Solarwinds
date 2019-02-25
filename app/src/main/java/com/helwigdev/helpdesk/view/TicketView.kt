package com.helwigdev.helpdesk.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.controller.TicketController
import com.helwigdev.helpdesk.controller.TicketInterface
import com.helwigdev.helpdesk.model.Ticket
import kotlinx.android.synthetic.main.a_ticket_view.*
import kotlinx.android.synthetic.main.fragment_ticket_list.*
import org.jetbrains.anko.doAsync
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat


class TicketView : AppCompatActivity(), TicketInterface {

    lateinit var ticketController: TicketController
    var ticketId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_ticket_view)

        ticketId = intent.getIntExtra(ARG_TICKET_ID, -1)
        ticketController = TicketController(PreferenceManager.getDefaultSharedPreferences(this),this)
        pb_ticket_view.visibility = View.VISIBLE
        doAsync {
            ticketController.loadFullTicket(ticketId)
        }

        setSupportActionBar(ticket_view_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    fun inflateTicket(ticket: Ticket){
        title = "Ticket " + ticket.id
        tv_ticket_view_id.text = ticket.id.toString()
        tv_ticket_view_client_fullname.text = ticket.displayClient

        val formatter = SimpleDateFormat("MMM dd yyyy '\nat' hh:mm aaa")
        tv_ticket_view_last_updated.text = "Updated ${formatter.format(ticket.lastUpdated)}"

        tv_ticket_view_assigned_tech.text = ticket.clientTech?.displayName
        tv_ticket_view_subject.text = ticket.subject
        tv_ticket_view_due_date.text = "Due\n${formatter.format(ticket.displayDueDate)}"
        tv_ticket_view_details.text = Html.fromHtml(ticket.detail)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun ticketRefreshResult(result: ArrayList<Ticket>) {
        //nothing here
    }

    override fun ticketError(error: String) {
        this.runOnUiThread {
            Log.d("TicketView", "Failed to load ticket: $error")
            pb_ticket_view.visibility = View.GONE
            val snackbar: Snackbar = Snackbar.make(ticket_view_parent, error, Snackbar.LENGTH_INDEFINITE)
            val snackbarLayout = snackbar.view
            val sbTv: TextView = snackbarLayout.findViewById(android.support.design.R.id.snackbar_text)
            sbTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0)
            sbTv.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
            snackbar.show()
        }
    }

    override fun ticketLoadResult(result: Ticket) {
        Log.d("TicketView", "Got ticket result: $result")
        this.runOnUiThread {
            pb_ticket_view.visibility = View.GONE
            inflateTicket(result)
        }
    }


    companion object {
        const val ARG_TICKET_ID = "ticket_id"
    }
}
