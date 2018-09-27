package com.think360.livelocation.interfaces;

import android.location.Location;

/**
 * Created by Why so serious !!! on 23-09-2017.
 */

public interface GpsOnListener {

    public void gpsStatus(boolean _status);
    public void gpsPermissionDenied(int deviceGpsStatus);
    public void gpsLocationFetched(Location location);
}
