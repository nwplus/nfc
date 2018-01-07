package io.nwhacks.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rice on 11/30/17.
 */

public class ReadFragment extends NFCFragment {
    public static final String ARG_OBJECT = "object";

    private List<String> arguments;
    private Spinner events;
    private CheckBox allowSeconds;
    private CheckBox allowUnlimited;
    private TextView recordDisplay;
    private TextView name;
    private TextView email;
    private TextView id;
    private View rootView;
    public static final int WARNING_COLOR = 0xFFff8000;
    public static final int ERROR_COLOR = 0xFFFF0000;
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        rootView = inflater.inflate(R.layout.read_fragment, container, false);
        events = rootView.findViewById(R.id.event_spinner);
        if (arguments != null) {
            this.setArguments(arguments);
        }
        recordDisplay = rootView.findViewById(R.id.recordDisplay);
        allowSeconds = rootView.findViewById(R.id.allowSeconds);
        allowUnlimited = rootView.findViewById(R.id.allowUnlimited);
        name = rootView.findViewById(R.id.name);
        email = rootView.findViewById(R.id.email);
        id = rootView.findViewById(R.id.id);
        return rootView;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
        if (events != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, arguments);
            events.setAdapter(adapter);
        }
    }

    public void loggedIn(FirebaseUser user) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("admin/events");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                arguments = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Event e = postSnapshot.getValue(Event.class);
                    arguments.add(e.name);
                }
                setArguments(arguments);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                MainActivity.toast(getContext(), databaseError.getMessage());
            }
        });
    }

    @Override
    public void tagDiscovered(NFCManager mgr, Intent intent) {
        ArrayList<String> records = mgr.readTagFromIntent(intent);

        if (records.size() == 0){
            MainActivity.toast(getContext(), "Tag is empty or not yet formatted.");
            setColor(ERROR_COLOR);
            resetDetailView();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tag records:\n");
        for (int i = 0; i<records.size(); i++) {
            sb.append(i);
            sb.append(". ");
            sb.append(records.get(i));
            sb.append("\n");
        }
        String body = sb.toString();
        recordDisplay.setText(body);

        if (records.size() > 0) {
            String id = records.get(0);
            this.id.setText(id);
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference("form/registration").child(id);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Hacker h = dataSnapshot.getValue(Hacker.class);
                    name.setText(h.first_name + " " + h.last_name);
                    email.setText(h.email);

                    String selectedEvent = formatEventName(events.getSelectedItem().toString());
                    for (DataSnapshot event : dataSnapshot.child("events").getChildren()) {
                        if (event.getKey().equals(selectedEvent)){
                            boolean result = onEventJoin(id, selectedEvent, Integer.valueOf(event.getValue().toString()));
                            if (result){
                                MainActivity.toast(getContext(),"Checked user into event!", 100);
                                setColor(DEFAULT_COLOR);
                            } else {
                                MainActivity.toast(getContext(), "User has already checked in!");
                                setColor(WARNING_COLOR);
                            }
                            return;
                        }
                    }
                    MainActivity.toast(getContext(),"Checked user into event for first time!", 100);
                    setColor(DEFAULT_COLOR);
                    onEventJoin(id, selectedEvent, 0);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    MainActivity.toast(getContext(), databaseError.getMessage());
                    setColor(ERROR_COLOR);
                    resetDetailView();
                }
            });
        }
    }

    private void resetDetailView() {
        name.setText("Name");
        email.setText("Email");
        id.setText("ID");
    }

    /* Write event attendance to participant in Firebase - returns true if user can join event */
    public Boolean onEventJoin(String id, String event_name, Integer checkInCount){
        if ( allowUnlimited.isChecked()
                || (checkInCount+1 == 2 && allowSeconds.isChecked())
                || checkInCount+1 < 2) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            db.getReference("form/registration/" + id + "/events/" + event_name).setValue(checkInCount + 1);
            return true;
        }
        return false;
    }

    public String formatEventName(String event_name){
        return event_name.replaceAll(" ", "_").toLowerCase();
    }

    private void setColor(int color) {
        rootView.setBackgroundColor(color);
    }
}
