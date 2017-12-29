package io.nwhacks.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.github.slugify.Slugify;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rice on 11/30/17.
 */

public class EventsFragment extends NFCFragment {
    public static final String ARG_OBJECT = "object";

    private List<String> arguments;
    private Spinner events;
    private EditText eventName;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.events_fragment, container, false);
        events = rootView.findViewById(R.id.event_spinner);
        if (arguments != null) {
            this.setArguments(arguments);
        }
        eventName = rootView.findViewById(R.id.eventName);
        rootView.findViewById(R.id.addEvent).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addEvent(v);
            }
        });

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

            }
        });
    }

    public void addEvent(View view) {
        String name = eventName.getText().toString();
        Slugify slg = new Slugify();
        String id = slg.slugify(name);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("admin/events").child(id);
        Event e = new Event();
        e.name = name;
        ref.setValue(e);
        eventName.getText().clear();
        MainActivity.toast(getContext(), "Added event: "+name);
    }

    @Override
    public void tagDiscovered(NFCManager mgr, Intent intent) {
    }
}
