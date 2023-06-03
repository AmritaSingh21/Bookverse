package com.app.bookverse;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.bookverse.Adapters.OfferRecyclerViewAdapter;
import com.app.bookverse.Entities.Offer;
import com.app.bookverse.Entities.User;
import com.app.bookverse.databinding.ActivityOfferListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyOfferListActivity extends AppCompatActivity {

    private static ActivityOfferListBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    private ArrayList<Offer> offers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfferListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(manager);

        fetchOffers();
    }

    private void fetchOffers() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                ArrayList<Offer> myOffers = user.getMyOffers();
                if (myOffers == null || myOffers.isEmpty()) {
                    return;
                }
                OfferRecyclerViewAdapter adapter =
                        new OfferRecyclerViewAdapter(myOffers);
//                adapter.setClickListener(MyOfferListActivity.this);
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}