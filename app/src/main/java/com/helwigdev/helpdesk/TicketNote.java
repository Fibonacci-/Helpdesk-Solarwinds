package com.helwigdev.helpdesk;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by helwig on 10/14/2015.
 */
public class TicketNote {
    private int id;
    private String nType;
    private Boolean isSolution;
    private String prettyUpdated;
    private String noteText;
    private String isTechNote;
    private String isHidden;
    private String workTime;

    public TicketNote(JSONObject o, int Type){
        try {
            switch (Type) {
                case Ticket.TYPE_SHORT:
                    this.id = o.getInt("id");
                    this.nType = o.getString("type");
                    this.noteText = o.getString("mobileListText");
                    //don't care about note colors yet
                    break;
                case Ticket.TYPE_LONG:
                    this.id = o.getInt("id");
                    this.nType = o.getString("type");
                    this.noteText = o.getString("mobileNoteText");
                    this.prettyUpdated = o.getString("prettyUpdatedString");
                default:
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public int getNoteId() {
        return id;
    }

    public String getnType() {
        return nType;
    }

    public Boolean getIsSolution() {
        return isSolution;
    }

    public String getPrettyUpdated() {
        return prettyUpdated;
    }

    public String getNoteText() {
        return noteText;
    }

    public String getIsTechNote() {
        return isTechNote;
    }

    public String getIsHidden() {
        return isHidden;
    }

    public String getWorkTime() {
        return workTime;
    }
}
