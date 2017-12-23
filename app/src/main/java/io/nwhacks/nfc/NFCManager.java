package io.nwhacks.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.nfc.Tag;

import java.util.ArrayList;
import java.util.Arrays;

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

    public ArrayList<String> readTagFromIntent(Intent intent){
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        return readTag(tag);
    }

    /* Return all text records from NFC tag.*/
    public ArrayList<String> readTag(Tag tag){
        ArrayList<String> contents = new ArrayList<String>();
        Ndef ndefTag = Ndef.get(tag);

        NdefMessage ndefMesg = ndefTag.getCachedNdefMessage();
        if (ndefMesg == null) {
            return contents;
        }
        NdefRecord[] ndefRecords = ndefMesg.getRecords();
        if (ndefRecords == null){
            return contents;
        }

        for (NdefRecord record : ndefRecords){
            String type = new String(record.getType());
            if (Arrays.equals(type.getBytes(), NdefRecord.RTD_TEXT)){
                String recordContents = new String(record.getPayload());
                contents.add(recordContents);
            }
        }
        return contents;
    }
}