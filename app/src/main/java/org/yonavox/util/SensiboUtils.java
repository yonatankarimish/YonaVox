package org.yonavox.util;

import okhttp3.*;

import java.io.IOException;

//Sensibo is the name of the company that manufactures the IoT sensor used to communicate with the air conditioner
//The class manages all communications with the sensor over HTTP.
//I really just tuned these to my own preferences. Any real production app would have this all customizable
public class SensiboUtils {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType APPLICATION_JSON = MediaType.get("application/json; charset=utf-8");
    private static final int[] temperatures = new int[] {16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
    private static final String[] modes = new String[]{"dry", "auto", "heat", "fan", "cool"};

    public static String apiKey;
    public static String deviceId;

    public static void turnOn(){
        String url = "https://home.sensibo.com/api/v2/pods/"+deviceId+"/acStates/on?apiKey="+apiKey;
        String json = "{\"newValue\":true}";

        RequestBody body = RequestBody.create(json, APPLICATION_JSON);
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();

        httpClient.newCall(request).enqueue(defaultCallback);
    }

    public static void turnOff(){
        String url = "https://home.sensibo.com/api/v2/pods/"+deviceId+"/acStates/on?apiKey="+apiKey;
        String json = "{\"newValue\":false}";

        RequestBody body = RequestBody.create(json, APPLICATION_JSON);
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();

        httpClient.newCall(request).enqueue(defaultCallback);
    }

    public static void heat(){
        String url = "https://home.sensibo.com/api/v2/pods/"+deviceId+"/acStates?apiKey="+apiKey;
        String json = "{\"acState\": {" +
            "\"on\": true, " +
            "\"fanLevel\": \"high\", " +
            "\"light\": \"on\", " +
            "\"temperatureUnit\": \"C\", " +
            "\"horizontalSwing\": \"stopped\", " +
            "\"swing\": \"fixedMiddleBottom\", " +
            "\"targetTemperature\": 30, " +
            "\"mode\": \"heat\"" +
        "}}";

        RequestBody body = RequestBody.create(json, APPLICATION_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(defaultCallback);
    }

    public static void cool(){
        String url = "https://home.sensibo.com/api/v2/pods/"+deviceId+"/acStates?apiKey="+apiKey;
        String json = "{\"acState\": {" +
                "\"on\": true, " +
                "\"fanLevel\": \"high\", " +
                "\"light\": \"on\", " +
                "\"temperatureUnit\": \"C\", " +
                "\"horizontalSwing\": \"stopped\", " +
                "\"swing\": \"fixedMiddleBottom\", " +
                "\"targetTemperature\": 16, " +
                "\"mode\": \"cool\"" +
                "}}";

        RequestBody body = RequestBody.create(json, APPLICATION_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(defaultCallback);
    }

    private static final Callback defaultCallback = new Callback() {
        @Override public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override public void onResponse(Call call, Response response) throws IOException {
            try (ResponseBody responseBody = response.body()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                Headers responseHeaders = response.headers();
                String responseData = responseBody.string();
                System.out.println(responseData);
            }
        }
    };
}
