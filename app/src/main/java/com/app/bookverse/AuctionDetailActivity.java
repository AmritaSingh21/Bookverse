package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.bookverse.Entities.Bid;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Entities.Offer;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Utilities.CommonMethods;
import com.app.bookverse.databinding.ActivityAuctionDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AuctionDetailActivity extends AppCompatActivity {

    private static ActivityAuctionDetailBinding binding;
    private String auctionId;
    private BookAuction auction;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuctionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get book id from bundle
        if (getIntent().getExtras().get("auctionID") != null) {
            auctionId = getIntent().getExtras().get("auctionID").toString();
        }

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        fetchAuctionDetail();

    }

    private void showBidOptionForOtherUser() {
        boolean showBuyOptionForBook = CommonMethods.showBuyOptionForBook(auth.getUid(), auction);
        if (showBuyOptionForBook) {
            binding.buyBookLayout.setVisibility(View.VISIBLE);
            binding.bid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placeBid();
                }
            });
        }
    }

    private void fetchAuctionDetail() {
        dbRef = firebaseInstance.getReference("auctions").child(auctionId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                auction = snapshot.getValue(BookAuction.class);
                binding.title.setText(auction.getTitle());
                binding.author.setText(auction.getAuthor());
                binding.price.setText(auction.getStartingPrice());
                binding.endTime.setText(auction.getEndTime());
                binding.pricecurrent.setText(auction.getPrice());
                if (auction.getStatus().equalsIgnoreCase("open")) {
                    binding.status.setTextColor(ContextCompat.
                            getColor(AuctionDetailActivity.this, R.color.green));
                } else {
                    binding.status.setTextColor(ContextCompat.
                            getColor(AuctionDetailActivity.this, R.color.red));
                }
                binding.status.setText(auction.getStatus());

                if (auction.getYear() != null) {
                    binding.year.setText(auction.getYear());
                } else {
                    binding.year.setText("-");
                }
                if (auction.getGenre() != null) {
                    binding.genre.setText(auction.getGenre());
                } else {
                    binding.genre.setText("-");
                }
                if (auction.getIsbn() != null) {
                    binding.isbn.setText(auction.getIsbn());
                } else {
                    binding.isbn.setText("-");
                }
                if(auction.isSold()){
                    binding.isSold.setVisibility(View.VISIBLE);
                }
                // Fetch image
                String url = auction.getPicUrl();
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(url);
                try {
                    File file = File.createTempFile(auction.getId(), "jpg");
                    imageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            binding.previewImage.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // for other user
                showBidOptionForOtherUser();
                if (CommonMethods.showMenuOptionsForBook(auth.getUid(), auction)) {
                    setSupportActionBar(binding.myToolbar);
                }

                if (auth.getUid().equals(auction.getOwnerId())) {
                    binding.btnMessage.setVisibility(View.INVISIBLE);
                } else {

                    binding.btnMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(AuctionDetailActivity.this, MessageActivity.class);
                            intent.putExtra("userId", auction.getOwnerId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean showMenu = CommonMethods.showMenuOptionsForBook(auth.getUid(), auction);
        ;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_auction_detail, menu);
        return showMenu;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean showMenu = CommonMethods.showMenuOptionsForBook(auth.getUid(), auction);
        ;
        switch (item.getItemId()) {
            case R.id.showBids:
                showBids();
                break;
            case R.id.update:
                updateAuction();
                break;
            case R.id.delete:
                deleteAuction();
                break;
        }
        return showMenu;
    }

    private void showBids() {
        Bundle bundle = new Bundle();
        bundle.putString("auctionID", auctionId);
        Intent intent = new Intent(AuctionDetailActivity.this, BidListActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void deleteAuction() {
        // remove book id from user as well and then delete book
        // after that go back to my library
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                ArrayList<String> newAuctionIdList = new ArrayList<>();
                for (String auctionId : user.getAuctionIds()) {
                    if (!auctionId.equals(auction.getId())) {
                        newAuctionIdList.add(auctionId);
                    }
                }
                dbRef.child("auctionIds").setValue(newAuctionIdList);
                dbRef = firebaseInstance.getReference("auctions").child(auctionId);
                dbRef.removeValue();
                startActivity(new Intent(AuctionDetailActivity.this, MyLibraryActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateAuction() {
        Bundle bundle = new Bundle();
        bundle.putString("auctionID", auctionId);
        Intent intent = new Intent(AuctionDetailActivity.this, AddAuctionActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void placeBid() {
        if (TextUtils.isEmpty(binding.editPrice.getText())) {
            binding.errorMsg.setText("Please enter the bid amount!");
            return;
        }

        double originalPrice = Double.parseDouble(auction.getPrice());
        double bid = Double.parseDouble(binding.editPrice.getText().toString());

        if (bid <= originalPrice) {
            binding.errorMsg.setText("Bid amount must be greater than the current bid price!");
            return;
        }
        addOfferInUser(bid);
        binding.editPrice.clearFocus();
    }

    private void addOfferInUser(double bid) {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Offer offer = new Offer(auctionId, binding.editPrice.getText().toString(),
                        auction.getTitle());
                ArrayList<Offer> existingOffers = user.getMyOffers();
                if (existingOffers == null) {
                    existingOffers = new ArrayList<>();
                }
                existingOffers.add(offer);
                user.setMyOffers(existingOffers);
                dbRef.setValue(user);
                addOfferInAuction(user.getName(), bid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addOfferInAuction(String name, Double bidPrice) {
        dbRef = firebaseInstance.getReference("auctions").child(auctionId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BookAuction auction = snapshot.getValue(BookAuction.class);
                Bid bid = new Bid(auth.getUid(), binding.editPrice.getText().toString(), name);
                ArrayList<Bid> existingBids = auction.getBids();
                if (existingBids == null) {
                    existingBids = new ArrayList<>();
                }
                existingBids.add(bid);
                auction.setBids(existingBids);
                auction.setPrice(bidPrice.toString());
                dbRef.setValue(auction);
                Toast.makeText(AuctionDetailActivity.this, "An offer has been sent.",
                        Toast.LENGTH_SHORT).show();
                binding.editPrice.setText("");
                binding.errorMsg.setText("");
                binding.pricecurrent.setText(auction.getPrice());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}