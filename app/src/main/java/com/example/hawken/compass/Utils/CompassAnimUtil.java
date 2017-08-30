package com.example.hawken.compass.Utils;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.example.hawken.compass.Activity.CompassActivity;

/**
 * Created by hawken on 21.08.2017.
 */

public class CompassAnimUtil {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    private float mCurrentDegree = 0f;
    private final float ALPHA = .03f;

    private boolean mLastAccelerometerSet, mLastMagnetometerSet;

    public CompassAnimUtil(SensorManager service){
        mSensorManager = service;
        setmAccelerometer(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        setmMagnetometer(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
    }

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null )
            return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public RotateAnimation onSensorChanged(SensorEvent event, Location currentLoc, Location target){
        if (event.sensor == mAccelerometer) {
            mLastAccelerometer = lowPassFilter(event.values, mLastAccelerometer);
            mLastAccelerometerSet = (true);
        } else if (event.sensor == mMagnetometer) {
            mLastMagnetometer = lowPassFilter(event.values, mLastMagnetometer);
            mLastMagnetometerSet = (true);
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet && currentLoc != null) {
            boolean success = SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);

            if (success) {
                SensorManager.getOrientation(mR, mOrientation);


                float azimuthInRadians = mOrientation[0];
                float azimuth = (float) Math.toDegrees(azimuthInRadians);

                GeomagneticField geoField = new GeomagneticField(
                        (float) currentLoc.getLatitude(),
                        (float) currentLoc.getLongitude(),
                        (float) currentLoc.getAltitude(),
                        System.currentTimeMillis());
                azimuth += geoField.getDeclination();
                float bearing = currentLoc.bearingTo(target);
                float direction = azimuth - bearing;

                RotateAnimation ra = new RotateAnimation(
                        mCurrentDegree,
                        -direction,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

                ra.setDuration(210);

                ra.setFillAfter(true);

                mCurrentDegree = (-direction);
                return ra;
            }
        }
        return null;
    }

    public void onStopSensorService(CompassActivity context){
        mSensorManager.unregisterListener(context, getmAccelerometer());
        mSensorManager.unregisterListener(context, getmMagnetometer());
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
    }

    public void onStartSensorService(CompassActivity context){
        mSensorManager.registerListener(context, getmAccelerometer(), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(context, getmMagnetometer(), SensorManager.SENSOR_DELAY_GAME);
    }

    public Sensor getmAccelerometer() {
        return mAccelerometer;
    }

    public void setmAccelerometer(Sensor mAccelerometer) {
        this.mAccelerometer = mAccelerometer;
    }

    public Sensor getmMagnetometer() {
        return mMagnetometer;
    }

    public void setmMagnetometer(Sensor mMagnetometer) {
        this.mMagnetometer = mMagnetometer;
    }
}
