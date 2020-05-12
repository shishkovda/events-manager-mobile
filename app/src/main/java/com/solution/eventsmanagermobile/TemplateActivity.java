package com.solution.eventsmanagermobile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import entity.Event;
import entity.Template;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class TemplateActivity extends AppCompatActivity {
    private ClipboardManager myClipboard;
    private ClipData myClip;
    private String eventId;
    private String userId;
    private List<Integer> eventIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle b = getIntent().getExtras();
        if (b!=null) {
            eventId = b.getString("eventId");
            userId = b.getString("userId");
            eventIds = b.getIntegerArrayList("eventIds");
        }

        GetTemplateByEventIdAndUserId getTemplateByEventIdAndUserId = new GetTemplateByEventIdAndUserId();
        getTemplateByEventIdAndUserId.execute(eventId, userId);
        Template template;
        while (true) {
            try {
                template = getTemplateByEventIdAndUserId.get();
                if (template != null) {
                    break;
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (template!=null){
            TextView editorRequestor = findViewById(R.id.editor_requestor);
            TextView editorTemplate = findViewById(R.id.editor_template);

            editorRequestor.setText(template.getRequestor());
            editorTemplate.setText(template.getTemplate());
        }

        final TextView editTemplate = findViewById(R.id.editor_message);
        final TextView hiddenTemplate = findViewById(R.id.hidden_template);
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        editTemplate.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()) {
                    case android.R.id.copy:
                        int min = 0;
                        int max = editTemplate.getText().length();
                        if (editTemplate.isFocused()) {
                            final int selStart = editTemplate.getSelectionStart();
                            final int selEnd = editTemplate.getSelectionEnd();

                            min = Math.max(0, Math.min(selStart, selEnd));
                            max = Math.max(0, Math.max(selStart, selEnd));
                        }
                        final CharSequence selectedText = editTemplate.getText()
                                .subSequence(min, max);
                        String text = selectedText.toString();
                        if("".equals(hiddenTemplate.getText().toString())){
                            hiddenTemplate.setText(editTemplate.getText().toString().replace(text, "{attribute}"));
                        } else {
                            hiddenTemplate.setText(hiddenTemplate.getText().toString().replace(text, "{attribute}"));
                        }
                        String templateText = hiddenTemplate.getText().toString();

                        myClip = ClipData.newPlainText("text", text);
                        myClipboard.setPrimaryClip(myClip);
                        mode.finish();

                        LinearLayout linearLayout1 = findViewById(R.id.linearLayoutMain1);
                        EditText editText1 = linearLayout1.findViewById(R.id.editor_attribute1);
                        LinearLayout linearLayout2 = findViewById(R.id.linearLayoutMain2);
                        EditText editText2 = linearLayout2.findViewById(R.id.editor_attribute2);

                        if("".equals(editText1.getText().toString())) {
                            editText1.setText(text);
                        } else if("".equals(editText2.getText().toString())) {
                            editText2.setText(text);
                        }

                        LinearLayout linearLayoutMain = findViewById(R.id.linearLayoutMain);
                        EditText templateEdit = linearLayoutMain.findViewById(R.id.editor_template);
                        templateEdit.setText(templateText);

                        return true;
                    case android.R.id.cut:
                        return true;
                    case android.R.id.paste:
                        return true;

                    default:
                        break;
                }
                return false;
            }
        });
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

    public void createTemplate(View view) {
        String requester = ((EditText) findViewById(R.id.editor_requestor)).getText().toString();
        String template = ((EditText) findViewById(R.id.editor_template)).getText().toString();
        CreateTemplate createTemplate = new CreateTemplate();
        createTemplate.execute(requester,template);
    }

    private class CreateTemplate extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            HttpRequestor httpRequestor = new HttpRequestor();

            MediaType mediaType = MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\n" +
                    "\t\t\"eventId\":\""+eventId+"\",\n" +
                    "\t\t\"userId\":\""+userId+"\",\n" +
                    "\t\t\"name\":\"Dima\",\n" +
                    "\t\t\"requestor\":\""+params[0]+"\",\n" +
                    "\t\t\"attributes\":[\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\t\"name\":\"Плательщик\",\n" +
                    "\t\t\t\t\"position\":\"1\"\n" +
                    "},"+
                    "\t\t\t{\n" +
                    "\t\t\t\t\"name\":\"Событие\",\n" +
                    "\t\t\t\t\"position\":\"2\"\n" +
                    "}"+
                    "\t\t],\n" +
                    "\t\t\"template\":\""+params[1].replaceAll("\"", "")+"\"\n" +
                    "}");
            String responseBody = httpRequestor.sendRequest("/api/templates", body, "POST");

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private class GetTemplateByEventIdAndUserId extends AsyncTask<String, Void, Template> {

        @Override
        protected Template doInBackground(String... params) {
            Template template = new Template();

            List<Event> events = new ArrayList<>();

            HttpRequestor httpRequestor = new HttpRequestor();
            String responseBody = httpRequestor.sendRequest("/api/templates/"+params[0]+"/"+params[1],
                    null, "GET");

            String jsonData = null;
            jsonData = responseBody;
            JSONObject Jobject = null;

            try {
                Jobject = new JSONObject(jsonData);
                template.setRequestor(Jobject.getString("requestor"));
                template.setTemplate(Jobject.getString("template"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return template;
        }

        @Override
        protected void onPostExecute(Template result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
