package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class AdapterPost1 extends RecyclerView.Adapter<AdapterPost1.ViewHolder> {
    private final LayoutInflater layoutInflater;
    List<String[]> posts;
    ConstraintLayout root;
    Context context;
    int br = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;

        public ViewHolder(View view) {
            super(view);

            icon = view.findViewById(R.id.card_image);
            icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    AdapterPost1(Context context, ConstraintLayout root, List<String[]> posts) {
        layoutInflater = LayoutInflater.from(context);
        this.root = root;
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public AdapterPost1.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPost1.ViewHolder holder, int position) {
        String[] post = posts.get(position);
        Glide.with(context).load(post[0]).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                br++;
                return false;
            }
        }).into(holder.icon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, Post.class).putExtra("ID", post[1]).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
