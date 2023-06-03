package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Fragments.MyAuctionRecyclerViewAdapter;
import com.app.bookverse.databinding.ActivityAuctionsBoughtBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AuctionsBoughtActivity extends AppCompatActivity
        implements MyAuctionRecyclerViewAdapter.ItemClickListener {

    private static ActivityAuctionsBoughtBinding binding;
    private static final String TAG = "AuctionBought";

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    private ArrayList<BookAuction> auctionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuctionsBoughtBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(manager);

        fetchAuctions();
    }

    private void fetchAuctions() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                ArrayList<String> wonAuctions = user.getAuctionsWon();
                if (wonAuctions == null || wonAuctions.isEmpty()) {
                    return;
                }
                showAuctions(wonAuctions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showAuctions(ArrayList<String> wonAuctions) {
        dbRef = firebaseInstance.getReference("auctions");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    auctionList.clear();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        BookAuction book = dataSnapshot.getValue(BookAuction.class);
                        if (wonAuctions.contains(book.getId())) {
                            auctionList.add(book);
                        }
                    }
                    Log.d(TAG, "auctions size: " + auctionList.size());
                    MyAuctionRecyclerViewAdapter adapter =
                            new MyAuctionRecyclerViewAdapter(auctionList);
                    adapter.setClickListener(AuctionsBoughtActivity.this);
                    binding.recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("auctionID", auctionList.get(position).getId());
        Intent intent = new Intent(AuctionsBoughtActivity.this, AuctionDetailActivity.class);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }
}