package com.helwigdev.helpdesk.view

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.controller.TicketController
import com.helwigdev.helpdesk.controller.TicketInterface
import com.helwigdev.helpdesk.model.Ticket
import kotlinx.android.synthetic.main.a_main_drawer.*
import kotlinx.android.synthetic.main.fragment_ticket_list.*
import org.jetbrains.anko.doAsync

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [TicketFragment.OnListFragmentInteractionListener] interface.
 */
class TicketFragment : Fragment(), TicketInterface {
    override fun ticketError(error: String) {
        activity?.runOnUiThread {
            listener?.setProgressVisibility(false)
            Log.d("TicketFragment", "Failed to load ticket: $error")

            val snackbar: Snackbar = Snackbar.make(list, error, Snackbar.LENGTH_LONG)
            val snackbarLayout = snackbar.view
            val sbTv: TextView = snackbarLayout.findViewById(android.support.design.R.id.snackbar_text)
            sbTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error, 0, 0, 0)
            sbTv.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
            snackbar.show()
        }
    }

    override fun ticketLoadResult(result: Ticket) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var ticketType = 1
    private var listener: OnListFragmentInteractionListener? = null

    private var ticketArray: ArrayList<Ticket> = ArrayList()

    private lateinit var tController: TicketController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            ticketType = it.getInt(ARG_TICKET_TYPE)
        }

        tController = TicketController(PreferenceManager.getDefaultSharedPreferences(this.context), this)
        startTicketRefresh()
    }

    fun startTicketRefresh(){
        listener?.setProgressVisibility(true)
        if(this.view != null) {
            with(this.view as RecyclerView) {
                with(adapter as MyTicketRecyclerViewAdapter) {
                    updateData(ArrayList<Ticket>())
                    Log.d("TicketFragment", "Updated data in adapter")
                }
            }
        }
        when(ticketType){
            TYPE_MY -> doAsync { tController.refreshMyTickets() }
            TYPE_GROUP -> doAsync { tController.refreshGroupTickets() }
            TYPE_FLAGGED -> doAsync { tController.refreshFlaggedTickets() }
            TYPE_RECENT -> doAsync { tController.refreshRecentTickets() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_ticket_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)

                adapter = MyTicketRecyclerViewAdapter(ticketArray, listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun ticketRefreshResult(result: ArrayList<Ticket>) {
        activity?.runOnUiThread {
            listener?.setProgressVisibility(false)
            with(this.view as RecyclerView){
                with(adapter as MyTicketRecyclerViewAdapter){
                    updateData(result)
                    Log.d("TicketFragment","Updated data in adapter")
                }
            }
        }
        this.ticketArray = result

        Log.d("TicketFragment","Got result with num items: " + result.size)
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(ticket: Ticket?)
        fun setProgressVisibility(visible: Boolean)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_TICKET_TYPE = "ticket-type"

        const val TYPE_MY = 1
        const val TYPE_GROUP = 2
        const val TYPE_FLAGGED = 3
        const val TYPE_RECENT = 4

        @JvmStatic
        fun newInstance(ticketListType: Int) =
                TicketFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_TICKET_TYPE, ticketListType)
                    }
                }
    }
}
