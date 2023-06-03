package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Fragments.MyBookRecyclerViewAdapter;
import com.app.bookverse.Fragments.TabPagerAdapter;
import com.app.bookverse.databinding.ActivityAuctionBinding;
import com.app.bookverse.databinding.ActivityMyLibraryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MyLibraryActivity extends AppCompatActivity {

    private static ActivityMyLibraryBinding binding;
    TabPagerAdapter pagerAdapter;
    private static final String TAG = "MyLibraryActivity";

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    private ArrayList<Book> bookList = new ArrayList<>();
    private ArrayList<BookAuction> auctionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navigationMenu();

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        if(pagerAdapter != null){
            pagerAdapter.notifyDataSetChanged();
        }
        // default tab is books
        insideBookTab();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.pager.setCurrentItem(tab.getPosition());
                Log.d(TAG, "inside onTabSelected");
                Log.d(TAG, "selected tab "+ tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        insideBookTab();
                        break;
                    case 1:
                        binding.addBook.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(MyLibraryActivity.this,
                                        AddAuctionActivity.class));
                            }
                        });
//                        getUserAuctions();
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
//                super.onPageSelected(position);
                Log.d(TAG, "selected tab with pager "+ position);
                binding.tabLayout.getTabAt(position).select();
            }
        });
    }

    private void insideBookTab() {
        binding.addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyLibraryActivity.this,
                        AddBookActivity.class));
            }
        });
        getUserBooks();
    }

    private void getUserAuctions() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.child("auctionIds").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    ArrayList<String> auctionIdList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        String bookIds = dataSnapshot.getValue(String.class);
                        auctionIdList.add(bookIds);
                    }
                    Log.d(TAG, "user auction size: " + auctionIdList.size());
                    fetchAuctionFromIds(auctionIdList);
                }
            }
        });
    }

    private void fetchAuctionFromIds(ArrayList<String> auctionIdList) {
        dbRef = firebaseInstance.getReference("auctions");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    auctionList.clear();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        BookAuction auction = dataSnapshot.getValue(BookAuction.class);
                        if (auctionIdList.contains(auction.getId())) {
                            auctionList.add(auction);
                            Log.d(TAG, "data list size" + auctionList.size());
                        }
                    }

                    pagerAdapter.setAuctionDataList(auctionList);
                    binding.pager.setAdapter(pagerAdapter);
                }
            }
        });
    }

    private void getUserBooks() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.child("bookIds").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    ArrayList<String> bookIdList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        String bookIds = dataSnapshot.getValue(String.class);
                        bookIdList.add(bookIds);
                    }
//                    Log.d(TAG, "user books size: " + bookIdList.size());
                    fetchBooksFromIds(bookIdList);
                }
            }
        });
    }

    private void fetchBooksFromIds(ArrayList<String> bookIdList) {
        dbRef = firebaseInstance.getReference("books");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    bookList.clear();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        Book book = dataSnapshot.getValue(Book.class);
//                        Log.d(TAG, "book: " + book.getId());
                        if (bookIdList.contains(book.getId())) {
                            bookList.add(book);
//                            Log.d(TAG, "data list size" + bookList.size());
                        }
                    }
                    Log.d(TAG, "creating adapter inside fetch books");
                    pagerAdapter = new TabPagerAdapter(MyLibraryActivity.this);
                    pagerAdapter.setDataList(bookList);

                    // NEW
                    getUserAuctions();

//                    binding.pager.setAdapter(pagerAdapter);
//                    pagerAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Bottom Navigation Menu
     */
    private void navigationMenu() {
        binding.bottomMenu.setSelectedItemId(R.id.menu_mybooks);
        binding.bottomMenu.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case (R.id.menu_home):
                    startActivity(new Intent(MyLibraryActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case (R.id.menu_auction):
                    startActivity(new Intent(MyLibraryActivity.this, AuctionActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case (R.id.menu_myprofile):
                    startActivity(new Intent(MyLibraryActivity.this, MyProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
                default:
                    break;
            }
            return true;
        });
    }
}