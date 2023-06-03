package com.app.bookverse.Fragments;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bookverse.Entities.Book;
import com.app.bookverse.Fragments.placeholder.PlaceholderContent.PlaceholderItem;
import com.app.bookverse.databinding.FragmentBookBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 */
public class MyBookRecyclerViewAdapter extends RecyclerView.Adapter<MyBookRecyclerViewAdapter.ViewHolder> {

    private final List<Book> mValues;
    private ItemClickListener itemClickListener;

    public MyBookRecyclerViewAdapter(List<Book> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentBookBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Book book = mValues.get(position);
        holder.bookTitle.setText(book.getTitle());
        holder.bookAuthor.setText("By: " + book.getAuthor());
        holder.bookPrice.setText("$" + book.getPrice());

        // Fetch image
        String url = "books/" + book.getId() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(url);
        try {
            File file = File.createTempFile(book.getId(), "jpg");
            imageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    holder.bookImage.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView bookTitle, bookAuthor, bookPrice;
        ImageView bookImage;

        public ViewHolder(FragmentBookBinding binding) {
            super(binding.getRoot());
            bookTitle = binding.title;
            bookAuthor = binding.author;
            bookPrice = binding.price;
            bookImage = binding.bookImage;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(itemClickListener!=null)
                itemClickListener.onItemClick(v,getBindingAdapterPosition());
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}