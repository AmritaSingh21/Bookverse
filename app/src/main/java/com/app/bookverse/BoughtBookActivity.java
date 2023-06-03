package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Fragments.MyBookRecyclerViewAdapter;
import com.app.bookverse.Fragments.TabPagerAdapter;
import com.app.bookverse.databinding.ActivityBoughtBookBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class BoughtBookActivity extends AppCompatActivity
        implements MyBookRecyclerViewAdapter.ItemClickListener{
    
    private static ActivityBoughtBookBinding binding;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;
    private MyBookRecyclerViewAdapter adapter;

    private ArrayList<Book> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBoughtBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        binding.recyclerView.setLayoutManager(manager);

        fetchBooks();
    }

    private void fetchBooks() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                ArrayList<String> bookIdList = user.getBoughtBooks();
                if(bookIdList == null){
                    return;
                }
                fetchBookFromUser(bookIdList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchBookFromUser(ArrayList<String> bookIdList) {
        dbRef = firebaseInstance.getReference("books");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    bookList.clear();
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        Book book = dataSnapshot.getValue(Book.class);
                        if (bookIdList.contains(book.getId())) {
                            bookList.add(book);
                        }
                    }
                    adapter = new MyBookRecyclerViewAdapter(bookList);
                    binding.recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("bookID", bookList.get(position).getId());
        Intent intent = new Intent(BoughtBookActivity.this, BookDetail.class);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }
}