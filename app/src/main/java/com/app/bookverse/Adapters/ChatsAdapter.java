package com.app.bookverse.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bookverse.ChatListActivity;
import com.app.bookverse.Entities.User;
import com.app.bookverse.MessageActivity;
import com.app.bookverse.databinding.ChatItemBinding;

import java.util.ArrayList;


public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private ArrayList<User> users = new ArrayList<>();
    private Context context;

    public ChatsAdapter() {
    }

    public ChatsAdapter(ArrayList<User> users) {
        this.users = users;
    }

    public ChatsAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ChatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.uName.setText(user.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId",user.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView uName;

        public ViewHolder(ChatItemBinding binding) {
            super(binding.getRoot());
            uName = binding.uName;
        }
    }
}
