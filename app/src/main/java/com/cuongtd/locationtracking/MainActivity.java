package com.cuongtd.locationtracking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PENDING_INTENT_REQUEST_CODE = 0;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    TextView textView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);

        Log.d("test", String.valueOf(today));

        initView();
        updateTextView();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void initView() {
        textView = (TextView)findViewById(R.id.txtDesc);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(MainActivity.this);
    }

    private void updateTextView() {
        boolean status = getAlarmStatusFromLocal();
        if (status) {
            textView.setText("Service is running in background...");
            button.setText("Stop");
        } else {
            textView.setText("");
            button.setText("Start");
        }
    }

    private void scheduleJob() {
        pendingIntent = createAlarmIntent();
        startAlarm();
    }

    private void startAlarm() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis() + (1000), 10000
                , pendingIntent);
        storeAlarmStatusToLocal(true);
        updateTextView();
    }

    private void stopAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        storeAlarmStatusToLocal(false);
        updateTextView();
    }

    private PendingIntent createAlarmIntent() {
        Intent alarmIntent = new Intent(this, JobBroadCastReceiver.class);
        return PendingIntent.getBroadcast(this, PENDING_INTENT_REQUEST_CODE, alarmIntent, 0);
    };

    private void storeAlarmStatusToLocal(boolean status) {
        SharedPreferences.Editor editor = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE).edit();
        editor.putBoolean("isServiceRunning", status);
        editor.apply();
    }

    private boolean getAlarmStatusFromLocal() {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE);
        return prefs.getBoolean("isServiceRunning", false);
    }

    @Override
    public void onClick(View v) {
        boolean status = getAlarmStatusFromLocal();
        if (status) {
            stopAlarm();
        } else {
            scheduleJob();
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (alarmManager != null) {
//            alarmManager.cancel(pendingIntent);
//        }
//    }
}