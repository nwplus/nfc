package io.nwhacks.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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
                addEvent();
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("nfc_events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    arguments = new ArrayList<>();
                    for(DocumentSnapshot eventDoc : docs){
                        Event event = eventDoc.toObject(Event.class);
                        arguments.add(event.name);
                    }
                    setArguments(arguments);
                }
            }
        });
    }

    public void addEvent() {
        String name = eventName.getText().toString();
        if (name.length() == 0) {
            MainActivity.toast(getContext(), "Invalid event name");
            return;
        }
        Event e = new Event();
        e.name = name;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("nfc_events").document(name);
        eventRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getData() != null) {
                    MainActivity.toast(getContext(), name+" already exists");
                } else {
                    db.collection("nfc_events").document(name).set(e)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    eventName.getText().clear();
                                    MainActivity.toast(getContext(), "Added event: "+name);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MainActivity.toast(getContext(), "Failed to add event: "+name);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void tagDiscovered(NFCManager mgr, Intent intent) {
    }
}
