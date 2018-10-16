package com.david.localweather.Utils;

/**
 * Created by david on 20/12/2017.
 */
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

public class RequestJSON {

    private final static String WEATHER_URL =
            "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=7e8beea2b1d7dd766cb330cbb75d07c0&units=metric";

    private final static String TAG = "Utils.requestJSON";

    //https://code.tutsplus.com/tutorials/create-a-weather-app-on-android--cms-21587
    public static JSONObject getJSON(Context context, String lat, String lon){
        try {
            URL open_weather_url = new URL(String.format(WEATHER_URL, lat, lon ));

            HttpURLConnection connection = (HttpURLConnection)open_weather_url .openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject api_data = new JSONObject(json.toString());

            if(api_data.getInt("cod") != 200){
                Log.e(TAG, "Error: cod is not 200, it is " + api_data.getInt("cod"));
                return null;
            }

            return api_data;
        }catch(Exception e){
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }
}
