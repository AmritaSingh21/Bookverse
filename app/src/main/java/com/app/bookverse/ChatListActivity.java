package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.app.bookverse.Adapters.ChatsAdapter;
import com.app.bookverse.Entities.Message;
import com.app.bookverse.Entities.User;
import com.app.bookverse.databinding.ActivityChatListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private static ActivityChatListBinding binding;
    private ArrayList<User> users;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private List<String> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();

                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Message message = snapshot1.getValue(Message.class);
                    if(message.getSender().equals(firebaseUser.getUid())){
                        usersList.add(message.getReceiver());
                    }
                    if(message.getReceiver().equals(firebaseUser.getUid())){
                        usersList.add(message.getSender());
                    }
                }
                readMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        users = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();

                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);

                    for(String id: usersList){
                        if (user.getId().equals(id) && !users.contains(user)){
                            users.add(user);
                        }
                    }
                }

                ChatsAdapter adapter = new ChatsAdapter(ChatListActivity.this, users);
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}