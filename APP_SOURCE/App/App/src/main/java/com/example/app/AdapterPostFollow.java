package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

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

public class AdapterPostFollow extends RecyclerView.Adapter<AdapterPostFollow.ViewHolder> {
    private final LayoutInflater layoutInflater;
    List<String[]> posts;
    ConstraintLayout root;
    Activity context;
    int br = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout card;
        private final TextView name, desc;

        public ViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.textView15);
            desc = view.findViewById(R.id.textView);
            card = view.findViewById(R.id.line);
        }
    }

    AdapterPostFollow(Activity context, ConstraintLayout root, List<String[]> posts) {
        layoutInflater = LayoutInflater.from(context);
        this.root = root;
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public AdapterPostFollow.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item2, parent, false);
        return new AdapterPostFollow.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPostFollow.ViewHolder holder, int position) {
        String[] post = posts.get(position);

        holder.desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, Post.class).putExtra("ID", post[2]));
            }
        });



        if (post[3].equals("image")) {
            ImageView i = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(900, 700);
            params.gravity = Gravity.CENTER;
            i.setLayoutParams(params);
            i.setScaleType(ImageView.ScaleType.CENTER);
            Glide.with(context).load(post[0]).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    br++;
                    i.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.startActivity(new Intent(context, Post.class).putExtra("ID", post[2]));
                        }
                    });
                    holder.card.addView(i, 1);
                    return false;
                }
            }).into(i);
        } else {
            VideoView k = new VideoView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(900, 700);
            params.gravity = Gravity.CENTER;
            k.setLayoutParams(params);
            k.setVideoURI(Uri.parse(post[0]));
            holder.card.addView(k, 1);
            k.start();
            MediaController m = new MediaController(context);
            m.show();
            k.setMediaController(m);
            br++;
        }

        holder.name.setText(post[5]);
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, UserProfile.class).putExtra("id", post[4]));
            }
        });

        holder.desc.setText(post[1]);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
