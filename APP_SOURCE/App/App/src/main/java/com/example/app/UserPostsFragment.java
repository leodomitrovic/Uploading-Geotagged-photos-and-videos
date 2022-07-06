package com.example.app;

import android.app.Activity;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class UserPostsFragment extends Fragment {
    RecyclerView rv;
    List<String[]> posts_list;
    Activity activity;
    AdapterPost1 ap;
    ConstraintLayout root;

    public UserPostsFragment() {
        // Required empty public constructor
    }

    public UserPostsFragment(Activity activity, List<String[]> posts_list, ConstraintLayout root) {
        this.activity = activity;
        this.posts_list = posts_list;
        this.root = root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_posts, container, false);
        rv = view.findViewById(R.id.rv20);
        rv.setLayoutManager(new GridLayoutManager(activity.getApplicationContext(), 4, GridLayoutManager.VERTICAL, false));
        ap = new AdapterPost1(activity.getApplicationContext(), root, posts_list);
        rv.setAdapter(ap);
        return view;
    }
}