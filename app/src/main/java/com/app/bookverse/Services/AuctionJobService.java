package com.app.bookverse.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.bookverse.AuctionActivity;
import com.app.bookverse.AuctionsBoughtActivity;
import com.app.bookverse.Entities.Bid;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Fragments.MyAuctionRecyclerViewAdapter;
import com.app.bookverse.MainActivity;
import com.app.bookverse.MyProfileActivity;
import com.app.bookverse.R;
import com.app.bookverse.Utilities.CommonMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AuctionJobService extends JobService {

    private static final String TAG = "AuctionJobService";
    private static final String NOTIFICATION_CHANNEL_ID = "bookverse_notification_channel";
    private static final int BOOKVERSE_NOTIFY = 0x1001;
    private boolean jobCancel = false;
    private NotificationManager notifier = null;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    @Override
    public boolean onStartJob(JobParameters params) {
//        Toast.makeText(this, "Job Started", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "inside run");
                endExpiredAuctions();
//                jobFinished(params, false);
            }
        }).start();
        return true;
    }

    public void endExpiredAuctions() {
        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        dbRef = firebaseInstance.getReference("auctions");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    HashMap<String, List<String>> userAuctionMap = new HashMap<>();

                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        BookAuction book = dataSnapshot.getValue(BookAuction.class);

                        if (!book.getStatus().equalsIgnoreCase("open")) {
                            continue;
                        }

                        Log.d(TAG, "Found book:" + book.getId());
                        // check if end date has passed then find winner and close
                        Date endDate = CommonMethods.fetchDateFromString(book.getEndTime());
                        Date now = new Date();
                        if (endDate == null || !endDate.before(now)) {
                            Log.d(TAG, "Continuing");
                            continue;
                        }
                        book.setStatus("Closed");
                        ArrayList<Bid> bids = book.getBids();
                        if (bids != null && !bids.isEmpty()) {
                            double max = Double.parseDouble(bids.get(0).getPrice());
                            Bid maxBid = bids.get(0);

                            for (Bid bid : bids) {
                                double num = Double.parseDouble(bid.getPrice());
                                if (num > max) {
                                    max = num;
                                    maxBid = bid;
                                }
                            }

                            book.setWinnerId(maxBid.getUserIds());
                            book.setSoldPrice(maxBid.getPrice());
                            book.setSold(true);
                            Log.d(TAG, "going to add in user");
//                            addWonAuctionInUser(book, maxBid.getUserIds());

                            List<String> userAuctionWon;
                            if (userAuctionMap.containsKey(maxBid.getUserIds())) {
                                userAuctionWon = userAuctionMap.get(maxBid.getUserIds());
                            } else {
                                userAuctionWon = new ArrayList<>();
                            }
                            userAuctionWon.add(book.getId());
                            userAuctionMap.put(maxBid.getUserIds(), userAuctionWon);

                        }
                        Log.d(TAG, "updating auction");
                        dbRef = firebaseInstance.getReference("auctions");
                        dbRef.child(book.getId()).setValue(book);

                    }
                    // Update users using the map
                    for (Map.Entry<String, List<String>> entry : userAuctionMap.entrySet()) {
                        String userKey = entry.getKey();
                        List<String> list = entry.getValue();
                        addWonAuctionInUser(userKey, list);
                    }

                }
                stopSelf();
            }
        });
    }

    private void addWonAuctionInUser(String userId, List<String> auctions) {
        Log.d(TAG, "inside won auction user");
        dbRef = firebaseInstance.getReference("users");
        dbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Log.d(TAG, "fetched the user");
                ArrayList<String> auctionsWon = user.getAuctionsWon();
                if (auctionsWon == null) {
                    Log.d(TAG, "no auctions won yet");
                    auctionsWon = new ArrayList<>();
                }
                auctionsWon.addAll(auctions);
                user.setAuctionsWon(auctionsWon);
                dbRef = firebaseInstance.getReference("users");
                dbRef.child(userId).setValue(user);

                // send notification to that user
                // Create a notification builder
                if(user.getId().equalsIgnoreCase(auth.getUid())) {
                    sendNotification();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification() {
        notifier = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                            "My Notification",
                            NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notifier.createNotificationChannel(notificationChannel);
        }
        Intent toLaunch = new Intent(getApplicationContext(), AuctionsBoughtActivity.class);
        PendingIntent intentBack = PendingIntent.getActivity(
                getApplicationContext(), 0, toLaunch, 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(),
                        NOTIFICATION_CHANNEL_ID);
        builder.setTicker("Bookverse");
        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("Congratulations!");
        builder.setContentText("You have won an Auction.");
//                    builder.setAutoCancel(true);
        builder.setContentIntent(intentBack);
        Notification notify = builder.build();
        notifier.notify(BOOKVERSE_NOTIFY, notify);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
