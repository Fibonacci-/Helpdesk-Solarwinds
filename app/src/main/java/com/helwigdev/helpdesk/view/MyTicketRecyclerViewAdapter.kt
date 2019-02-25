package com.helwigdev.helpdesk.view

import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.TextView
import com.helwigdev.helpdesk.model.Ticket


import com.helwigdev.helpdesk.view.TicketFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_ticket.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import com.helwigdev.helpdesk.R
import java.util.*




/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyTicketRecyclerViewAdapter(
        private var mValues: ArrayList<Ticket>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyTicketRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Ticket
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(com.helwigdev.helpdesk.R.layout.fragment_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = item.id.toString()
        holder.mClient.text = item.displayClient
        holder.mLastUpdated.text = item.prettyLastUpdated
        holder.mSubject.text = item.shortSubject
        holder.mDetails.text = item.shortDetail

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    fun updateData(data: ArrayList<Ticket>){
        mValues = data
        notifyDataSetChanged()
    }



    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.tv_tick_id
        val mClient: TextView = mView.tv_tick_client
        val mLastUpdated: TextView = mView.tv_tick_last_updated
        val mSubject: TextView = mView.tv_tick_subj
        val mDetails: TextView = mView.tv_tick_det

        override fun toString(): String {
            return super.toString() + " '" + mSubject.text + "'"
        }
    }
}
