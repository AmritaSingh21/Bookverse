package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Fragments.BookFragment;
import com.app.bookverse.Fragments.MyBookRecyclerViewAdapter;
import com.app.bookverse.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class HomeActivity extends AppCompatActivity
        implements MyBookRecyclerViewAdapter.ItemClickListener {

    private static ActivityHomeBinding binding;
    private static final String TAG = "HOMEACTVITY";

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    private ArrayList<Book> bookList = new ArrayList<>();

    MenuItem searchTitle, searchAuthor, nearbySeller;
    Double myLongitude, myLatitude;
    public static final double RADIUS_OF_EARTH_KM = 6371.01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);

//        Menu menu = findViewById(R.id.home_menu);
//        searchTitle = menu.findItem(R.id.searchTitle);
//        searchAuthor = menu.findItem(R.id.searchAuthor);
//        nearbySeller = menu.findItem(R.id.searchNearby);

        navigationMenu();

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                if (user.getLongitude() != null) {
                    myLongitude = user.getLongitude();
                }
                if (user.getLatitude() != null) {
                    myLatitude = user.getLatitude();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        binding.recyclerView.setLayoutManager(manager);

        fetchBooks(false);

        // implement search
        searchingUsingKey();
    }

    private void searchingUsingKey() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchBooks(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fetchBooks(true);
                return true;
            }
        });
    }

    private void fetchBooks(boolean keywordFlag) {
        dbRef = firebaseInstance.getReference("books");
        dbRef.limitToFirst(30).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            bookList.clear();
                            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                                Book book = dataSnapshot.getValue(Book.class);
                                if (book.getOwnerId().equals(auth.getUid()) || book.isSold()) {
                                    continue;
                                }
                                // check for nearby seller
//                                if (nearbySeller.isChecked() && !checkIsNearbySeller(book.getOwnerId())) {
//                                    continue;
//                                }

                                if (keywordFlag && !binding.searchView.getQuery().
                                        toString().isEmpty()) {
                                    if (searchTitle.isChecked() && book.getTitle().toLowerCase(Locale.ROOT)
                                            .contains(binding.searchView.getQuery()
                                                    .toString().toLowerCase(Locale.ROOT))) {
                                        bookList.add(book);
                                    } else if (searchAuthor.isChecked() && book.getAuthor().toLowerCase(Locale.ROOT)
                                            .contains(binding.searchView.getQuery()
                                                    .toString().toLowerCase(Locale.ROOT))) {
                                        bookList.add(book);
                                    }
                                } else {
                                    bookList.add(book);
                                }

                            }
                            Log.d(TAG, "books size: " + bookList.size());
                            MyBookRecyclerViewAdapter adapter =
                                    new MyBookRecyclerViewAdapter(bookList);
                            adapter.setClickListener(HomeActivity.this);
                            binding.recyclerView.setAdapter(adapter);
                        }
                    }
                });
    }

    private boolean checkIsNearbySeller(String ownerId) {
        final Boolean[] nearby = {true};
//        final CountDownLatch latch = new CountDownLatch(1);
        if (myLongitude == null || myLatitude == null) {
            return true;
        }
        dbRef = firebaseInstance.getReference("users").child(ownerId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User otherUser = snapshot.getValue(User.class);

                // calculate distance
                if (otherUser.getLatitude() != null && otherUser.getLongitude() != null) {
                    double distance = calculateTheDistance(myLatitude, myLongitude,
                            otherUser.getLatitude(), otherUser.getLongitude());
                    Log.d(TAG, "Distance between users: " + distance + " km");
                    if(distance <= 15){
                        nearby[0] = true;
                    }
                }

//                latch.countDown();
            }

            public double calculateTheDistance(double lat1, double lon1, double lat2, double lon2) {
                double dLat = Math.toRadians(lat2 - lat1);
                double dLon = Math.toRadians(lon2 - lon1);
                lat1 = Math.toRadians(lat1);
                lat2 = Math.toRadians(lat2);
                double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                return RADIUS_OF_EARTH_KM * c;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                latch.countDown();
            }
        });

//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        return nearby[0];
    }

    private void navigationMenu() {
        binding.bottomMenu.setSelectedItemId(R.id.menu_home);
        binding.bottomMenu.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case (R.id.menu_auction):
                    startActivity(new Intent(HomeActivity.this, AuctionActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
                case (R.id.menu_mybooks):
                    startActivity(new Intent(HomeActivity.this, MyLibraryActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
                case (R.id.menu_myprofile):
                    startActivity(new Intent(HomeActivity.this, MyProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
//        Menu menu = findViewById(R.id.home_menu);
        searchTitle = menu.findItem(R.id.searchTitle);
        searchAuthor = menu.findItem(R.id.searchAuthor);
//        nearbySeller = menu.findItem(R.id.searchNearby);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.searchNearby:
//                if (nearbySeller.isChecked()) {
//                    nearbySeller.setChecked(false);
//                } else {
//                    nearbySeller.setChecked(true);
//                    fetchBooks(false);
//                }
//                break;
            case R.id.searchTitle:
                if (searchTitle.isChecked()) {
                    searchTitle.setChecked(false);
                    searchAuthor.setChecked(true);
                } else {
                    searchTitle.setChecked(true);
                    searchAuthor.setChecked(false);
                    binding.searchView.setQueryHint("Search by Book Title");
                    fetchBooks(false);
                }
                break;
            case R.id.searchAuthor:
                if (searchAuthor.isChecked()) {
                    searchTitle.setChecked(true);
                    searchAuthor.setChecked(false);
                } else {
                    searchTitle.setChecked(false);
                    searchAuthor.setChecked(true);
                    binding.searchView.setQueryHint("Search by Book Author");
                    fetchBooks(false);
                }
                break;
        }
        return true;
    }

    private void logout() {
        auth.signOut();
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
        Toast.makeText(getApplicationContext(),
                "Successfully signed out", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("bookID", bookList.get(position).getId());
        Intent intent = new Intent(HomeActivity.this, BookDetail.class);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }
}