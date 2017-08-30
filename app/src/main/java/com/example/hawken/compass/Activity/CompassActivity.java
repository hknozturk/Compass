package com.example.hawken.compass.Activity;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.example.hawken.compass.R;
import com.example.hawken.compass.Utils.CompassAnimUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class CompassActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //compass image
    private ImageView image;
    private CompassAnimUtil compassOrientation;
    private GoogleApiClient googleApiClient;

    private boolean hasAccelerometer;

    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private Location userCurrentLocation = new Location("userCurrentLocation");
    private Location destinationLocation = new Location("destinationLocation");

    //our destination location. Right now North Pole
    private double destinationLocationLatitude = 90.0;
    private double destinationLocationLongitude = 0.0;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        PackageManager packageManager = this.getPackageManager();
        hasAccelerometer = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);

        destinationLocation.setLatitude(destinationLocationLatitude);
        destinationLocation.setLongitude(destinationLocationLongitude);

        image = (ImageView) findViewById(R.id.compass_image);

//        setting up accelerometer and magnetometer

        compassOrientation = new CompassAnimUtil((SensorManager) getSystemService(SENSOR_SERVICE));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        access google api to get location

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

    }

    @Override
    protected void onStart() {
        super.onStart();

        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasAccelerometer) {

        } else {
            checkGPS();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        compassOrientation.onStopSensorService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        googleApiClient.disconnect();
    }

    //    Check that if user turned off gps from notification drawer.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            onResume();
        }
    }

    //    detect and calculate changes
    @Override
    public void onSensorChanged(SensorEvent event) {

        RotateAnimation rotateAnimation = compassOrientation.onSensorChanged(event, userCurrentLocation, destinationLocation);

        if (rotateAnimation != null) {

            image.startAnimation(rotateAnimation);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    //    get location updates from gps when google api connected
    @Override
    public void onConnected(Bundle bundle) {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);


        userCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (userCurrentLocation != null) {
//            can put some toast message
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("GoogleApiClient", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("GoogleApiClient", "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {

        userCurrentLocation = location;

        double distance = userCurrentLocation.distanceTo(destinationLocation);

        System.out.println("Distance to destination: " +distance);
    }

    private void checkGPS() {

        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            alert toast message here
        } else {
            compassOrientation.onStartSensorService(this);
        }
    }


    @Override
    public void onBackPressed() {
//        don't go previous activity
    }

}
