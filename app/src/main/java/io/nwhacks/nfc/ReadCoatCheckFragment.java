package io.nwhacks.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReadCoatCheckFragment extends NFCFragment {

    private TextView recordDisplay;
    private TextView name;
    private TextView email;
    private TextView id;
    private TextView applicantTypeText;
    public TextView coatCheckNumber;
    public static final int WARNING_COLOR = 0xFFff8000;
    public static final int ERROR_COLOR = 0xFFFF0000;
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        rootView = inflater.inflate(R.layout.read_coat_check_number_fragment, container, false);

        recordDisplay = rootView.findViewById(R.id.coatRecordDisplay);
        name = rootView.findViewById(R.id.coatName);
        email = rootView.findViewById(R.id.coatEmail);
        applicantTypeText = rootView.findViewById(R.id.coatApplicantType);
        id = rootView.findViewById(R.id.coatId);
        coatCheckNumber = rootView.findViewById(R.id.coatCheckNumber);
        return rootView;
    }

    @Override
    public void loggedIn(FirebaseUser user){};

    private void resetDetailView() {
        name.setText("Name");
        email.setText("Email");
        id.setText("ID");
        applicantTypeText.setText("Applicant Type");
        coatCheckNumber.setText("Coat Check #");

    }

    @Override
    public void tagDiscovered(NFCManager mgr, Intent intent) {
        setColor(DEFAULT_COLOR);
        ArrayList<String> records = mgr.readTagFromIntent(intent);
        if (records.size() == 0) {
            MainActivity.toast(getContext(), "Tag is empty or not yet formatted.");
            setColor(DEFAULT_COLOR);
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
        if (records.size() > 1){
            applicantCollection = ApplicantInfo.applicantMap.get(records.get(1));
        }else{
            applicantCollection = null;
        }
        String body = sb.toString();
        recordDisplay.setText(body);
        if (applicantCollection != null) {
            String id = records.get(0);
            this.id.setText(id);
            applicantTypeText.setText(records.get(1));
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference applicant = db.collection(applicantCollection).document(id);
            applicant.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ApplicantInfo h = documentSnapshot.toObject(ApplicantInfo.class);
                            name.setText(h.firstName + " " + h.lastName);
                            email.setText(h.email);
                            if (h.coatCheckNumber > 0){
                                coatCheckNumber.setText(Integer.toString(h.coatCheckNumber));
                            }else{
                                MainActivity.toast(getContext(), "No coat check number found for applicant");
                                coatCheckNumber.setText("Coat Check #");
                                setColor(WARNING_COLOR);
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
        }else {
            MainActivity.toast(getContext(), "No ApplicantType for this applicant. Please rewrite tag.");
            resetDetailView();
            setColor(ERROR_COLOR);
        }
    }
}
