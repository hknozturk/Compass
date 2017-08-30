package com.example.hawken.compass.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.hawken.compass.R;

/**
 * Created by Hawken on 15.08.2017.
 */

public class WelcomePage extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionGranted = true;

            Intent intent = new Intent(getApplicationContext(), CompassActivity.class);
            startActivity(intent);
        }

        else {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grandResults) {

        mLocationPermissionGranted = false;

        switch (requestCode) {

            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grandResults.length > 0 && grandResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted = true;

                    Intent intent = new Intent(getApplicationContext(), CompassActivity.class);
                    startActivity(intent);
                }
            }
         }
    }
}
