package io.nwhacks.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.nfc.Tag;
import android.nfc.tech.NdefFormatable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
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
        Tag tag = getTagFromIntent(intent);
        return readTag(tag);
    }

    public boolean writeTagFromIntent(Intent intent, String message){
        Tag tag = getTagFromIntent(intent);
        NdefMessage payload = createTextMessage(message);

        try {
            writeTag(tag, payload);
            return true;
        } catch (Exception e){
            MainActivity.toast(activity.getApplicationContext(), "NFC Write Error:\n" + e.getMessage());
            return false;
        }
    }

    public Tag getTagFromIntent(Intent intent){
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        return tag;
    }

    /* Return all text records from NFC tag.*/
    public ArrayList<String> readTag(Tag tag){
        ArrayList<String> contents = new ArrayList<String>();

        Ndef ndefTag = Ndef.get(tag);
        if (ndefTag == null){
            return contents;
        }

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

    /* Create and return a text record to write to an NFC tag. */
    NdefMessage createTextMessage(String message) {
        try {
            byte[] messageBytes = message.getBytes("UTF-8");
            ByteArrayOutputStream payload = new ByteArrayOutputStream(messageBytes.length);
            payload.write(messageBytes, 0, messageBytes.length);

            NdefRecord record = new NdefRecord(
                    NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT,
                    new byte[0],
                    payload.toByteArray());

            return new NdefMessage(new NdefRecord[]{record});
        } catch (Exception e){
            return null;
        }
    }

    /* Write given message to given NFC tag. */
    public void writeTag(Tag tag, NdefMessage message) throws Exception {
        if (tag != null) {
            try {
                Ndef ndefTag = Ndef.get(tag);
                if (ndefTag != null) {
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(message);
                    ndefTag.close();
                } else { // format tag before writing
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null) {
                        nForm.connect();
                        nForm.format(message);
                        nForm.close();
                    } else {
                        // force tag to format to NDEF
                        ndefTag.writeNdefMessage(new NdefMessage(new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)));
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public String[][] getTechList(){
        return new String[][] {
                { android.nfc.tech.Ndef.class.getName() },
                { android.nfc.tech.NdefFormatable.class.getName() }
        };
    }
}