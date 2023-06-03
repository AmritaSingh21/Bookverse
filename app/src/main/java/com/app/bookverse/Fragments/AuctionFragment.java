package com.app.bookverse.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.bookverse.AuctionDetailActivity;
import com.app.bookverse.BookDetail;
import com.app.bookverse.Entities.Book;
import com.app.bookverse.Entities.BookAuction;
import com.app.bookverse.R;
import com.app.bookverse.Fragments.placeholder.PlaceholderContent;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class AuctionFragment extends Fragment implements MyAuctionRecyclerViewAdapter.ItemClickListener{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    // data list
    ArrayList<BookAuction> dataList = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AuctionFragment() {
    }

    public AuctionFragment(ArrayList<BookAuction> list) {
        dataList = list;
    }

    @SuppressWarnings("unused")
    public static AuctionFragment newInstance(int columnCount) {
        AuctionFragment fragment = new AuctionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auction_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MyAuctionRecyclerViewAdapter adapter = new MyAuctionRecyclerViewAdapter(dataList);
            adapter.setClickListener(AuctionFragment.this);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("auctionID", dataList.get(position).getId());
        Intent intent = new Intent(AuctionFragment.this.getActivity(), AuctionDetailActivity.class);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }
}