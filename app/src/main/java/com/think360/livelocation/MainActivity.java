package com.think360.livelocation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.think360.livelocation.dialogs.CheckGPSDialog;
import com.think360.livelocation.dialogs.PermissionDeniedDialog;
import com.think360.livelocation.interfaces.DialogClickListener;
import com.think360.livelocation.interfaces.GpsOnListener;
import com.think360.livelocation.internetconnection.ConnectivityReceiver;
import com.think360.livelocation.internetconnection.MyApplication;

import java.util.List;

/**
 * Created by Why so serious !!! on 23-09-2017.
 */

public class MainActivity extends AppCompatActivity implements GpsOnListener,ConnectivityReceiver.ConnectivityReceiverListener {

    TextView currentLocationTxt;
    GetCurrentLocation getCurrentLocation;
    private ProgressDialog dialog;
    final Handler handler = new Handler();
    final Handler handler2 = new Handler();
    static double lati = 0.0, longi = 0.0;
    static TextView getLocationTxt, stopTxt;
    public static final String TAG = MainActivity.class.getSimpleName();
    private double[] getGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {

            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         *
         * */
        findViewById(R.id.btn_stopneFragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler2.removeCallbacksAndMessages(null);
                checkConnection();
            }
        });

        /*
        *
        * **/
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
//                loadFragment(new FragmentLocation());
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler2.postDelayed(this, 3000);
                        Log.e(TAG, "onCreate: 79>>>"+getGPS()[0] );
                        Log.e(TAG, "onCreate: 79>>>"+getGPS()[1] );
                        if (getGPS()[0]!=0.0&&getGPS()[1]!=0.0) {
                            Toast.makeText(MainActivity.this, "" + getGPS()[0], Toast.LENGTH_SHORT).show();
                            handler2.removeCallbacksAndMessages(null);
                        }
                    }
                }, 1000);
            }
        });
        if (checkInternetConnect(MainActivity.this)){
            getLocationTxt.setEnabled(true);
            findViewById(R.id.btn_opneFragment).setEnabled(false);
        }else {
            getLocationTxt.setEnabled(false);
            findViewById(R.id.btn_opneFragment).setEnabled(true);
        }
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

    public static boolean checkInternetConnect(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "pause", Toast.LENGTH_SHORT).show();
    }
  /*  int count=-1;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Toast.makeText(this, ""+hasFocus, Toast.LENGTH_SHORT).show();
        if (!hasFocus){
         *//* startActivity(new Intent(MainActivity.this,MainActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
          finish();*//*
        }else if (hasFocus){
            count=1;
        }else {
          if (count==1){
              startActivity(new Intent(MainActivity.this,MainActivity.class)
                      .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
              count=-1;
          }
        }


    }*/

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "stop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
       /* Snackbar snackbar = Snackbar
                .make( message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();*/
    }
}
