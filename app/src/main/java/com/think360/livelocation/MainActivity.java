package com.think360.livelocation;

import android.app.Activity;
import android.app.Dialog;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.think360.livelocation.dialogs.CheckGPSDialog;
import com.think360.livelocation.dialogs.PermissionDeniedDialog;
import com.think360.livelocation.interfaces.DialogClickListener;
import com.think360.livelocation.interfaces.GpsOnListener;

/**
 * Created by Why so serious !!! on 23-09-2017.
 */

public class MainActivity extends AppCompatActivity implements GpsOnListener {

    TextView currentLocationTxt;
    GetCurrentLocation getCurrentLocation;
    private ProgressDialog dialog;
    final Handler handler = new Handler();
    static double lati=0.0,longi=0.0;
    static TextView getLocationTxt,stopTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setCancelable(false);

        getLocationTxt = (TextView) findViewById(R.id.getLocationTxt);
        stopTxt = (TextView) findViewById(R.id.stopTxt);

        currentLocationTxt = (TextView) findViewById(R.id.currentLocationTxt);
//        getLocationTxt.setTypeface(new FontTypeFace(this).MontserratRegular());
//        currentLocationTxt.setTypeface(new FontTypeFace(this).MontserratRegular());
        getCurrentLocation = new GetCurrentLocation(MainActivity.this);
        getLocationTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdate();

            }
        });


        stopTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdate();
            }
        });
        findViewById(R.id.btn_opneFragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new com.think360.livelocation.FragmentLocation());
            }
        });
    }

    public void stopLocationUpdate() {
        Toast.makeText(MainActivity.this, "Location update stop", Toast.LENGTH_SHORT).show();
        getCurrentLocation.stopLocationUpdate();
        handler.removeCallbacksAndMessages(null);
    }

    public  void startLocationUpdate() {
        dialog.setMessage("Doing something, please wait.");
        dialog.show();
        getCurrentLocation.getContinuousLocation(true);
        getCurrentLocation.getCurrentLocation();
    }

    @Override
    public void gpsStatus(boolean _status) {
        if (_status == false) {
            new CheckGPSDialog(this).showDialog(new DialogClickListener() {
                @Override
                public void positiveListener(Activity context, Dialog dialog) {
                    dialog.dismiss();
                    getCurrentLocation.getCurrentLocation();
                }
                @Override
                public void negativeListener(Activity context, Dialog dialog) {
                    dialog.dismiss();
                }
            });
        } else {
            getCurrentLocation.getCurrentLocation();
        }
    }

    @Override
    public void gpsPermissionDenied(int deviceGpsStatus) {
        if (deviceGpsStatus == 1) {
            permissionDeniedByUser();
        } else {
            getCurrentLocation.getCurrentLocation();
        }
    }

    @Override
    public void gpsLocationFetched(final Location location) {
        if (location != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            lati=location.getLatitude();
            longi=location.getLongitude();
            currentLocationTxt.setText(location.getLatitude() + ", " + location.getLongitude());
            Log.w("locationUpdate", currentLocationTxt.getText().toString());
            Log.w("GetAddress", new GetAddress(this).fetchCurrentAddress(location));
//             currentLocationTxt.setText(new GetAddress(this).fetchCurrentAddress(location));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this, 3000);
//                    Log.e("38", ">>>>" + location.getLatitude());
//                    Log.e("39", ">>>>" + location.getLongitude());
                }
            }, 1000);
        } else {
            Toast.makeText(this, "unable_find_location", Toast.LENGTH_SHORT).show();
        }
    }

    private void permissionDeniedByUser() {

        new PermissionDeniedDialog(this).showDialog(new DialogClickListener() {
            @Override
            public void positiveListener(Activity context, Dialog dialog) {
                dialog.dismiss();
                getCurrentLocation.getCurrentLocation();
            }

            @Override
            public void negativeListener(Activity context, Dialog dialog) {
                dialog.dismiss();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        // create a FragmentManager
        FragmentManager fm = getFragmentManager();
        // create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        // replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }
   /* ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                service.scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
            MainActivity.getLocationTxt.performClick();
            Log.e(TAG, "run: 53>>"+MainActivity.lati+","+MainActivity.longi );
        }
    }, 1, 1, TimeUnit.SECONDS);
                service.shutdownNow();*/
}
