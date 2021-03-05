package com.cuongtd.locationtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class JobBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent serviceIntent = new Intent(context, JobService.class);
        JobService.enqueueWork(context, new Intent());
    }
}
