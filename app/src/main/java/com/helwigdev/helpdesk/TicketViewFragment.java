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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by helwig on 10/15/2015.
 */
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
    SharedPreferences preferences;
    RNInterface netInterface = this;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int ticketId = getActivity().getIntent().getIntExtra(Ticket.KEY_TICKET_ID, -1);
        if (ticketId == -1) {
            Toast.makeText(getActivity(), "Internal error", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mTicket = TicketSingleton.getInstance().getTicketById(ticketId);

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
                                    tick.put("type", mTicket.gettType());
                                    tick.put("id", mTicket.getTicketId());
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
        tvId.setText(String.valueOf(mTicket.getTicketId()));
        tvClient.setText("Client: " + mTicket.getDisplayClient());
        tvPrettyUpdated.setText(mTicket.getPrettyLastUpdated());
        tvUpdated.setText(mTicket.getLastUpdated().toString());
        tvSubject.setText(mTicket.getShortSubject());
        tvDetails.setText(mTicket.getShortDetail());

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
                String.valueOf(mTicket.getTicketId()) +
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
                    mTicket.updateTicketLong(o);
                    //need: created by, full subj, full detail, notes
                    tvCreatedBy.setVisibility(View.VISIBLE);
                    String s = "Created by: " + mTicket.getCreatedBy();
                    tvCreatedBy.setText(s);
                    tvSubject.setText(mTicket.getLongSubject());
                    tvDetails.setText(Html.fromHtml(mTicket.getLongDetail()));

                    //add notes

                    ListView listView = new ListView(getActivity());
                    NoteArrayAdapter adapter = new NoteArrayAdapter(mTicket.getNotes());
                    int totalHeight = 0;

                    for (int i = 0; i < adapter.getCount(); i++) {
                        View mView = adapter.getView(i, null, listView);

                        mView.measure(
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                        totalHeight += mView.getMeasuredHeight();
                        //Log.w("HEIGHT" + i, String.valueOf(totalHeight));

                    }

                    listView.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, totalHeight
                            + (listView.getDividerHeight() * (adapter.getCount() - 1))));
                    listView.setDividerHeight(0);
                    listView.setAdapter(adapter);
                    llNotes.addView(listView);

                    if (mTicket.getNotes().size() == 0) {
                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        View v = inflater.inflate(R.layout.part_note, llNotes, true);
                        TextView tvNote = (TextView) v.findViewById(R.id.tv_note_text);
                        tvNote.setText(getResources().getString(R.string.no_notes));

                    }
                    pbNoteLoader.setVisibility(View.GONE);

                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
                break;
            case TASK_NOTES:

                break;
            case TASK_NEW_NOTE:
                //process data returned
                //init ticket refresh
                Log.d("New Note:::::::", output);
                getTicketDetails();
                break;
        }
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
            tvNoteText.setText(note.getNoteText());

            return convertView;
        }
    }
}
