package com.app.bookverse.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.bookverse.BookDetail;
import com.app.bookverse.Entities.Book;
import com.app.bookverse.MyLibraryActivity;
import com.app.bookverse.R;
import com.app.bookverse.Fragments.placeholder.PlaceholderContent;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class BookFragment extends Fragment implements MyBookRecyclerViewAdapter.ItemClickListener{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 2;

    // data list
    ArrayList<Book> dataList = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookFragment() {
    }

    public BookFragment(ArrayList<Book> list) {
        dataList = list;
    }

    @SuppressWarnings("unused")
    public static BookFragment newInstance(int columnCount) {
        BookFragment fragment = new BookFragment();
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
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MyBookRecyclerViewAdapter adapter = new MyBookRecyclerViewAdapter(dataList);
            adapter.setClickListener(BookFragment.this);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("bookID", dataList.get(position).getId());
        Intent intent = new Intent(BookFragment.this.getActivity(), BookDetail.class);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }
}