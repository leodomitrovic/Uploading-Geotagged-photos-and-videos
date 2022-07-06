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

public class BestRateFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    RecyclerView rv;
    ConstraintLayout root;
    AdapterPostFollow ap;
    List<String[]> posts_list;
    Activity activity;
    List<String[]> users;

    public BestRateFragment() {
        // Required empty public constructor
    }


    public BestRateFragment (Activity activity) {
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
        View view = inflater.inflate(R.layout.fragment_best_rate, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        getUsers();
        rv = view.findViewById(R.id.rv15);
        root = view.findViewById(R.id.root15);
        rv.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        return view;
    }

    void get_rates(Context context) {
        db.collection("Rating").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    if (queryDocumentSnapshots.size() == 0) {
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        for (int i = 0; i < posts_list.size(); i++) {
                            if (doc.getString("Post-ID").equals(posts_list.get(i)[2])) {
                                double sum = Double.valueOf(posts_list.get(i)[6]) + doc.getDouble("Rate");
                                double num = Double.valueOf(posts_list.get(i)[7]) + 1;
                                posts_list.get(i)[6] = String.valueOf(sum);
                                posts_list.get(i)[7] = String.valueOf(num);
                            }
                        }
                    }

                    for (int i = 0; i < posts_list.size(); i++) {
                        double sum = Double.valueOf(posts_list.get(i)[6]);
                        double num = Double.valueOf(posts_list.get(i)[7]);
                        if (num > 0) {
                            posts_list.get(i)[6] = String.valueOf(sum / num);
                        }
                    }

                    if (posts_list.size() > 1) sort();
                    else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ap = new AdapterPostFollow(activity, root, posts_list);
                                rv.setAdapter(ap);
                            }
                        });
                    }
                }
            }
        });
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
                                    String[] pom = new String[8];
                                    pom[0] = doc.getString("Image URL");
                                    pom[1] = doc.getString("Description");
                                    pom[2] = doc.getId();
                                    pom[3] = doc.getString("Type");
                                    pom[4] = users.get(k)[0];
                                    pom[5] = users.get(k)[1];
                                    pom[6] = "0";
                                    pom[7] = "0";
                                    posts_list.add(pom);
                                    i++;
                                }
                            }
                        }
                    }
                    get_rates(context);
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void sort() {
        Collections.sort(posts_list, new Comparator<String[]>(){
            public int compare(String[] obj1, String[] obj2) {
                if (Double.valueOf(obj1[6]) == Double.valueOf(obj2[6])) {
                    return 0;
                } else if (Double.valueOf(obj1[6]) > Double.valueOf(obj2[6])) {
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

    void getUsers() {
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