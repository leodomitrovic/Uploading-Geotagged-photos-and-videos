package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.ViewHolder> {
    private final LayoutInflater layoutInflater;
    List<String[]> users;
    ConstraintLayout root;
    Activity context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, gone;

        public ViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.textView);
            gone = view.findViewById(R.id.textView15);
        }
    }

    AdapterUsers(Activity context, ConstraintLayout root, List<String[]> users) {
        layoutInflater = LayoutInflater.from(context);
        this.root = root;
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public AdapterUsers.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item2, parent, false);
        return new AdapterUsers.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUsers.ViewHolder holder, int position) {
        holder.gone.setVisibility(View.GONE);
        String[] user = users.get(position);
        holder.name.setText(user[0]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, UserProfile.class).putExtra("id", user[1]));
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
