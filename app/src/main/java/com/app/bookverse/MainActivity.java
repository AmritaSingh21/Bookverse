package com.app.bookverse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.app.bookverse.Services.AuctionJobService;
import com.app.bookverse.Services.LocationService;
import com.app.bookverse.databinding.ActivityMainBinding;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static  ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Do nothing
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(this, AuctionJobService.class);

//        Job test code
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 12); // Replace with the hour you want the job to start
//        calendar.set(Calendar.MINUTE, 5); // Replace with the minute you want the job to start
//
//        JobInfo jobInfo = new JobInfo.Builder(0, componentName)
//                .setMinimumLatency(calendar.getTimeInMillis() - System.currentTimeMillis())
//                .setOverrideDeadline(calendar.getTimeInMillis() - System.currentTimeMillis() + 1000)
//                .setPersisted(true)
//                .setRequiresCharging(false)
//                .setRequiresDeviceIdle(false)
//                .build();
        JobInfo jobInfo = new JobInfo.Builder(0, componentName)
                .setMinimumLatency(0)
                .setOverrideDeadline(0)
                .setPersisted(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setPeriodic(AlarmManager.INTERVAL_DAY)
                .build();
//
        jobScheduler.schedule(jobInfo);

    }

    public void login(View view) {
        startActivity(new Intent(MainActivity.this, SigninActivity.class));
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public void register(View view) {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

}