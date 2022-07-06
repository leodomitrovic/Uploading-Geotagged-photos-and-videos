package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.ViewHolder> {
    private final LayoutInflater layoutInflater;
    List<String[]> posts;
    ConstraintLayout root;
    Context context;
    String user_type;
    static ProgressBar p;
    int br = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView desc;
        private final ImageView icon;
        private final Button b1, b2;

        public ViewHolder(View view) {
            super(view);

            desc = view.findViewById(R.id.textView7);
            icon = view.findViewById(R.id.imageView2);
            b1 = view.findViewById(R.id.button5);
            b2 = view.findViewById(R.id.button8);
        }
    }

    AdapterPost(Context context, ConstraintLayout root, List<String[]> posts, String user_type) {
        layoutInflater = LayoutInflater.from(context);
        this.root = root;
        this.context = context;
        this.posts = posts;
        this.user_type = user_type;
        p = root.findViewById(R.id.progressBar8);
    }

    @NonNull
    @Override
    public AdapterPost.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPost.ViewHolder holder, int position) {
        String[] artist = posts.get(position);
        holder.desc.setText(artist[1]);
        if (user_type.equals("admin")) holder.b1.setVisibility(View.VISIBLE);
        else holder.b1.setVisibility(View.INVISIBLE);
        if (artist[3].equals("image")) {
            Glide.with(context).load(artist[0]).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    br++;
                    if (br == getItemCount()) p.setVisibility(View.INVISIBLE);
                    return false;
                }
            }).into(holder.icon);
        } else {
            Glide.with(context).load(artist[0]).thumbnail(Glide.with(context).load(artist[0])).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    br++;
                    if (br == getItemCount()) p.setVisibility(View.INVISIBLE);
                    return false;
                }
            }).into(holder.icon);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, Post.class).putExtra("ID", artist[2]).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        holder.b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference colRef = db.collection("Posts");
                colRef.document(artist[2]).update("Approved", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            posts.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Approved successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        holder.b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                storage.getReferenceFromUrl(artist[0]).delete();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Posts").document(artist[2]).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_LONG).show();
                            posts.remove(position);
                            notifyDataSetChanged();

                        } else {
                            Toast.makeText(context, task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

}
