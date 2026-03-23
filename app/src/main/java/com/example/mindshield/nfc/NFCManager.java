package com.example.mindshield.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

public class NFCManager {

    public static String readTag(Intent intent) {

        Parcelable[] rawMsgs =
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMsgs != null) {

            NdefMessage msg = (NdefMessage) rawMsgs[0];

            NdefRecord record = msg.getRecords()[0];

            byte[] payload = record.getPayload();

            return new String(payload);

        }

        return null;
    }
}
