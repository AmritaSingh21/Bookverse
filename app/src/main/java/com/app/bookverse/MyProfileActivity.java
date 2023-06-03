package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Services.LocationService;
import com.app.bookverse.Utilities.CommonMethods;
import com.app.bookverse.databinding.ActivityMyProfileBinding;
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

public class MyProfileActivity extends AppCompatActivity {

    private static ActivityMyProfileBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        setSupportActionBar(binding.myToolbar);

        navigationMenu();

        fetchUserInfo();
    }

    private void fetchUserInfo() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                binding.name.setText(user.getName());
                binding.email.setText(user.getEmail());
                if (user.getAddressLine() != null && !user.getAddressLine().isEmpty()) {
                    binding.addressLine.setText(user.getAddressLine());
                } else {
                    binding.addressLine.setText("-");
                }
                binding.city.setText(user.getCity());
                binding.province.setText(user.getProvince());
                binding.country.setText(user.getCountry());
                binding.zip.setText(user.getPostalCode());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                break;
            case R.id.updateProfile:
                startActivity(new Intent(MyProfileActivity.this, UpdateProfileActivity.class));
                break;
            case R.id.boughtBooks:
                startActivity(new Intent(MyProfileActivity.this, BoughtBookActivity.class));
                break;
            case R.id.boughtAuctions:
                startActivity(new Intent(MyProfileActivity.this, AuctionsBoughtActivity.class));
                break;
            case R.id.myOffers:
                startActivity(new Intent(MyProfileActivity.this, MyOfferListActivity.class));
                break;
            case R.id.chats:
                startActivity(new Intent(MyProfileActivity.this, ChatListActivity.class));
                break;
        }
        return true;
    }

    private void logout() {
        auth.signOut();
        startActivity(new Intent(MyProfileActivity.this, MainActivity.class));
        Toast.makeText(getApplicationContext(),
                "Successfully signed out", Toast.LENGTH_SHORT).show();
        // stop location service
        Intent service = new Intent(MyProfileActivity.this, LocationService.class);
        stopService(service);
    }

    /**
     * Bottom Navigation Menu
     */
    private void navigationMenu() {
        binding.bottomMenu.setSelectedItemId(R.id.menu_myprofile);
        binding.bottomMenu.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case (R.id.menu_home):
                    startActivity(new Intent(MyProfileActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case (R.id.menu_auction):
                    startActivity(new Intent(MyProfileActivity.this, AuctionActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                case (R.id.menu_mybooks):
                    startActivity(new Intent(MyProfileActivity.this, MyLibraryActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    break;
                default:
                    break;
            }
            return true;
        });
    }
}