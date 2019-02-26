package com.helwigdev.helpdesk.view

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.SettingsActivity
import com.helwigdev.helpdesk.controller.TicketController
import com.helwigdev.helpdesk.controller.TicketInterface
import com.helwigdev.helpdesk.model.Note
import com.helwigdev.helpdesk.model.Ticket
import kotlinx.android.synthetic.main.a_ticket_view.*
import kotlinx.android.synthetic.main.fragment_ticket_list.*
import kotlinx.android.synthetic.main.note_detail.view.*
import org.jetbrains.anko.doAsync
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds




class TicketView : AppCompatActivity(), TicketInterface {

    lateinit var ticketController: TicketController
    var ticketId: Int = -1
    private var menuRefresh: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_ticket_view)

        ticketId = intent.getIntExtra(ARG_TICKET_ID, -1)
        ticketController = TicketController(PreferenceManager.getDefaultSharedPreferences(this),this)

        setSupportActionBar(ticket_view_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        MobileAds.initialize(applicationContext, "ca-app-pub-5637328886369714~1187638383")
        if (!preferences.getBoolean(SettingsActivity.PREF_ADS_REMOVED, false)) {
            val adRequest = AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("4283C4B685567508FFF2525F7AB7B819")
                    .build()

            av_ticket_view_bottom.loadAd(adRequest)
        } else {
            //hide them
            av_ticket_view_bottom.visibility = View.GONE

        }


        startRefresh()
    }

    private fun startRefresh(){
        pb_ticket_view.visibility = View.VISIBLE
        if(menuRefresh != null){
            menuRefresh!!.isVisible = false
        }
        doAsync {
            ticketController.loadFullTicket(ticketId)
        }
    }

    private fun inflateTicket(ticket: Ticket){
        title = "Ticket " + ticket.id
        tv_ticket_view_id.text = ticket.id.toString()
        tv_ticket_view_client_fullname.text = ticket.displayClient

        var formatter = SimpleDateFormat("MMM dd yyyy 'at' hh:mm aaa")
        tv_ticket_view_last_updated.text = "Updated ${formatter.format(ticket.lastUpdated)}"

        tv_ticket_view_assigned_tech.text = ticket.clientTech?.displayName
        tv_ticket_view_subject.text = ticket.subject

        formatter = SimpleDateFormat("MMM dd 'at' hh:mm aaa")
        tv_ticket_view_due_date.text = "Due\n${formatter.format(ticket.displayDueDate)}"
        tv_ticket_view_details.text = Html.fromHtml(ticket.detail)

        if(ticket.notes != null) {
            ll_note_content.removeAllViews()
            for (i in ticket.notes.indices){
                val note: Note = ticket.notes[i]
                val noteView = LayoutInflater.from(this).inflate(R.layout.note_detail, ticket_view_parent, false)

                if(note.isSolution != null && note.isSolution){
                    noteView.tv_note_detail_resolution.visibility = View.VISIBLE
                } else {
                    noteView.tv_note_detail_resolution.visibility = View.GONE
                }

                noteView.tv_note_detail_pretty_last_updated.text = Html.fromHtml(note.prettyUpdatedString)
                noteView.tv_note_detail_detail.text = Html.fromHtml(note.mobileNoteText)

                ll_note_content.addView(noteView)
                Log.d("TicketView","Added note view for note id " + note.id)

            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_dv_refresh -> {
                startRefresh()
                true
            }
            R.id.menu_feedback -> {
                val intent = Intent(Intent.ACTION_VIEW)
                val data = Uri.parse("mailto:helwigdev@gmail.com?subject=Feedback for Web Help Desk app")
                return try {
                    intent.data = data
                    startActivity(intent)
                    true
                } catch (e: Exception){
                    Log.e("Feedback error ","something went wrong",e)
                    Crashlytics.logException(e)
                    false
                }
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_drawer_view, menu)
        menuRefresh = menu?.findItem(R.id.menu_dv_refresh)
        return true
    }

    override fun ticketRefreshResult(result: ArrayList<Ticket>) {
        //nothing here
    }

    override fun ticketError(error: String) {
        this.runOnUiThread {
            Log.d("TicketView", "Failed to load ticket: $error")
            if(menuRefresh != null){
                menuRefresh!!.isVisible = true
            }
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
            if(menuRefresh != null){
                menuRefresh!!.isVisible = true
            }
            pb_ticket_view.visibility = View.GONE
            inflateTicket(result)
        }
    }


    companion object {
        const val ARG_TICKET_ID = "ticket_id"
    }
}
