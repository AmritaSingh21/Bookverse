package com.app.bookverse;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Entities.User;
import com.app.bookverse.Services.CaptureAct;
import com.app.bookverse.databinding.ActivityAddAuctionBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.IOException;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;

public class AddAuctionActivity extends AppCompatActivity {

    private static ActivityAddAuctionBinding binding;
    private static final String TAG = "AddAuctionActivity";
    private static FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseDatabase firebaseInstance;
    private StorageReference storageRef;
    private String auctionId = "";
    private String updateAuctionId;
    private boolean isUpdate = false;

    private Uri imageUri = null;

    private String title, author, year, genre, isbn, price, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAuctionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);

        auth = FirebaseAuth.getInstance();
        firebaseInstance = FirebaseDatabase.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // if updating book
        if (getIntent().getExtras() != null &&
                getIntent().getExtras().get("auctionID") != null) {
            updateAuctionId = getIntent().getExtras().get("auctionID").toString();
            isUpdate = true;
            binding.header.setText("Update Auction Details");
            binding.addBook.setText("Update Auction");
            showAuctionValues();
        }
    }

    private void showAuctionValues() {
        dbRef = firebaseInstance.getReference("auctions").child(updateAuctionId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BookAuction book = snapshot.getValue(BookAuction.class);
                binding.editTitle.setText(book.getTitle());
                binding.editAuthor.setText(book.getAuthor());
                binding.editPrice.setText(book.getStartingPrice());
                binding.endDate.setText(book.getEndTime());


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
    public void selectDate(View view) {
        binding.errorMsg.setText("");
        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String mStr = "";
                        String dStr = "";
                        if (month <= 9) {
                            mStr = "0";
                        }
                        if (dayOfMonth <= 9) {
                            dStr = "0";
                        }
                        binding.endDate.setText(mStr + month + "/" + dStr + dayOfMonth + "/" + year);
//                binding.endDate.setText("23:59:59");
                    }
                }, Year.now().getValue(), MonthDay.now().getMonthValue(), MonthDay.now().getDayOfMonth());
        dialog.show();
        Log.d(TAG, "Month: " + MonthDay.now().getMonthValue());
    }

//    public void selectTime(View view) {
//        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
//            @Override
//            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                binding.endDate.setText(hourOfDay + ":" + minute + ":00");
//            }
//        }, 12, 0, false);
//        dialog.show();
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addAuction(View view) {
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
//        endTime = binding.endDate.getText().toString() + " " + binding.endTime.getText().toString();
        endTime = binding.endDate.getText().toString();

        if (isUpdate) {
            updateAuctionInDB(title, author, year, genre, isbn, price, endTime);
            return;
        }

        dbRef = firebaseInstance.getReference("auctions");
        auctionId = dbRef.push().getKey();

        // upload image and get the image url
        String url = uploadAndFetchImageUrl();

        BookAuction book = new BookAuction(null, endTime, auctionId, title, author, genre, year, isbn, price,
                url, auth.getUid());
        book.setStartingPrice(price);

        dbRef.child(auctionId).setValue(book);
        Log.d(TAG, "Auction created.");
        // add auction id in user
        addAuctionIdToUser();

        startActivity(new Intent(AddAuctionActivity.this,
                MyLibraryActivity.class));
    }

    private void updateAuctionInDB(String title, String author, String year,
                                   String genre, String isbn, String price, String endTime) {
        dbRef = firebaseInstance.getReference("auctions").child(updateAuctionId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BookAuction auction = snapshot.getValue(BookAuction.class);
                auction.setTitle(title);
                auction.setAuthor(author);
                auction.setYear(year);
                auction.setGenre(genre);
                auction.setIsbn(isbn);
                auction.setStartingPrice(price);
                auction.setEndTime(endTime);
                if (imageUri != null) {
                    String url = uploadAndFetchImageUrl();
                    auction.setPicUrl(url);
                }
                dbRef.setValue(auction);
                startActivity(new Intent(AddAuctionActivity.this, MyLibraryActivity.class));
                // TODO check if we can go back to details and then use back button for library
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addAuctionIdToUser() {
        dbRef = firebaseInstance.getReference("users");
        String userId = auth.getCurrentUser().getUid();
        dbRef.child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    User user = task.getResult().getValue(User.class);
                    ArrayList<String> userAuctionIds = user.getAuctionIds();
                    if (userAuctionIds == null) {
                        userAuctionIds = new ArrayList<>();
                    }
                    userAuctionIds.add(auctionId);
                    dbRef.child(userId).child("auctionIds").setValue(userAuctionIds);
                }
            }
        });
    }

    private String uploadAndFetchImageUrl() {
        String url = "";
        if (isUpdate) {
            url = "auctions/" + updateAuctionId + ".jpg";
        } else {
            url = "auctions/" + auctionId + ".jpg";
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
        if (TextUtils.isEmpty(binding.endDate.getText())) {
            binding.errorMsg.setText(" Please enter End Time! ");
            flag = false;
        }

        return flag;
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
            Toast.makeText(AddAuctionActivity.this, "Book not found!", Toast.LENGTH_SHORT).show();
        }
    });
}