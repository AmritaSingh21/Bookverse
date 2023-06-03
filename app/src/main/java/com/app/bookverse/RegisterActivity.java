package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.app.bookverse.Entities.User;
import com.app.bookverse.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static ActivityRegisterBinding binding;
    private static final String TAG = "RegisterActivity";

    private static FirebaseAuth auth;
    private String userId = "";

    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Auth instance
        auth = FirebaseAuth.getInstance();

        //DB instance
        firebaseInstance = FirebaseDatabase.getInstance();
        firebaseInstance.getReference("app_title").setValue("Bookverse");
    }

    public void register(View view) {
        String email = binding.editEmail.getText().toString();
        String password = binding.editPassword.getText().toString();

        boolean flag = checkMandatoryFields();
        if (!flag) {
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                                if (!task.isSuccessful()) {
                                    Log.d(TAG, "Auth Failed." + task.getException());
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        binding.errorMsg.setText("Email already registered.");
                                    }
                                } else {
                                    binding.errorMsg.setText("");
                                    createUser();
                                    startActivity(new Intent(RegisterActivity.this,
                                            HomeActivity.class));
                                    Toast.makeText(getApplicationContext(),
                                            "Successfully registered", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
    }

    /**
     * This method checks the mandatory fields
     * and gives appropriate error message
     *
     * @return
     */
    private boolean checkMandatoryFields() {
        boolean flag = true;
        if (TextUtils.isEmpty(binding.editEmail.getText())) {
            binding.editEmail.setError(" Please Enter Email! ");
            flag = false;
        }
        if (TextUtils.isEmpty(binding.editPassword.getText())) {
            binding.editPassword.setError(" Please Enter Password! ");
            flag = false;
        } else if (binding.editPassword.getText().toString().length() < 6) {
            binding.editPassword.setError(" Password must be at least 6 characters long. ");
            flag = false;
        }
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

    /**
     * This method creates user entity in db
     */
    private void createUser() {
        Log.d(TAG, "Going to create user.");
        User user = new User(auth.getUid(), binding.editName.getText().toString(),
                binding.editEmail.getText().toString(), binding.editAddress.getText().toString(),
                binding.editCity.getText().toString(), binding.editProvince.getText().toString(),
                binding.editCountry.getText().toString(), binding.editZip.getText().toString());
        dbRef = firebaseInstance.getReference("users");
        dbRef.child(auth.getUid()).setValue(user);
        Log.d(TAG, "User created.");
    }
}