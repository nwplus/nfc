package io.nwhacks.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    private TextView recordDisplay;
    private TextView name;
    private TextView email;
    private TextView id;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.read_fragment, container, false);
        events = rootView.findViewById(R.id.event_spinner);
        if (arguments != null) {
            this.setArguments(arguments);
        }
        recordDisplay = rootView.findViewById(R.id.recordDisplay);
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
        StringBuilder sb = new StringBuilder();
        sb.append("Tag records:\n");
        for (int i = 0; i<records.size(); i++) {
            sb.append(i);
            sb.append(". ");
            sb.append(records.get(i));
            sb.append("\n");
        }
        String body = sb.toString();
        MainActivity.toast(getContext(), "Read Tag!");
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
                            onEventJoin(id, selectedEvent, Integer.valueOf(event.getValue().toString()));
                            MainActivity.toast(getContext(),"Checked user into event!");
                            return;
                        }
                    }
                    onEventJoin(id, selectedEvent, 0);
                    MainActivity.toast(getContext(),"Checked user into event for first time!");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    MainActivity.toast(getContext(), databaseError.getMessage());
                }
            });
        }
    }

    /* Write event attendance to participant in Firebase */
    public void onEventJoin(String id, String event_name, Integer checkInCount){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference("form/registration/" + id + "/events/" + event_name).setValue(checkInCount+1);
    }

    public String formatEventName(String event_name){
        return event_name.replaceAll(" ", "_").toLowerCase();
    }
}
