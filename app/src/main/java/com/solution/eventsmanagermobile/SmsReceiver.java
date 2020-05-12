package com.solution.eventsmanagermobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.SmsMessage;
import okhttp3.*;

import java.util.List;

public class SmsReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            SmsMessage[] messages = new SmsMessage[pduArray.length];
            for (int i = 0; i < pduArray.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = intent.getExtras().getString("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i], format);
                }
                else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                }
            }

            StringBuilder bodyText = new StringBuilder();
            for (int i = 0; i < messages.length; i++) {
                bodyText.append(messages[i].getMessageBody());
            }
            String body = bodyText.toString();
            sendMessage(body);

        }
    }

    private void sendMessage(String message){
        SendMessage sendMessage = new SendMessage();
        String eventId = message.substring(message.lastIndexOf(" ")+1);
        eventId = eventId.replaceAll("\"", "");
        sendMessage.execute(message, eventId);
    }

    private class SendMessage extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            HttpRequestor httpRequestor = new HttpRequestor();

            MediaType mediaType = MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\n" +
                    "\t\t\"eventId\":\""+params[1]+"\",\n" +
                    "\t\t\"userId\":\""+MainActivity.userId+"\",\n" +
                    "\t\t\"message\":\""+params[0].replaceAll("\"", "")+"\"\n" +
                    "}");
            String responseBody = httpRequestor.sendRequest("/api/messages", body, "POST");

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
