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

import com.app.bookverse.Services.LocationService;
import com.app.bookverse.databinding.ActivitySigninBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SigninActivity extends AppCompatActivity {

    private static ActivitySigninBinding binding;
    static final String TAG = "SigninActivity";

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
    }

    public void signin(View view) {
        boolean flag = checkMandatoryFields();
        if (!flag) {
            return;
        }

        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(SigninActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInUserWithEmail:onComplete:" + task.isSuccessful());
                                if (!task.isSuccessful()) {
                                    Log.d(TAG, "Auth Failed." + task.getException());
                                    if (task.getException() instanceof FirebaseAuthInvalidUserException
                                            || task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        binding.errorMsg.setText("Invalid credentials.");
                                    }
                                } else {
                                    binding.errorMsg.setText("");
                                    startActivity(new Intent(SigninActivity.this,
                                            HomeActivity.class));
                                    // start location service
                                    Intent service = new Intent(SigninActivity.this, LocationService.class);
                                    startService(service);
                                    Toast.makeText(getApplicationContext(),
                                            "Successfully signed in", Toast.LENGTH_SHORT).show();
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

    private boolean checkMandatoryFields() {
        boolean flag = true;
        if (TextUtils.isEmpty(binding.email.getText())) {
            binding.email.setError(" Please enter your Email! ");
            flag = false;
        }
        if (TextUtils.isEmpty(binding.password.getText())) {
            binding.password.setError(" Please enter your Password! ");
            flag = false;
        }

        return flag;
    }
}