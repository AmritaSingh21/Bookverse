package com.app.bookverse.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.app.bookverse.Entities.User;
import com.app.bookverse.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationService extends Service {

    private static final String DEBUG_TAG = "LOCATIONSERVICE";
    private static final int GPX_NOTIFY = 0x1001;
    private LocationManager location = null;
    Location lastLocation;

    private static FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    private final LocationListener trackListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
//            String locInfo = String.format("Currect location: (%f, %f) @ (%f meters up)",
//                    location.getLatitude(),
//                    location.getLongitude(),
//                    location.getAltitude());
//            if (lastLocation != null) {
//                float distance = location.distanceTo(lastLocation);
//                locInfo += String.format("\nDistance from last = %f meters", distance);
//            }

            // TODO save in db
            dbRef = firebaseInstance.getReference("users").child(auth.getUid());
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    user.setLongitude(location.getLongitude());
                    user.setLatitude(location.getLatitude());
                    dbRef.setValue(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Auth instance
        auth = FirebaseAuth.getInstance();
        //DB instance
        firebaseInstance = FirebaseDatabase.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (flags != 0) {
            Log.v(DEBUG_TAG, "Redelivering or retrying service start: " + flags);
        }
        doServiceStart();
        return Service.START_REDELIVER_INTENT;
    }

    private void doServiceStart() {
        long updateRate = 5000; // 60,000 - 1 min
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String best = location.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.v(DEBUG_TAG, "Permission not granted."); return;
        }
        location.requestLocationUpdates(best, updateRate, 0, trackListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (location != null) {
            location.removeUpdates(trackListener);
            location = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
