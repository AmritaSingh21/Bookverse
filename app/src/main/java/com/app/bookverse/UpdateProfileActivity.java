package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.app.bookverse.Entities.User;
import com.app.bookverse.databinding.ActivityUpdateProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateProfileActivity extends AppCompatActivity {

    private static ActivityUpdateProfileBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        fetchUserInfo();
    }

    private void fetchUserInfo() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                binding.editName.setText(user.getName());
                if(user.getAddressLine() != null && !user.getAddressLine().isEmpty()){
                    binding.editAddress.setText(user.getAddressLine());
                } else{
                    binding.editAddress.setText("");
                }
                binding.editCity.setText(user.getCity());
                binding.editProvince.setText(user.getProvince());
                binding.editCountry.setText(user.getCountry());
                binding.editZip.setText(user.getPostalCode());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void updateProfile(View view) {
        boolean flag = checkMandatoryFields();
        if (!flag) {
            return;
        }

        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                user.setName(binding.editName.getText().toString());
                user.setAddressLine(binding.editAddress.getText().toString());
                user.setCity(binding.editCity.getText().toString());
                user.setProvince(binding.editProvince.getText().toString());
                user.setCountry(binding.editCountry.getText().toString());
                user.setPostalCode(binding.editZip.getText().toString());
                dbRef.setValue(user);
                startActivity(new Intent(UpdateProfileActivity.this, MyProfileActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean checkMandatoryFields() {
        boolean flag = true;
        if (TextUtils.isEmpty(binding.editName.getText())) {
            binding.editName.setError(" Please Enter your Name! ");
            flag = false;
        }
        if (TextUtils.isEmpty(binding.editCity.getText())) {
            binding.editCity.setError(" Please Enter your City! ");
            flag = false;
        }
        if (TextUtils.isEmpty(binding.editProvince.getText())) {
            binding.editProvince.setError(" Please Enter your Province! ");
            flag = false;
        }
        if (TextUtils.isEmpty(binding.editCountry.getText())) {
            binding.editCountry.setError(" Please Enter your Country! ");
            flag = false;
        }
        if (TextUtils.isEmpty(binding.editZip.getText())) {
            binding.editZip.setError(" Please Enter your Postal Code! ");
            flag = false;
        }

        return flag;
    }
}