package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.app.bookverse.Adapters.MessagesAdapter;
import com.app.bookverse.Entities.Message;
import com.app.bookverse.Entities.User;
import com.app.bookverse.databinding.ActivityMessageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private static ActivityMessageBinding binding;
    private MessagesAdapter messagesAdapter;
    private List<Message> messages;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private String otherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);

        if (getIntent().getExtras() != null) {
            otherUserId = getIntent().getExtras().getString("userId");
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                binding.uName.setText(user.getName());
                readMessage(firebaseUser.getUid(), otherUserId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = binding.msgText.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(), otherUserId, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message.",
                            Toast.LENGTH_SHORT).show();
                }
                binding.msgText.setText("");
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        String[] parts = today.toString().split(" ");
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("sender", sender);
        hm.put("receiver", receiver);
        hm.put("message", message);
        hm.put("date_time", parts[0] + ", " + parts[3]);

        reference.child("messages").push().setValue(hm);

        // TODO notification to the sender.

    }

    private void readMessage(String myId, String userId) {
        messages = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Message message = snapshot1.getValue(Message.class);
                    if (message.getReceiver().equals(userId) && message.getSender().equals(myId)
                            || message.getReceiver().equals(myId) && message.getSender().equals(userId)) {
                        messages.add(message);
                    }
                    messagesAdapter = new MessagesAdapter(MessageActivity.this, messages);
                    binding.recyclerView.setAdapter(messagesAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}