package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Fragments.MyAuctionRecyclerViewAdapter;
import com.app.bookverse.Fragments.MyBookRecyclerViewAdapter;
import com.app.bookverse.databinding.ActivityAuctionBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class AuctionActivity extends AppCompatActivity
        implements MyAuctionRecyclerViewAdapter.ItemClickListener {

    private static ActivityAuctionBinding binding;
    private static final String TAG = "AuctionACTVITY";

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    private ArrayList<BookAuction> auctionList = new ArrayList<>();

    MenuItem searchTitle, searchAuthor, nearbySeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuctionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);

        navigationMenu();

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        LinearLayoutManager manager = new LinearLayoutManager(this);
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
        dbRef = firebaseInstance.getReference("auctions");
        dbRef.limitToFirst(30).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            auctionList.clear();
                            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                                BookAuction book = dataSnapshot.getValue(BookAuction.class);
                                if (book.getOwnerId().equals(auth.getUid()) || book.isSold()
                                        || !book.getStatus().equalsIgnoreCase("open")) {
                                    continue;
                                }
                                if (keywordFlag && !binding.searchView.getQuery().
                                        toString().isEmpty()) {
                                    if (searchTitle.isChecked() && book.getTitle().toLowerCase(Locale.ROOT)
                                            .contains(binding.searchView.getQuery()
                                                    .toString().toLowerCase(Locale.ROOT))) {
                                        auctionList.add(book);
                                    } else if (searchAuthor.isChecked() && book.getAuthor().toLowerCase(Locale.ROOT)
                                            .contains(binding.searchView.getQuery()
                                                    .toString().toLowerCase(Locale.ROOT))) {
                                        auctionList.add(book);
                                    }
                                } else {
                                    auctionList.add(book);
                                }

                            }
                            Log.d(TAG, "auctions size: " + auctionList.size());
                            MyAuctionRecyclerViewAdapter adapter =
                                    new MyAuctionRecyclerViewAdapter(auctionList);
                            adapter.setClickListener(AuctionActivity.this);
                            binding.recyclerView.setAdapter(adapter);
                        }
                    }
                });
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("auctionID", auctionList.get(position).getId());
        Intent intent = new Intent(AuctionActivity.this, AuctionDetailActivity.class);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }

    /**
     * Bottom Navigation Menu
     */
    private void navigationMenu() {
        binding.bottomMenu.setSelectedItemId(R.id.menu_auction);
        binding.bottomMenu.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case (R.id.menu_home):
                    startActivity(new Intent(AuctionActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case (R.id.menu_mybooks):
                    startActivity(new Intent(AuctionActivity.this, MyLibraryActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
                case (R.id.menu_myprofile):
                    startActivity(new Intent(AuctionActivity.this, MyProfileActivity.class));
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


}