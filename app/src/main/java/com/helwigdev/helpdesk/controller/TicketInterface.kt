package com.helwigdev.helpdesk.controller

import com.helwigdev.helpdesk.model.Ticket

interface TicketInterface {
    fun ticketRefreshResult(result: ArrayList<Ticket>)
    fun ticketError(error: String)
    fun ticketLoadResult(result: Ticket)
}