package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;

import com.app.bookverse.Adapters.BidRecyclerViewAdapter;
import com.app.bookverse.Entities.Bid;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Fragments.MyAuctionRecyclerViewAdapter;
import com.app.bookverse.databinding.ActivityBidListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BidListActivity extends AppCompatActivity {

    private static ActivityBidListBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    private ArrayList<Bid> bids = new ArrayList<>();
    private String auctionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBidListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        //get book id from bundle
        if (getIntent().getExtras().get("auctionID") != null) {
            auctionId = getIntent().getExtras().get("auctionID").toString();
        }

        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(manager);

        fetchBids();
    }

    private void fetchBids() {
        dbRef = firebaseInstance.getReference("auctions").child(auctionId);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BookAuction auction = snapshot.getValue(BookAuction.class);
                bids = auction.getBids();
                if (bids == null) {
                    return;
                }
                BidRecyclerViewAdapter adapter =
                        new BidRecyclerViewAdapter(bids);
//                adapter.setClickListener(AuctionActivity.this);
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}