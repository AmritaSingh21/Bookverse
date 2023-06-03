package com.app.bookverse;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Services.CaptureAct;
import com.app.bookverse.databinding.ActivityAddAuctionBinding;
import com.app.bookverse.databinding.ActivityAddBookBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;

public class AddBookActivity extends AppCompatActivity {

    private static ActivityAddBookBinding binding;
    private static final String TAG = "AddBookActivity";
    private static FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;
    private StorageReference storageRef;
    private String bookId = "";
    private String updateBookId;
    private boolean isUpdate = false;

    private Uri imageUri = null;

    private String title, author, year, genre, isbn, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // if updating book
        if (getIntent().getExtras() != null &&
                getIntent().getExtras().get("bookID") != null) {
            updateBookId = getIntent().getExtras().get("bookID").toString();
            isUpdate = true;
            binding.header.setText("Update Book Details");
            binding.addBook.setText("Update Book");
            showBookValues();
        }
    }

    private void showBookValues() {
        dbRef = firebaseInstance.getReference("books").child(updateBookId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book book = snapshot.getValue(Book.class);
                binding.editTitle.setText(book.getTitle());
                binding.editAuthor.setText(book.getAuthor());
                binding.editPrice.setText(book.getPrice());

                if (book.getYear() != null) {
                    binding.editYear.setText(book.getYear());
                }
                if (book.getGenre() != null) {
                    binding.editGenre.setText(book.getGenre());
                }
                if (book.getIsbn() != null) {
                    binding.editIsbn.setText(book.getIsbn());
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
                            binding.previewImage.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addBook(View view) {
        boolean flag = checkMandatoryFields();
        if (!flag) {
            return;
        }

        title = binding.editTitle.getText().toString();
        author = binding.editAuthor.getText().toString();
        year = binding.editYear.getText().toString();
        genre = binding.editGenre.getText().toString();
        isbn = binding.editIsbn.getText().toString();
        price = binding.editPrice.getText().toString();

        if (isUpdate) {
            updateBookInDB(title, author, year, genre, isbn, price);
            return;
        }

        dbRef = firebaseInstance.getReference("books");
        bookId = dbRef.push().getKey();

        // upload image and get the image url
        String url = uploadAndFetchImageUrl();

        Book book = new Book(bookId, title, author, genre, year, isbn, price,
                url, auth.getUid());

        dbRef.child(bookId).setValue(book);
        Log.d(TAG, "Book created.");
        // add book id in user
        addBookIdToUser();
    }

    private void updateBookInDB(String title, String author, String year,
                                String genre, String isbn, String price) {
        dbRef = firebaseInstance.getReference("books").child(updateBookId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book book = snapshot.getValue(Book.class);
                book.setTitle(title);
                book.setAuthor(author);
                book.setYear(year);
                book.setGenre(genre);
                book.setIsbn(isbn);
                book.setPrice(price);
                if (imageUri != null) {
                    String url = uploadAndFetchImageUrl();
                    book.setPicUrl(url);
                }
                dbRef.setValue(book);
                startActivity(new Intent(AddBookActivity.this, MyLibraryActivity.class));
                // TODO check if we can go back to details and then use back button for library
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addBookIdToUser() {
        dbRef = firebaseInstance.getReference("users");
        String userId = auth.getCurrentUser().getUid();
        dbRef.child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    User user = task.getResult().getValue(User.class);
                    ArrayList<String> bookIds = user.getBookIds();
                    if (bookIds == null) {
                        bookIds = new ArrayList<>();
                    }
                    bookIds.add(bookId);
                    dbRef.child(userId).child("bookIds").setValue(bookIds);
                    startActivity(new Intent(AddBookActivity.this,
                            MyLibraryActivity.class));
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkMandatoryFields() {
        boolean flag = true;
        if (TextUtils.isEmpty(binding.editTitle.getText())) {
            binding.editTitle.setError(" Please enter Title! ");
            flag = false;
        }
        if (TextUtils.isEmpty(binding.editAuthor.getText())) {
            binding.editAuthor.setError(" Please enter Author name! ");
            flag = false;
        }
        if (!TextUtils.isEmpty(binding.editYear.getText())) {
            int yearInt = Integer.parseInt(binding.editYear.getText().toString());
            Year currYear = Year.now();
            if (yearInt > currYear.getValue()) {
                binding.editYear.setError(" Please enter valid year! ");
                flag = false;
            }
        }
        if (TextUtils.isEmpty(binding.editPrice.getText())) {
            binding.editPrice.setError(" Please enter Price! ");
            flag = false;
        }
        if (imageUri == null && !isUpdate) {
            binding.selectImage.setError(" Please upload an image! ");
            flag = false;
        }
        if(!flag){
            binding.errorMsg.setText("Please check all fields for errors.");
        }
        return flag;
    }

    private String uploadAndFetchImageUrl() {
        String url = "";
        if (isUpdate) {
            url = "books/" + updateBookId + ".jpg";
        } else {
            url = "books/" + bookId + ".jpg";
        }
        StorageReference imageRef = storageRef.child(url);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(
                                    UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "Image uploaded successfully.");
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error Occurred while uploading image.");
                    }
                });
        return url;
    }

    public void chooseImage(View view) {
        // create an instance of the intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        imageUri = selectedImageUri;
                        Bitmap selectedImageBitmap = null;
                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(), selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error occurred while getting image.");
                        }
                        binding.previewImage.setVisibility(View.VISIBLE);
                        binding.previewImage.setImageBitmap(selectedImageBitmap);
                    }
                } else {
                    Log.d(TAG, "Error occurred while getting image with result code: "
                            + result.getResultCode());
                }
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                    scanBook();
                break;
        }
        return true;
    }

    private void scanBook() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Volume up to flash on");
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            // fetch data and populate
            String isbn = result.getContents();
            dbRef = firebaseInstance.getReference("books_inventory").child(isbn);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Book book = snapshot.getValue(Book.class);
                    binding.editTitle.setText(book.getTitle());
                    binding.editAuthor.setText(book.getAuthor());
                    binding.editIsbn.setText(book.getIsbn());
                    binding.editGenre.setText(book.getGenre());
                    binding.editYear.setText(book.getYear());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else{
            Toast.makeText(AddBookActivity.this, "Book not found!", Toast.LENGTH_SHORT).show();
        }
    });
}