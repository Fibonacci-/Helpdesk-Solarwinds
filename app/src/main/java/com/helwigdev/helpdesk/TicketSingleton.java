package com.helwigdev.helpdesk;

import java.util.ArrayList;

/**
 * Created by helwig on 10/14/2015.
 */
public class TicketSingleton {

    private static TicketSingleton instance;
    private ArrayList<Ticket> tickets;

    private TicketSingleton(){
        tickets = new ArrayList<>();
    }

    public static TicketSingleton getInstance(){
        if(instance == null){
            instance = new TicketSingleton();
        }
        return instance;
    }

    public Ticket getTicketById(int id){
        for (Ticket t: tickets) {
            if(t.getTicketId() == id){
                return t;
            }
        }
        return null;
    }

    public void clear(){
        tickets = new ArrayList<>();
    }

    public void addTicket(Ticket ticket){
        if(getTicketById(ticket.getTicketId()) == null) tickets.add(ticket);

    }

    public ArrayList<Ticket> getTickets(){
        return tickets;
    }
}
