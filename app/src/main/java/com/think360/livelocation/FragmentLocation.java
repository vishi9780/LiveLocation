package com.think360.livelocation;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentLocation extends Fragment {
    TextView currentLocationTxt;
    private ProgressDialog dialog;
    View view;
    public static final String TAG=FragmentLocation.class.getSimpleName();
    final Handler handler = new Handler();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main, container, false);
        TextView getLocationTxt = (TextView) view.findViewById(R.id.getLocationTxt);
        TextView stopTxt = (TextView) view.findViewById(R.id.stopTxt);
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);

        currentLocationTxt = (TextView) view.findViewById(R.id.currentLocationTxt);
        getLocationTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler.postDelayed(this, 3000);
                        handler.sendEmptyMessage(909);
                        Log.e(TAG, "run: 54>>"+ MainActivity.lati+","+ MainActivity.longi);
                    }
                }, 1000);
            }
        });
        stopTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacksAndMessages(null);
                handler.removeMessages(909);
            }
        });
        return view;
    }
}
