package com.helwigdev.helpdesk.view

import android.app.Dialog
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
import android.widget.ArrayAdapter
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.SettingsActivity
import com.helwigdev.helpdesk.controller.TicketController
import com.helwigdev.helpdesk.controller.TicketInterface
import kotlinx.android.synthetic.main.a_ticket_view.*
import kotlinx.android.synthetic.main.fragment_ticket_list.*
import kotlinx.android.synthetic.main.note_detail.view.*
import org.jetbrains.anko.doAsync
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.helwigdev.helpdesk.controller.NoteInterface
import com.helwigdev.helpdesk.model.*
import kotlinx.android.synthetic.main.dialog_note_layout.*


class TicketView : AppCompatActivity(), TicketInterface, NoteInterface {


    lateinit var ticketController: TicketController
    private var ticketId: Int = -1
    private var ticket: Ticket? = null
    private var menuRefresh: MenuItem? = null
    private var menuNewNote: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_ticket_view)

        ticketId = intent.getIntExtra(ARG_TICKET_ID, -1)
        ticketController = TicketController(PreferenceManager.getDefaultSharedPreferences(this),this)

        setSupportActionBar(ticket_view_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        startRefresh()
    }

    override fun onResume() {
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
        super.onResume()
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
        tv_ticket_view_id.visibility = View.VISIBLE
        tv_ticket_view_id.text = ticket.id.toString()

        if(ticket.displayClient != null) {
            tv_ticket_view_client_fullname.visibility = View.VISIBLE
            tv_ticket_view_client_fullname.text = ticket.displayClient
        }

        var formatter = SimpleDateFormat("MMM dd yyyy 'at' hh:mm aaa")
        if(ticket.lastUpdated != null) {
            tv_ticket_view_last_updated.visibility = View.VISIBLE
            tv_ticket_view_last_updated.text = "Updated ${formatter.format(ticket.lastUpdated)}"
        }

        if(ticket.clientTech?.displayName != null) {
            tv_ticket_view_assigned_tech.visibility = View.VISIBLE
            tv_ticket_view_assigned_tech.text = "Assigned to ${ticket.clientTech?.displayName}"
        }

        if(ticket.subject != null) {
            tv_ticket_view_subject.visibility = View.VISIBLE
            tv_ticket_view_subject.text = ticket.subject
        }

        formatter = SimpleDateFormat("MMM dd 'at' hh:mm aaa")
        if(ticket.displayDueDate != null) {
            tv_ticket_view_due_date.visibility = View.VISIBLE
            tv_ticket_view_due_date.text = "Due\n${formatter.format(ticket.displayDueDate)}"
        }
        v_ticket_view_line2.visibility = View.VISIBLE

        if(ticket.detail != null) {
            tv_ticket_view_details.visibility = View.VISIBLE
            tv_ticket_view_details.text = Html.fromHtml(ticket.detail)
        }

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
            R.id.menu_add_note -> {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog_note_layout)
                dialog.setTitle(resources.getString(R.string.new_note))

                val statuses = ticket?.enabledStatusTypes
                val adapter: ArrayAdapter<StatusType> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statuses)

                dialog.spin_status.adapter = adapter
                var initialPosition = -1
                for(i in 0..(adapter.count - 1)){
                    val s = adapter.getItem(i)
                    if(s.statusTypeName == ticket?.statusType?.statusTypeName){
                        initialPosition = i
                    }
                }
                dialog.spin_status.setSelection(initialPosition)

                if(ticket?.bccAddresses != null){
                    dialog.et_bcc_list.setText(ticket?.bccAddresses)
                }

                dialog.cb_email_tech_grouplevel.isChecked = ticket!!.emailTechGroupLevel
                dialog.cb_email_bcc.isChecked = ticket!!.emailBcc
                dialog.cb_email_groupmanager.isChecked = ticket!!.emailGroupManager
                dialog.cb_email_cc.isChecked = ticket!!.emailCc
                dialog.cb_email_client.isChecked = ticket!!.emailClient
                dialog.cb_email_tech.isChecked = ticket!!.emailTech

                dialog.b_note_cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.b_note_ok.setOnClickListener {
                    val selectedStatus = dialog.spin_status.selectedItem as StatusType
                    var bccList: String? = dialog.et_bcc_list.text.toString()
                    var minutesWorked = "0"

                    if(dialog.et_minutes_worked.text.toString() != ""){
                        minutesWorked = dialog.et_minutes_worked.text.toString()
                    }
                    if(dialog.et_bcc_list.text.toString() == ""){
                        bccList = null
                    }
                    val note = FullNote(
                            bccList,
                            dialog.cb_email_bcc.isChecked,
                            dialog.cb_email_cc.isChecked,
                            dialog.cb_email_client.isChecked,
                            dialog.cb_email_groupmanager.isChecked,
                            dialog.cb_email_tech.isChecked,
                            dialog.cb_email_tech_grouplevel.isChecked,
                            dialog.cb_hide_note.isChecked,
                            dialog.cb_is_solution.isChecked,
                            JobTicket(ticketId),
                            dialog.et_note_text.text.toString(),
                            selectedStatus.id,
                            minutesWorked
                    )
                    Log.d("TicketView","Submitting note $note")
                    pb_ticket_view.visibility = View.VISIBLE
                    if(menuRefresh != null){
                        menuRefresh!!.isVisible = false
                    }
                    val noteInterface = this
                    doAsync {
                        ticketController.sendNote(noteInterface, note)
                    }
                    dialog.dismiss()
                }

                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_drawer_view, menu)
        menuInflater.inflate(R.menu.menu_ticket_view, menu)
        menuRefresh = menu?.findItem(R.id.menu_dv_refresh)
        menuNewNote = menu?.findItem(R.id.menu_add_note)
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
        this.ticket = result
        this.runOnUiThread {
            if(menuRefresh != null){
                menuRefresh!!.isVisible = true
            }
            if(menuNewNote != null){
                menuNewNote!!.isVisible = true
            }
            pb_ticket_view.visibility = View.GONE
            inflateTicket(result)
        }
    }

    override fun noteSendResult() {
        this.runOnUiThread {
            Log.d("TicketView","Got note result OK")
            this.startRefresh()
        }
    }

    override fun noteSendError(error: String) {
        this.runOnUiThread {
            Log.d("TicketView", "Failed to send note: $error")
            if(menuRefresh != null){
                menuRefresh!!.isVisible = true
            }
            pb_ticket_view.visibility = View.GONE
            val snackbar: Snackbar = Snackbar.make(ticket_view_parent, "Failed to add note: $error", Snackbar.LENGTH_INDEFINITE)
            val snackbarLayout = snackbar.view
            val sbTv: TextView = snackbarLayout.findViewById(android.support.design.R.id.snackbar_text)
            sbTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0)
            sbTv.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
            snackbar.show()
        }
    }


    companion object {
        const val ARG_TICKET_ID = "ticket_id"
    }
}
