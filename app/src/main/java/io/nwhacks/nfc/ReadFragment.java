package io.nwhacks.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

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
    public static final String EVENT_DROPDOWN_PLACEHOLDER = "Select an event...";

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("nfc_events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                System.out.println(queryDocumentSnapshots);
                if (e != null) {
                    MainActivity.toast(getContext(), e.getMessage());
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    arguments = new ArrayList<>();
                    arguments.add(EVENT_DROPDOWN_PLACEHOLDER);
                    for (DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        arguments.add(event.name);
                    }
                    setArguments(arguments);
                }
            }
        });
    }

    @Override
    public void tagDiscovered(NFCManager mgr, Intent intent) {
        String selectedEvent = EventsFragment.formatEventName(events.getSelectedItem().toString());
        if (selectedEvent.equals(EventsFragment.formatEventName(EVENT_DROPDOWN_PLACEHOLDER))){
            MainActivity.toast(getContext(), "Please select an event first.");
            return;
        }

        ArrayList<String> records = mgr.readTagFromIntent(intent);
        if (records.size() == 0) {
            MainActivity.toast(getContext(), "Tag is empty or not yet formatted.");
            setColor(ERROR_COLOR);
            resetDetailView();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tag records:\n");
        for (int i = 0; i < records.size(); i++) {
            sb.append(i);
            sb.append(". ");
            sb.append(records.get(i));
            sb.append("\n");
        }
        String applicantCollection;
        if (records.get(1) != null){
            applicantCollection = ApplicantInfo.applicantMap.get(records.get(1));
        }else{
            applicantCollection = "hacker_short_info";
        }
        String body = sb.toString();
        recordDisplay.setText(body);
        if (records.size() > 0) {
            String id = records.get(0);
            this.id.setText(id);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference applicant = db.collection(applicantCollection).document(id);
            applicant.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ApplicantInfo h = documentSnapshot.toObject(ApplicantInfo.class);
                            name.setText(h.firstName + " " + h.lastName);
                            email.setText(h.email);
                            if (h.events == null) {
                                h.events = new HashMap<String, Integer>();
                            }
                            Integer checkInCount = h.events.get(selectedEvent);
                            if (checkInCount == null) {
                                onEventJoin(id, selectedEvent, 0);
                                MainActivity.toast(getContext(),"Checked user into event for first time!", 100);
                                setColor(DEFAULT_COLOR);
                            } else {
                                boolean result = onEventJoin(id, selectedEvent, checkInCount);
                                if (result) {
                                    MainActivity.toast(getContext(), "Checked user into event!", 100);
                                    setColor(DEFAULT_COLOR);
                                } else {
                                    MainActivity.toast(getContext(), "User has already checked in!");
                                    setColor(WARNING_COLOR);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MainActivity.toast(getContext(), e.getMessage());
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

    /* Write event attendance to participant in database - returns true if user can join event */
    public Boolean onEventJoin(String id, String event_name, Integer checkInCount){
        if ( allowUnlimited.isChecked()
                || (checkInCount+1 == 2 && allowSeconds.isChecked())
                || checkInCount+1 < 2) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference applicant = db.collection("hacker_short_info").document(id);
            applicant.update("events."+event_name, checkInCount + 1);
            return true;
        }
        return false;
    }

    private void setColor(int color) {
        rootView.setBackgroundColor(color);
    }
}
