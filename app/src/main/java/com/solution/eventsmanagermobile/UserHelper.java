package com.solution.eventsmanagermobile;

import android.os.AsyncTask;
import entity.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class UserHelper {

    public User getUserByPhone(String phone){
        GetUserByPhoneNumber getUserByPhoneNumber = new GetUserByPhoneNumber();
        getUserByPhoneNumber.execute(phone);

        User user;
        while (true) {
            try {
                user = getUserByPhoneNumber.get();
                if (user != null) {
                    break;
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    public User getUserById(String id){
        GetUserById getUserById = new GetUserById();
        getUserById.execute(id);

        User user;
        while (true) {
            try {
                user = getUserById.get();
                if (user != null) {
                    break;
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return user;
    }


    private class GetUserByPhoneNumber extends AsyncTask<String, Void, User> {

        @Override
        protected User doInBackground(String... params) {

            User user = new User();

            HttpRequestor httpRequestor = new HttpRequestor();
            System.out.println("test");
            params[0] = "+79172142611";
            String phone = params[0].replaceAll("\\+", "%2B");
            String responseBody = httpRequestor.sendRequest("/api/users?phoneNumber=" + phone,
                    null, "GET");

            String jsonData = responseBody;
            JSONObject Jobject = null;
            try {
                Jobject = new JSONObject(jsonData);
                user.setId(Jobject.getString("id"));
                user.setFirstName(Jobject.getString("firstName"));
                user.setLastName(Jobject.getString("lastName"));
                user.setPhoneNumber(Jobject.getString("phoneNumber"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return user;
        }
    }


    private class GetUserById extends AsyncTask<String, Void, User> {

        @Override
        protected User doInBackground(String... params) {

            User user = new User();

            HttpRequestor httpRequestor = new HttpRequestor();
            String responseBody = httpRequestor.sendRequest("/api/users/" + params[0],
                    null, "GET");

            String jsonData = responseBody;
            JSONObject Jobject = null;
            try {
                Jobject = new JSONObject(jsonData);
                user.setFirstName(Jobject.getString("firstName"));
                user.setLastName(Jobject.getString("lastName"));
                user.setPhoneNumber(Jobject.getString("phoneNumber"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return user;
        }
    }
}
