package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.ViewHolder> {
    private final LayoutInflater layoutInflater;
    List<String[]> comments;
    LinearLayout root;
    Activity context;
    static ProgressBar p;
    int br = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, comment;

        public ViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.textView15);
            comment = view.findViewById(R.id.textView);
        }
    }

    AdapterComments(Activity context, LinearLayout root, List<String[]> comments) {
        layoutInflater = LayoutInflater.from(context);
        this.root = root;
        this.context = context;
        this.comments = comments;
        p = root.findViewById(R.id.progressBar10);
    }

    @NonNull
    @Override
    public AdapterComments.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item2, parent, false);
        return new AdapterComments.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterComments.ViewHolder holder, int position) {
        String[] comment = comments.get(position);

        holder.name.setText(comment[3]);
        holder.comment.setText(comment[0]);

        if (comment[4].equals("3")) {
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, UserProfile.class).putExtra("id", comment[1]));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addItem(String[] comment) {
        comments.add(0, comment);
        notifyDataSetChanged();
    }
}
