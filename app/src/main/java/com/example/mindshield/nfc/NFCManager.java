package com.example.mindshield.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import com.example.mindshield.firebase.FirebaseManager;

public class NFCManager {

    public static String readTag(Intent intent) {

        Parcelable[] rawMsgs =
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMsgs != null && rawMsgs.length > 0) {

            NdefMessage msg = (NdefMessage) rawMsgs[0];
            NdefRecord record = msg.getRecords()[0];

            byte[] payload = record.getPayload();

            try {
                // Skip language code
                int langLength = payload[0] & 0x3F;

                return new String(payload, langLength + 1,
                        payload.length - langLength - 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    public static NdefMessage createMessage(String text) {
        return new NdefMessage(
                new NdefRecord[]{
                        NdefRecord.createTextRecord("en", text)
                }
        );
    }
    public static void addFriendByNFC(String otherUid) {
        FirebaseManager.acceptFriend(otherUid);
    }
}
