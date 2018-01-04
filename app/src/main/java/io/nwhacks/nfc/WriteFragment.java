package io.nwhacks.nfc;

import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by rice on 11/30/17.
 */

public class WriteFragment extends NFCFragment {
    public static final String ARG_OBJECT = "object";
    public static final int SUCCESS_COLOR = 0xFF5cb85c;
    public static final int ERROR_COLOR = 0xFFd9534f;
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;


    private String android_id;
    private String manufacturer = Build.MANUFACTURER;
    private String model = Build.MODEL;
    private TextView deviceInfo;
    private TextView writeID;
    private TextView writeName;
    private boolean loggedIn = false;
    private boolean created = false;
    private FirebaseUser user;
    private DeviceInfo di;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        rootView = inflater.inflate(R.layout.write_fragment, container, false);
        deviceInfo = rootView.findViewById(R.id.device_info);
        writeID = rootView.findViewById(R.id.write_id);
        writeName = rootView.findViewById(R.id.write_name);
        android_id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
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
        deviceInfo.setText("Device ID: " + android_id + "\nManufacturer: "+manufacturer+"\nModel: "+model+"\nUser: "+user.getDisplayName()+"\nEmail: "+user.getEmail());

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference ref = db.getReference("admin/devices").child(android_id);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DeviceInfo di = dataSnapshot.getValue(DeviceInfo.class);
                di.id = android_id;
                di.manufacturer = manufacturer;
                di.model = model;
                di.email = user.getEmail();
                di.name = user.getDisplayName();
                ref.setValue(di);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                MainActivity.toast(getContext(), databaseError.getMessage());
            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                di = dataSnapshot.getValue(DeviceInfo.class);
                writeID.setText(di.write_id);
                writeName.setText(di.write_name);
                setColor(DEFAULT_COLOR);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
              MainActivity.toast(getContext(), databaseError.getMessage());
            }
        });
    }

    private void setColor(int color) {
        rootView.setBackgroundColor(color);
    }

    @Override
    public void tagDiscovered(NFCManager mgr, Intent intent) {
        if (di.write_id.length() == 0) {
            MainActivity.toast(getContext(), "no ID to write");
        }

        mgr.writeTagFromIntent(intent, di.write_id);
        MainActivity.toast(getContext(), "ID written to tag", 100);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference("form/registration/" + di.write_id + "/nfc_written").setValue(true);
        setColor(SUCCESS_COLOR);
    }
}
