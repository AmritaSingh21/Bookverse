package com.app.bookverse.Fragments;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.MyLibraryActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class TabPagerAdapter extends FragmentStateAdapter {

    private static final String TAG = "TabPagerAdapter";
    private ArrayList<Book> dataList = new ArrayList<>();
    private ArrayList<BookAuction> auctionDataList = new ArrayList<>();

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public ArrayList<Book> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<Book> dataList) {
        this.dataList = dataList;
    }

    public ArrayList<BookAuction> getAuctionDataList() {
        return auctionDataList;
    }

    public void setAuctionDataList(ArrayList<BookAuction> auctionDataList) {
        this.auctionDataList = auctionDataList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();
        Log.d(TAG, "creating fragment for position: " + position);
        switch (position) {
            case 0:
                return new BookFragment(dataList);
            case 1:
                Log.d(TAG, "Adapter data list size: " + auctionDataList.size());
                return new AuctionFragment(auctionDataList);
            default:
                return new BookFragment(dataList);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
