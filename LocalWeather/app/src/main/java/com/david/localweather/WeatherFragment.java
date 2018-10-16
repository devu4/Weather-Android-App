package com.david.localweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.david.localweather.Utils.RequestJSON;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class WeatherFragment extends Fragment {


    TextView cityField;
    TextView timeUpdated;
    TextView weather_Icon;
    TextView weather_Desc;
    TextView details;
    TextView currentTemp;

    Handler weather_handler;

    Typeface weather_Font;

    public WeatherFragment(){
        weather_handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.town_field);
        timeUpdated = (TextView)rootView.findViewById(R.id.time_updated);
        weather_Icon = (TextView)rootView.findViewById(R.id.weather_icon);
        weather_Desc = (TextView)rootView.findViewById(R.id.weather_desc);
        details = (TextView)rootView.findViewById(R.id.details_field);
        currentTemp = (TextView)rootView.findViewById(R.id.current_temp);

        weather_Icon.setTypeface(weather_Font);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weather_Font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weathericons-regular-webfont.ttf");
    }



    protected void asyncUpdateWeatherData(final String lat, final String lon){
        new Thread(){
            public void run(){
                JSONObject json = null;

                if(isNetworkAvailable(getActivity())) {
                    json = RequestJSON.getJSON(getActivity(), lat, lon);
                    saveFile(getActivity(), "savedRest.json", json.toString() );
                    Log.d("WeatherFrag", "Downloading json from internet and saved");
                }
                else if(doesFileExist(getActivity(), "savedRest.json")) {
                    json = readFile(getActivity(), "savedRest.json");
                    Log.d("WeatherFrag", "No internet, getting Json from internal");

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "No internet, getting weather from save!", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                final JSONObject json1 = json;

                if(json1 == null){
                    weather_handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),"Can not find info for location and no previous saves found!", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    weather_handler.post(new Runnable(){
                        public void run(){
                            showWeather(json1);
                        }
                    });
                }
            }
        }.start();
    }

    private void showWeather(JSONObject json){
        try {

            JSONObject weatherDetails = json.getJSONArray("weather").getJSONObject(0);

            //get city address
            if("".equals(json.getString("name"))) {
                JSONObject cord = json.getJSONObject("coord");
                //https://stackoverflow.com/questions/2296377/how-to-get-city-name-from-latitude-and-longitude-coordinates-in-google-maps
                Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(cord.getDouble("lat"), cord.getDouble("lon"), 1);
                if (addresses.size() > 0) {
                    cityField.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
                } else {
                    cityField.setText(cord.getString("lat") + " " + cord.getString("lon"));
                }
            }
            else
                cityField.setText(json.getString("name") + ", " + json.getJSONObject("sys").getString("country"));


            //parse time updated
            DateFormat df = DateFormat.getDateTimeInstance();
            String timeUpdatedf = df.format(new Date(json.getLong("dt")*1000));
            timeUpdated.setText(timeUpdatedf);

            //set Weather Icon
            setWeatherIcon(weatherDetails.getString("icon"));

            //set Weather Desc
            weather_Desc.setText(weatherDetails.getString("description").toUpperCase(Locale.US));

            //set Weather Details
            JSONObject main = json.getJSONObject("main");
            String detailsText = "";

            if(json.has("visibility"))
                detailsText = "Visibility: " + json.getString("visibility") + " m";

            if(main.has("pressure"))
                detailsText += "\n" + "Pressure: " + main.getString("pressure") + " hPa";

            if(main.has("humidity"))
                detailsText += "\n" + "Humidity: " + main.getString("humidity") + " %";

            details.setText(detailsText);

            //set current temperature
            currentTemp.setText(main.getString("temp") + " ℃");

        }catch(Exception e){
            Log.e("WeatherFragment", "Fields not found in the JSON data: " + e.getMessage());
        }
    }

    private void setWeatherIcon(String Id){
        String icon = "";

        switch(Id) {
            case "01d" : icon = getActivity().getString(R.string.weather_01d);
                break;
            case "01n" : icon = getActivity().getString(R.string.weather_01n);
                break;
            case "02d" : icon = getActivity().getString(R.string.weather_02d);
                break;
            case "02n" : icon = getActivity().getString(R.string.weather_02n);
                break;
            case "03d" : icon = getActivity().getString(R.string.weather_03d);
                break;
            case "03n" : icon = getActivity().getString(R.string.weather_03n);
                break;
            case "04d" : icon = getActivity().getString(R.string.weather_04d);
                break;
            case "04n" : icon = getActivity().getString(R.string.weather_04n);
                break;
            case "09d" : icon = getActivity().getString(R.string.weather_09d);
                break;
            case "09n" : icon = getActivity().getString(R.string.weather_09n);
                break;
            case "10d" : icon = getActivity().getString(R.string.weather_10d);
                break;
            case "10n" : icon = getActivity().getString(R.string.weather_10n);
                break;
            case "11d" : icon = getActivity().getString(R.string.weather_11d);
                break;
            case "11n" : icon = getActivity().getString(R.string.weather_11n);
                break;
            case "13d" : icon = getActivity().getString(R.string.weather_13d);
                break;
            case "13n" : icon = getActivity().getString(R.string.weather_13n);
                break;
            case "50d" : icon = getActivity().getString(R.string.weather_50d);
                break;
            case "50n" : icon = getActivity().getString(R.string.weather_50n);
                break;
        }

        weather_Icon.setText(icon);
    }

    //https://stackoverflow.com/questions/40168601/android-how-to-save-json-data-in-a-file-and-retrieve-it
    private JSONObject readFile(Context context, String fileName) {
        BufferedReader input = null;
        File file = null;
        try {
            file = new File(context.getFilesDir(), fileName);

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            return new JSONObject(buffer.toString());

        } catch (Exception e) {
            Log.e("WeatherFragment", e.getMessage());
            return null;
        }
    }

    //https://stackoverflow.com/questions/16035513/saving-files-internal
    private boolean saveFile(Context context, String fileName, String jsonString)
    {
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(jsonString.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            Log.e("WeatherFragment", e.getMessage());
            return false;
        }
    }

    //https://stackoverflow.com/questions/10576930/trying-to-check-if-a-file-exists-in-internal-storage
    public boolean doesFileExist(Context context, String fname){
        File file = context.getFileStreamPath(fname);
        return file.exists();
    }

    //https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    public boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
