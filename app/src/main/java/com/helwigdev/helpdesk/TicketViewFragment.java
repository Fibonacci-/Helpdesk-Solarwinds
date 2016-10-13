package com.helwigdev.helpdesk;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class TicketViewFragment extends Fragment implements RNInterface {

    protected static final int TASK_FULL_TICKET = 100;
    protected static final int TASK_NOTES = 101;
    protected static final int TASK_NEW_NOTE = 102;
    private static final String TAG = "TicketViewFragment";

    Ticket mTicket;

    TextView tvId, tvClient, tvPrettyUpdated, tvUpdated, tvSubject, tvCreatedBy, tvDetails;
    Button bNewNote;
    ProgressBar pbNoteLoader;
    LinearLayout llNotes;
    ScrollView svTicketScroll;
    SharedPreferences preferences;
    RNInterface netInterface = this;
    ListView listView;
    RelativeLayout rlTicketRelative;
    int ticketId;
    String ticketType = "Ticket";
    String prettyLastUpdated;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ticketId = getActivity().getIntent().getIntExtra(Ticket.KEY_TICKET_ID, -1);
        prettyLastUpdated = getActivity().getIntent().getStringExtra(Ticket.KEY_TICKET_PRETTY_UPDATED);
        if (ticketId == -1) {
            Toast.makeText(getActivity(), "Internal error", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //mTicket = TicketSingleton.getInstance().getTicketById(ticketId);

        View v = inflater.inflate(R.layout.full_ticket, container, false);


        tvId = (TextView) v.findViewById(R.id.tv_full_id);
        tvClient = (TextView) v.findViewById(R.id.tv_full_client);
        tvPrettyUpdated = (TextView) v.findViewById(R.id.tv_full_pretty_updated);
        tvUpdated = (TextView) v.findViewById(R.id.tv_full_updated);
        tvSubject = (TextView) v.findViewById(R.id.tv_full_subj);
        tvCreatedBy = (TextView) v.findViewById(R.id.tv_full_created_by);
        tvDetails = (TextView) v.findViewById(R.id.tv_full_details);
        bNewNote = (Button) v.findViewById(R.id.b_full_new_note);
        pbNoteLoader = (ProgressBar) v.findViewById(R.id.pb_full_note_loader);
        llNotes = (LinearLayout) v.findViewById(R.id.ll_full_notes);
        svTicketScroll = (ScrollView) v.findViewById(R.id.sv_ticket_scrollview);
        rlTicketRelative = (RelativeLayout) v.findViewById(R.id.rl_ticket_relative);

        bNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater

                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View vDia = inflater.inflate(R.layout.dialog_note, null);

                builder.setView(vDia)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText etDetails = (EditText) vDia.findViewById(R.id.et_dia_note);
                                EditText etTime = (EditText) vDia.findViewById(R.id.et_dia_time);
                                CheckBox cbVisible, cbEmailTech, cbEmailClient;
                                cbVisible = (CheckBox) vDia.findViewById(R.id.cb_is_visible);
                                cbEmailTech = (CheckBox) vDia.findViewById(R.id.cb_dia_email_tech);
                                cbEmailClient = (CheckBox) vDia.findViewById(R.id.cb_dia_email_client);



                                if (etDetails.getText().toString().equals("") ||
                                        etTime.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "All fields must be filled", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                try {
                                    JSONObject o = new JSONObject();
                                    o.put("noteText", etDetails.getText().toString());
                                    JSONObject tick = new JSONObject();
                                    tick.put("type", ticketType);
                                    tick.put("id", ticketId);
                                    o.put("jobticket", tick);
                                    o.put("worktime", etTime.getText().toString());
                                    o.put("isHidden", !cbVisible.isChecked());
                                    o.put("isSolution", false);//TODO
                                    o.put("emailClient", cbEmailClient.isChecked());
                                    o.put("emailTech", cbEmailTech.isChecked());

                                    String sUrl = "http://" +
                                            preferences.getString(Init.PREF_SERVER, "") +
                                            "/helpdesk/WebObjects/Helpdesk.woa/ra/TechNotes?sessionKey=" +
                                            preferences.getString(Init.PREF_SESSION_KEY, "");

                                    new WriteNetwork(TASK_NEW_NOTE, o.toString(), netInterface,
                                            true, preferences.getString(Init.PREF_COOKIE, "")).execute(new URL(sUrl));

                                } catch (JSONException | MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            }
        });

        pbNoteLoader.setVisibility(View.VISIBLE);

        //find and insert ticket details from cached views
//        tvId.setText(String.valueOf(mTicket.getTicketId()));
//        tvClient.setText("Client: " + mTicket.getDisplayClient());
//        tvPrettyUpdated.setText(mTicket.getPrettyLastUpdated());
//        tvUpdated.setText(mTicket.getLastUpdated().toString());
//        tvSubject.setText(mTicket.getShortSubject());
//        tvDetails.setText(mTicket.getShortDetail());

        tvSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvDetails.getVisibility() == View.VISIBLE) {
                    tvDetails.setVisibility(View.GONE);
                } else {
                    tvDetails.setVisibility(View.VISIBLE);
                }
            }
        });

        //get deets from network
        getTicketDetails();

        return v;
    }


    private void getTicketDetails() {
        //curl "http://localhost:8081/helpdesk/WebObjects/Helpdesk.woa\
        //        > /ra/Tickets/1\
        //> ?apiKey=OdWPct19cIZbGIbVJkarpbrIvvx561tErxx87l3l"

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sUrl = "http://" +
                preferences.getString(Init.PREF_SERVER, "") +
                "/helpdesk/WebObjects/Helpdesk.woa/ra/Tickets/" +
                ticketId +
                "?sessionKey=" +
                preferences.getString(Init.PREF_SESSION_KEY, "");

        try {
            new ReadNetwork(TASK_FULL_TICKET, this, true, preferences.getString(Init.PREF_COOKIE, "")).execute(new URL(sUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void processResult(String output, int taskID) {
        switch (taskID) {
            case TASK_FULL_TICKET:
                //expect json object
                try {
                    JSONObject o = new JSONObject(output);
                    //mTicket.updateTicketLong(o);
                    //need: created by, full subj, full detail, notes
                    tvCreatedBy.setVisibility(View.VISIBLE);
                    String s = "Created by: " + o.getJSONObject("clientReporter").getString("email");
                    tvCreatedBy.setText(s);
                    tvSubject.setText(o.getString("subject"));
                    tvDetails.setText(Html.fromHtml(o.getString("detail")));
                    ticketType = o.getString("type");

                    tvId.setText(String.valueOf(ticketId));
                    String stvClient = "Client: " + o.getJSONObject("clientReporter").getString("firstName") + " " + o.getJSONObject("clientReporter").getString("lastName");
                    tvClient.setText(stvClient);
                    tvPrettyUpdated.setText(prettyLastUpdated);
                    String sUpdated = o.getString("lastUpdated");
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.ENGLISH);
                    Date lastUpdated = format.parse(sUpdated);
                    tvUpdated.setText(lastUpdated.toString());
//                    tvSubject.setText(mTicket.getShortSubject());
//                    tvDetails.setText(mTicket.getShortDetail());

                    //add notes

                    try{
                        llNotes.removeView(listView);


                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    listView = new ListView(getActivity());
                    listView.setOnTouchListener(new ListView.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            int action = event.getAction();
                            switch (action) {
                                case MotionEvent.ACTION_DOWN:
                                    // Disallow ScrollView to intercept touch events.
                                    v.getParent().requestDisallowInterceptTouchEvent(true);
                                    break;

                                case MotionEvent.ACTION_UP:

                                    // Allow ScrollView to intercept touch events.

                                    v.getParent().requestDisallowInterceptTouchEvent(false);

                                    break;
                            }

                            // Handle ListView touch events.
                            v.onTouchEvent(event);
                            return true;
                        }
                    });

                    ArrayList<TicketNote> notes = new ArrayList<>();
                    JSONArray jsonNotes = o.getJSONArray("notes");
                    for(int i = 0; i < jsonNotes.length(); i++){
                        notes.add(new TicketNote(jsonNotes.getJSONObject(i), Ticket.TYPE_LONG));
                    }

                    NoteArrayAdapter adapter = new NoteArrayAdapter(notes);

                    //this expands the list view by a magic number so the note isn't hidden behind the ticket (wtf)
                    //listView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1500));//magic number is a dirty hack
                    listView.setDividerHeight(0);
                    listView.setAdapter(adapter);


                    llNotes.addView(listView);


                    if (notes.size() == 0) {
                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        View v = inflater.inflate(R.layout.part_note, llNotes, true);
                        TextView tvNote = (TextView) v.findViewById(R.id.tv_note_text);
                        tvNote.setText(getResources().getString(R.string.no_notes));

                    }
                    pbNoteLoader.setVisibility(View.GONE);

                    //refresh layout sizes
                    setListViewHeightBasedOnChildren(listView);

                    llNotes.requestLayout();
                    rlTicketRelative.requestLayout();
                    svTicketScroll.requestLayout();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case TASK_NOTES:

                break;
            case TASK_NEW_NOTE:
                //process data returned
                //init ticket refresh
                Log.d("New Note:::::::", output);
                try{
                    JSONObject o = new JSONObject(output);
                    if(o.has("stackTrace")){
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //do nothing
                                        break;

                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(o.getString("stackTrace"))
                                .setPositiveButton("OK", dialogClickListener)
                                .setTitle("Server error!")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                getTicketDetails();
                break;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void authErr(int type, int taskId) {
        switch (taskId) {
            case TASK_FULL_TICKET:
                switch (type) {
                    case 401:
                        //deauth
                        Toast.makeText(getActivity(), "Session expired. Please restart app", Toast.LENGTH_SHORT).show();

                        break;
                    case 403:
                        //not allowed
                        Toast.makeText(getActivity(), "You are not allowed to do that.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity(), "Something broke: " + type, Toast.LENGTH_SHORT).show();
                }
                break;
            case TASK_NOTES:

                break;
            case TASK_NEW_NOTE:
                switch (type) {
                    case 401:
                        //deauth
                        Toast.makeText(getActivity(), "New note : Session expired. Please restart app", Toast.LENGTH_SHORT).show();

                        break;
                    case 403:
                        //not allowed
                        Toast.makeText(getActivity(), "New note : You are not allowed to do that.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity(), "New note : Something broke: " + type, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void setCookie(String cookie) {
        //ignore
    }

    @Override
    public void onPause() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        super.onPause();
    }

    private class NoteArrayAdapter extends ArrayAdapter<TicketNote> {

        public NoteArrayAdapter(ArrayList<TicketNote> notes) {
            super(getActivity(), 0, notes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.part_note, null);
            }
            TicketNote note = getItem(position);

            TextView tvNoteText = (TextView) convertView.findViewById(R.id.tv_note_text);
            TextView tvNotePrettyUpdated = (TextView) convertView.findViewById(R.id.tv_note_pretty_updated);
            tvNoteText.setText(Html.fromHtml(note.getNoteText()));
            tvNotePrettyUpdated.setText(Html.fromHtml(note.getPrettyUpdated()));

            return convertView;
        }
    }
}
