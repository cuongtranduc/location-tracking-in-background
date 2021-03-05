package com.cuongtd.locationtracking;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Query.Direction;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import io.grpc.okhttp.internal.Util;

public class JobService extends JobIntentService {
    Double Latitude, Longitude;
    public static final int JOB_ID = 1;
    private static final String TAG = "MyLocationService";
    static private FusedLocationProviderClient fusedLocationClient;
    static private FirebaseFirestore db;

    public JobService() {
        super();
    }

    public static void enqueueWork(Context context, Intent work) {
        JobService.enqueueWork(context, JobService.class, JOB_ID, work);
    }

    private static void firebaseSetup(Context context) {
        FirebaseApp.initializeApp(context);
        // [START get_firestore_instance]
        JobService.db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        // FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
        //         .setPersistenceEnabled(true)
        //         .build();
        // JobService.db.setFirestoreSettings(settings);
        // [END set_firestore_settings]
    }

    public void setupCacheSize() {
        // [START fs_setup_cache]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        JobService.db.setFirestoreSettings(settings);
        // [END fs_setup_cache]
    }

    @Override
    public void onHandleWork(@NonNull Intent intent) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            firebaseSetup(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                           Log.d("latitude", String.valueOf(location.getLatitude()));
                            Log.d("longitude", String.valueOf(location.getLongitude()));
                            setLocation(location);
                        }
                    }
                });
        }
    }

    static public void setLocation(Location location) {
        // [START set_document]
        Map<String, Object> locationObj = new HashMap<>();
        locationObj.put("latitude", String.valueOf(location.getLatitude()));
        locationObj.put("longitude", String.valueOf(location.getLongitude()));

        JobService.db.collection("locations").document(Build.MODEL).collection(Utils.getTodayDate())
                .document(Utils.getCurrentTime()).set(locationObj)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        // [END set_document]
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
