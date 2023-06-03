package com.app.bookverse.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bookverse.Entities.Bid;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.Fragments.MyAuctionRecyclerViewAdapter;
import com.app.bookverse.databinding.BidListItemBinding;
import com.app.bookverse.databinding.FragmentAuctionBinding;

import java.util.ArrayList;


public class BidRecyclerViewAdapter extends RecyclerView.Adapter<BidRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Bid> bids = new ArrayList<>();

    public BidRecyclerViewAdapter() {
    }

    public BidRecyclerViewAdapter(ArrayList<Bid> bids) {
        this.bids = bids;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                BidListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bid bid = bids.get(position);
        holder.name.setText(bid.getUserName());
        holder.price.setText("$" + bid.getPrice());
    }

    @Override
    public int getItemCount() {
        return bids.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;

        public ViewHolder(BidListItemBinding binding) {
            super(binding.getRoot());
            name = binding.name;
            price = binding.price;
        }
    }
}
