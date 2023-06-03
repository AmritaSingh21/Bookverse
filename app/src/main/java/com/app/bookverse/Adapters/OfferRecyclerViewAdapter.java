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
import com.app.bookverse.Entities.Offer;
import com.app.bookverse.Fragments.MyAuctionRecyclerViewAdapter;
import com.app.bookverse.databinding.BidListItemBinding;
import com.app.bookverse.databinding.FragmentAuctionBinding;
import com.app.bookverse.databinding.OfferListItemBinding;

import java.util.ArrayList;


public class OfferRecyclerViewAdapter extends RecyclerView.Adapter<OfferRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Offer> offers = new ArrayList<>();

    public OfferRecyclerViewAdapter() {
    }

    public OfferRecyclerViewAdapter(ArrayList<Offer> offers) {
        this.offers = offers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                OfferListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.title.setText(offer.getTitle());
        holder.price.setText("$" + offer.getPrice());
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;

        public ViewHolder(OfferListItemBinding binding) {
            super(binding.getRoot());
            title = binding.title;
            price = binding.price;
        }
    }
}
