package com.example.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UncheckedFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    RecyclerView rv;
    ConstraintLayout root;
    AdapterPost ap;
    List<String[]> posts_list;
    Activity activity;

    public UncheckedFragment() {
        // Required empty public constructor
    }

    public UncheckedFragment(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_unchecked, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        getPosts(activity.getApplicationContext());
        rv = view.findViewById(R.id.rv20);
        root = view.findViewById(R.id.root);
        rv.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        return view;
    }

    void getPosts(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("Posts");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    int i = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (!doc.getBoolean("Approved")) i++;
                    }
                    posts_list = new ArrayList<>(i);
                    i = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (!doc.getBoolean("Approved")) {
                            String[] pom = new String[4];
                            pom[0] = doc.getString("Image URL");
                            pom[1] = doc.getString("Description");
                            GeoPoint g = doc.getGeoPoint("GeoTag");
                            pom[2] = doc.getId();
                            pom[3] = doc.getString("Type");
                            posts_list.add(pom);
                            i++;
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (posts_list.size() > 0) {
                                ap = new AdapterPost(context, root, posts_list, "admin");
                                rv.setAdapter(ap);
                            }
                        }
                    });
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}