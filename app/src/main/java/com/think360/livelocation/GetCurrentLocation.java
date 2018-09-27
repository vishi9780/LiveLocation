package com.think360.livelocation;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.think360.livelocation.interfaces.GpsOnListener;
import com.think360.livelocation.permission.PermissionResult;
import com.think360.livelocation.playservices.PlayServiceAvailability;

/**
 * Created by Why so serious !!! on 23-09-2017.
 */

public class GetCurrentLocation extends BroadcastReceiver implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Context activity;
    GpsOnListener gpsOnListener;
    public GetCurrentLocation(Context context) {
        this.activity=context;
        this.gpsOnListener=(GpsOnListener)activity;
    }

    public GetCurrentLocation(Fragment fragment) {
        try {
            this.activity=fragment.getActivity();
            this.gpsOnListener = ((GpsOnListener) fragment);
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement AdapterCallback.");
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (isGPSON()){
            mCurrentLocation = location;
            if (location.getLatitude()!=0.0 && mGoogleApiClient.isConnected()==true){
                if (continuousLocationLocation==true){
                    gpsOnListener.gpsLocationFetched(location);
                }else {
                    gpsOnListener.gpsLocationFetched(location);
                    mGoogleApiClient.disconnect();
                }
            }
        }
    }

    public boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private boolean isGPSON() {
        isGPSEnabled = GPSCheckPoint.gpsProviderEnable((Activity)activity);
        isNetworkEnabled = GPSCheckPoint.networkProviderEnable((Activity)activity);
        if (!isGPSEnabled && !isNetworkEnabled) {
            return false;
        }else {
            return true;
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void getCurrentLocation() {
        if (!PlayServiceAvailability.isAvailable((Activity) activity)) {
            ((Activity)activity).finish();
        }
        createLocationRequest();
        getLocation();
        registerReceiver();
        permissionActivity();
    }

    private static final String TAG = "GetCurrentLocation";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    public static LocationRequest mLocationRequest;
    public static GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLocation() {
        if (mGoogleApiClient!=null){

        }else {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    boolean continuousLocationLocation=false;
    public void getContinuousLocation(boolean update) {
        this.continuousLocationLocation=update;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras!=null){
            if (extras.containsKey("PERMISSION")){
                if (intent.getStringExtra("PERMISSION").equals("GRANTED")){
                    unregisterReceiver();
                    stopLocationUpdate();
                    gpsOnListener.gpsPermissionDenied(0);
                }else {
                    gpsOnListener.gpsPermissionDenied(1);
                    unregisterReceiver();
                    stopLocationUpdate();
                }
            }else if(extras.containsKey("GPS")){
                if (intent.getStringExtra("GPS").equals("GRANTED")){
                    unregisterReceiver();
                    stopLocationUpdate();
                    gpsOnListener.gpsStatus(true);
                }else {
                    unregisterReceiver();
                    stopLocationUpdate();
                    gpsOnListener.gpsStatus(false);
                }
            }
        }
    }

    private void permissionActivity() {
        Intent intent2=new Intent(activity, PermissionResult.class);
        activity.startActivity(intent2);
    }

    public void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(this);
        }catch (Exception e){

        }

    }

    public void registerReceiver() {
        try {
            LocalBroadcastManager.getInstance(activity).registerReceiver(this, new IntentFilter("com.fusedbulblib"));
        }catch (Exception e){}

    }

    public void stopLocationUpdate() {
        try {
            if (mGoogleApiClient!=null){
                mGoogleApiClient.disconnect();
                mGoogleApiClient=null;
            }}catch (Exception e){}
    }

}
