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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PopularFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    RecyclerView rv;
    ConstraintLayout root;
    AdapterPostFollow ap;
    List<String[]> posts_list;
    Activity activity;
    List<String[]> users;

    public PopularFragment() {
        // Required empty public constructor
    }

    public PopularFragment (Activity activity) {
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
        View view = inflater.inflate(R.layout.activity_popular, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        getUsers(activity.getApplicationContext());
        rv = view.findViewById(R.id.rv6);
        root = view.findViewById(R.id.root5);
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
                        if (doc.getBoolean("Approved")) i++;
                    }
                    posts_list = new ArrayList<>(i);
                    i = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getBoolean("Approved")) {
                            for (int k = 0; k < users.size(); k++) {
                                if (doc.getString("User-ID").equals(users.get(k)[0])) {
                                    String[] pom = new String[7];
                                    pom[0] = doc.getString("Image URL");
                                    pom[1] = doc.getString("Description");
                                    pom[2] = doc.getId();
                                    pom[3] = doc.getString("Type");
                                    pom[4] = users.get(k)[0];
                                    pom[5] = users.get(k)[1];
                                    pom[6] = String.valueOf(doc.getLong(("Views")));
                                    posts_list.add(pom);
                                    i++;
                                }
                            }
                        }
                    }
                    if (posts_list.size() > 1) sort(context);
                    else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ap = new AdapterPostFollow(activity, root, posts_list);
                                rv.setAdapter(ap);
                            }
                        });
                    }
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void sort(Context context) {
        Collections.sort(posts_list, new Comparator<String[]>(){
            public int compare(String[] obj1, String[] obj2) {
                if (Integer.valueOf(obj1[6]) == Integer.valueOf(obj2[6])) {
                    return 0;
                } else if (Integer.valueOf(obj1[6]) > Integer.valueOf(obj2[6])) {
                    return -1;
                }
                return 1;
            }
        });

        if (posts_list.size() > 5) posts_list = posts_list.subList(0, 5);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ap = new AdapterPostFollow(activity, root, posts_list);
                rv.setAdapter(ap);
            }
        });
    }

    void getUsers(Context context) {
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();

                    users = new ArrayList<>(queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String[] pom = new String[2];
                        pom[0] = doc.getString("ID");
                        pom[1] = doc.getString("Full name");
                        users.add(pom);
                    }
                    getPosts(activity.getApplicationContext());
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}