package com.david.localweather;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";

    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkPermission();
        setBackgroundFromPref();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_changeBackground) {
            showColourDialog();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }


    private void showColourDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Colours");
        final EditText background = new EditText(this);
        background.setText(new BackgroundPreference(this).getBackground());
        background.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(background);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    Color.parseColor(background.getText().toString());
                    changeBackground(background.getText().toString());

                }catch(IllegalArgumentException e){
                    Toast.makeText(MainActivity.this, "Colour not recognised, please try again!", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();
    }

    public void changeBackground(String background){
        new BackgroundPreference(this).setBackground(background);
        setBackgroundFromPref();
    }

    public void setBackgroundFromPref(){
        String colour = new BackgroundPreference(this).getBackground();
        LinearLayout LL = (LinearLayout)findViewById(R.id.main_background);
        LL.setBackgroundColor(Color.parseColor(colour));
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            final Task location = mFusedLocationProviderClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {

                        // Logic to handle location object
                        WeatherFragment weather_frag = (WeatherFragment) getSupportFragmentManager().findFragmentById(R.id.weather_frag);
                        weather_frag.asyncUpdateWeatherData(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                        Log.d(TAG, "getDeviceLocation: location saved for device");

                    }else{
                        WeatherFragment weather_frag = (WeatherFragment) getSupportFragmentManager().findFragmentById(R.id.weather_frag);
                        weather_frag.asyncUpdateWeatherData("0", "0");
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(MainActivity.this, "Unable to get Current Location. Using saved data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }


    }

    //https://stackoverflow.com/questions/32491960/android-check-permission-for-locationmanager/34025702
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            //request permission if the permissions are not given!
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
        else
            getDeviceLocation();
    }
}
