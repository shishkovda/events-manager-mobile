package com.solution.eventsmanagermobile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.telephony.TelephonyManager;
import android.view.*;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import entity.Event;
import entity.User;
import okhttp3.*;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static String userId;
    public static List<Integer> eventIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.RECEIVE_SMS}, 1000);
        }

        UserHelper userHelper = new UserHelper();
        setCurrentPhoneNumber();
        User user = userHelper.getUserByPhone(mPhoneNumber);
        userId = user.getId();

        GetAllEvents getAllEvents = new GetAllEvents();
        getAllEvents.execute("");
        List<Event> events;
        while (true) {
            try {
                events = getAllEvents.get();
                if (events.size() != 0) {
                    break;
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (Event event : events) {
            if(event.getAssignee().equals("null")){
                addRow(event, "Собрать кассу");
            } else {
                userHelper = new UserHelper();
                user = userHelper.getUserById(event.getAssignee());
                addRow(event, user.getFirstName());
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class GetAllEvents extends AsyncTask<String, Void, List<Event>> {

        @Override
        protected List<Event> doInBackground(String... params) {

            List<Event> events = new ArrayList<>();

            HttpRequestor httpRequestor = new HttpRequestor();
            String responseBody = httpRequestor.sendRequest("/api/events",null, "GET");

            JSONArray Jarray = null;
            try {
                Jarray = new JSONArray(responseBody);
                for (int i = 0; i < Jarray.length(); i++) {
                    JSONObject object = Jarray.getJSONObject(i);
                    Event event = new Event();
                    event.setId(object.getLong("id"));
                    event.setTitle(object.getString("title"));
                    event.setDate(object.getString("date"));
                    event.setAssignee(object.getString("assignee"));
                    events.add(event);
                    eventIds.add(Integer.valueOf(event.getId().intValue()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return events;
        }

        @Override
        protected void onPostExecute(List<Event> result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private class UpdateEvent extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HttpRequestor httpRequestor = new HttpRequestor();

            MediaType mediaType = MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\n\t\t\"assignee\":\""+params[1]+"\"\n}");
            String responseBody = httpRequestor.sendRequest("/api/events/" + params[0], body, "POST");

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void addRow(final Event event, String c2) {
        String c0 = event.getTitle();
        String c1 = event.getDate();
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tvEvents);
        final LayoutInflater inflater = LayoutInflater.from(this);
        TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
        TextView tv = (TextView) tr.findViewById(R.id.col1);
        tv.setText(c0);
        tv = (TextView) tr.findViewById(R.id.col2);
        tv.setText(c1);
        tv = (TextView) tr.findViewById(R.id.col3);
        tv.setText(c2);

        if (c2.equals("Собрать кассу")){
            tv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UpdateEvent updateEvent = new UpdateEvent();
                    UserHelper userHelper = new UserHelper();
                    setCurrentPhoneNumber();
                    User user = userHelper.getUserByPhone(mPhoneNumber);
                    updateEvent.execute(event.getId().toString(), user.getId().toString());

                    Intent intent1 = new Intent(getApplicationContext(), TemplateActivity.class);
                    Bundle b = new Bundle();
                    intent1.putExtra("eventId", event.getId().toString());
                    intent1.putExtra("userId", userId);
                    startActivity(intent1);
                }
            });
        } else {
            tr.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), TemplateActivity.class);
                    intent.putExtra("eventId", event.getId().toString());
                    intent.putExtra("userId", event.getAssignee());
                    startActivity(intent);
                }
            });
        }
        tableLayout.addView(tr);
        tableLayout.invalidate();
    }

    String mPhoneNumber;

    private void setCurrentPhoneNumber(){
        TelephonyManager tMgr = (TelephonyManager)  this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mPhoneNumber = tMgr.getLine1Number();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
                setCurrentPhoneNumber();
                break;
        }
    }
}
