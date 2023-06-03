package com.app.bookverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Fragments.BookFragment;
import com.app.bookverse.Utilities.CommonMethods;
import com.app.bookverse.databinding.ActivityBookDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class BookDetail extends AppCompatActivity {

    private ActivityBookDetailBinding binding;
    private static final String TAG = "BookDetail";
    private String bookId;
    private Book book;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setSupportActionBar(binding.myToolbar);

        //get book id from bundle
        if (getIntent().getExtras() != null &&
                getIntent().getExtras().get("bookID") != null) {
            bookId = getIntent().getExtras().get("bookID").toString();
        }

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();

        fetchBookDetail();

    }

    private void showBuyBookOptionForOtherUser() {
        boolean showBuyOptionForBook = CommonMethods.showBuyOptionForBook(auth.getUid(), book);

        if (showBuyOptionForBook) {
            binding.buyBookLayout.setVisibility(View.VISIBLE);
            binding.buyBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyBook();
                }
            });
        }
    }

    private void buyBook() {
        dbRef = firebaseInstance.getReference("books").child(bookId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book book = snapshot.getValue(Book.class);
                book.setSold(true);
                dbRef.setValue(book);
                addBoughtBookInUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addBoughtBookInUser() {
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                ArrayList<String> boughtBooks = user.getBoughtBooks();
                if (boughtBooks == null) {
                    boughtBooks = new ArrayList<>();
                }
                boughtBooks.add(bookId);
                user.setBoughtBooks(boughtBooks);
                dbRef.setValue(user);
                Toast.makeText(BookDetail.this, "Book Bought Successfully.",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(BookDetail.this, HomeActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchBookDetail() {
        dbRef = firebaseInstance.getReference("books").child(bookId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                book = snapshot.getValue(Book.class);
                binding.title.setText(book.getTitle());
                binding.author.setText(book.getAuthor());
                binding.price.setText(book.getPrice());

                if (book.getYear() != null && !book.getYear().isEmpty()) {
                    binding.year.setText(book.getYear());
                } else {
                    binding.year.setText("-");
                }
                if (book.getGenre() != null && !book.getGenre().isEmpty()) {
                    binding.genre.setText(book.getGenre());
                } else {
                    binding.genre.setText("-");
                }
                if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                    binding.isbn.setText(book.getIsbn());
                } else {
                    binding.isbn.setText("-");
                }

                if(book.isSold()){
                    binding.status.setVisibility(View.VISIBLE);
                }
                // Fetch image
                String url = book.getPicUrl();
                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(url);
                try {
                    File file = File.createTempFile(book.getId(), "jpg");
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
                showBuyBookOptionForOtherUser();
                if (CommonMethods.showMenuOptionsForBook(auth.getUid(), book)) {
                    setSupportActionBar(binding.myToolbar);
                }

                if (auth.getUid().equals(book.getOwnerId())) {
                    binding.btnMessage.setVisibility(View.INVISIBLE);
                } else {

                    binding.btnMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(BookDetail.this, MessageActivity.class);
                            intent.putExtra("userId", book.getOwnerId());
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
        Log.d(TAG, "inside onCreateOptionsMenu");
        boolean showMenu = CommonMethods.showMenuOptionsForBook(auth.getUid(), book);
        Log.d(TAG, "showMenu: " + showMenu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_book_detail, menu);
        return showMenu;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "inside onOptionsItemSelected");
        boolean showMenu = CommonMethods.showMenuOptionsForBook(auth.getUid(), book);
        switch (item.getItemId()) {
            case R.id.update:
                updateBook();
                break;
            case R.id.delete:
                deleteBook();
                break;
        }
        return showMenu;
    }

    private void deleteBook() {
        // remove book id from user as well and then delete book
        // after that go back to my library
        dbRef = firebaseInstance.getReference("users").child(auth.getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                ArrayList<String> newBookIdList = new ArrayList<>();
                for (String bookId : user.getBookIds()) {
                    if (!bookId.equals(book.getId())) {
                        newBookIdList.add(bookId);
                    }
                }
                dbRef.child("bookIds").setValue(newBookIdList);
                dbRef = firebaseInstance.getReference("books").child(bookId);
                dbRef.removeValue();
                startActivity(new Intent(BookDetail.this, MyLibraryActivity.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateBook() {
        Bundle bundle = new Bundle();
        bundle.putString("bookID", bookId);
        Intent intent = new Intent(BookDetail.this, AddBookActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}