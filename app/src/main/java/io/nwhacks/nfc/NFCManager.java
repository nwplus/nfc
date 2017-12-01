package io.nwhacks.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;

/**
 * Created by rice on 11/30/17.
 */

public class NFCManager {
    private Activity activity;
    private NfcAdapter nfcAdpt;

    public NFCManager(Activity activity) {
        this.activity = activity;
    }

    public boolean verifyNFC() {
        nfcAdpt = NfcAdapter.getDefaultAdapter(activity);

        return nfcAdpt != null;
    }
}