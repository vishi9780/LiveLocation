package com.think360.livelocation.interfaces;

import android.app.Activity;
import android.app.Dialog;

/**
 * Created by Why so serious !!! on 23-09-2017.
 */

public interface DialogClickListener {
    
    void positiveListener(Activity context, Dialog dialog);
    void negativeListener(Activity context, Dialog dialog);
}
