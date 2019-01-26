package io.nwhacks.nfc;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

/**
 * Created by rice on 11/30/17.
 */

public class WriteFragment extends NFCFragment {
    public static final String ARG_OBJECT = "object";
    public static final int ERROR_COLOR = 0xFFFF0000;
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    private String androidId;
    private String manufacturer = Build.MANUFACTURER;
    private String model = Build.MODEL;
    private TextView deviceInfo;
    private TextView writeId;
    private TextView writeName;
    private TextView writeType;
    private boolean loggedIn = false;
    private boolean created = false;
    private FirebaseUser user;
    private DeviceInfo di;
    private String applicantCollection;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        rootView = inflater.inflate(R.layout.write_fragment, container, false);
        deviceInfo = rootView.findViewById(R.id.device_info);
        writeId = rootView.findViewById(R.id.write_id);
        writeName = rootView.findViewById(R.id.write_name);
        writeType = rootView.findViewById(R.id.write_type);
        androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        created = true;
        attemptInit();
        return rootView;
    }

    public void loggedIn(FirebaseUser user) {
        loggedIn = true;
        this.user = user;
        attemptInit();
    }

    private void attemptInit() {
        if (loggedIn && created) {
            initView();
        }
    }

    private void initView() {
        deviceInfo.setText("Device ID: " + androidId + "\nManufacturer: "+manufacturer+"\nModel: "+model+"\nEmail: "+user.getEmail());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference deviceRef = db.collection("nfc_devices").document(androidId);
        deviceRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        DeviceInfo deviceInfo = documentSnapshot.toObject(DeviceInfo.class);
                        if (deviceInfo == null) {
                            deviceInfo = new DeviceInfo();
                        }
                        deviceInfo.id = androidId;
                        deviceInfo.manufacturer = manufacturer;
                        deviceInfo.model = model;
                        deviceInfo.email = user.getEmail();
                        deviceRef.set(deviceInfo);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MainActivity.toast(getContext(), e.getMessage());
                    }
                });
        deviceRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    di = documentSnapshot.toObject(DeviceInfo.class);
                    writeId.setText(di.writeId);
                    writeName.setText(di.writeName);
                    if (di.writeApplicantType != null){
                        applicantCollection = ApplicantInfo.applicantMap.get(di.writeApplicantType);
                        writeType.setText(di.writeApplicantType);
                        setColor(DEFAULT_COLOR);
                    }else{
                        MainActivity.toast(getContext(), "No ApplicantType for this applicant. Please check that you've selected an applicant.");
                        setColor(ERROR_COLOR);
                    }
                    setColor(DEFAULT_COLOR);
                }
            }
        });
    }


    @Override
    public void tagDiscovered(NFCManager mgr, Intent intent) {
        if (di.writeId.length() == 0)
            MainActivity.toast(getContext(), "No ID to write");

        if (mgr.writeTagFromIntent(intent, new String[]{di.writeId, di.writeApplicantType})){
            MainActivity.toast(getContext(), "ID written to tag", 100);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference applicant = db.collection(applicantCollection).document(di.writeId);
            applicant.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    ApplicantInfo applicantInfo = documentSnapshot.toObject(ApplicantInfo.class);
                    if (applicantInfo != null) {
                        applicant.update("nfc_written", true);
                        setColor(DEFAULT_COLOR);
                    }
                }
            });
        } else {
            setColor(ERROR_COLOR);
        }

    }
}
