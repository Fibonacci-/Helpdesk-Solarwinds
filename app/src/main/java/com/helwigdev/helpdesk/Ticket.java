package com.helwigdev.helpdesk;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by helwig on 10/14/2015.
 */
public class Ticket {
    public static final int TYPE_SHORT = 0;
    public static final int TYPE_LONG = 1;
    public static final int TYPE_UNKNOWN = 255;
    private static final String TAG = "Ticket";
    public static final String KEY_TICKET_ID = "key_ticket_id";
    public static final String KEY_TICKET_PRETTY_UPDATED = "key_ticket_pretty_updated";

    private int id;
    private String tType;
    private Date lastUpdated;
    private String shortSubject;
    private String shortDetail;
    private String longSubject;
    private String longDetail;
    private String displayClient;
    private int updateFlagType;
    private String prettyLastUpdated;
    private String createdBy;
    private ArrayList<TicketNote> notes;


    public Ticket(JSONObject o, int type) {
        notes = new ArrayList<>();
        try {
            switch (type) {
                case TYPE_SHORT:
                    updateTicketShort(o);
                    break;
                case TYPE_LONG:

                    break;
                case TYPE_UNKNOWN:

                default:
            }
        }catch (JSONException | ParseException e){
            e.printStackTrace();
        }

    }

    public int getTicketId() {
        return id;
    }


    public String gettType() {
        return tType;
    }


    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getShortSubject() {
        return shortSubject;
    }

    public String getShortDetail() {
        return shortDetail;
    }

    public String getDisplayClient() {
        return displayClient;
    }

    public int getUpdateFlagType() {
        return updateFlagType;
    }

    public String getLongSubject() {
        return longSubject;
    }

    public String getLongDetail() {
        return longDetail;
    }

    public String getPrettyLastUpdated() {
        return prettyLastUpdated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void addNote(JSONObject note, int type)throws JSONException{
        TicketNote toAdd = new TicketNote(note, type);
        for(TicketNote n : notes){
            if(n.getNoteId() == toAdd.getNoteId()) return;
        }
        notes.add(toAdd);
    }

    public ArrayList<TicketNote> getNotes(){
        return notes;
    }


    private void updateTicketShort(JSONObject o) throws JSONException, ParseException {
        //extract data
        this.id = o.getInt("id");
        this.tType = o.getString("type");
        String s = o.getString("lastUpdated");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.ENGLISH);
        lastUpdated = format.parse(s);
        this.shortSubject = o.getString("shortSubject");
        this.shortDetail = o.getString("shortDetail");
        this.displayClient = o.getString("displayClient");
        this.updateFlagType = o.getInt("updateFlagType");
        this.prettyLastUpdated = o.getString("prettyLastUpdated");

    }

    public void updateTicketLong(JSONObject o) throws JSONException, ParseException {
        //need: created by, full subj, full detail, notes
        Log.d(TAG, o.toString());
        this.createdBy = o.getJSONObject("clientReporter").getString("email");
        this.longSubject = o.getString("subject");
        this.longDetail = o.getString("detail");
        JSONArray ary = o.getJSONArray("notes");
        for(int i = 0; i < ary.length(); i++){
            addNote(ary.getJSONObject(i), TYPE_LONG);
        }
    }

}
