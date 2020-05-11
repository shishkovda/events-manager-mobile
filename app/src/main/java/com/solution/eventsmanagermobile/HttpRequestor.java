package com.solution.eventsmanagermobile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class HttpRequestor {
    private static String host = "http://84.201.175.97:8081";

    public String sendRequest(String path, RequestBody body, String method){
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(host + path)
                .method(method, body)
                .addHeader("Content-Type", "application/json")
                .build();
        String bodyResponse = null;
        try {
            Response response = client.newCall(request).execute();
            bodyResponse = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bodyResponse;
    }
}
