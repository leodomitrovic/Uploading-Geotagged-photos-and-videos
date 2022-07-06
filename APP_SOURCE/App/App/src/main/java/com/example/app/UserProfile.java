package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfile extends AppCompatActivity {
    ConstraintLayout root;
    ImageView icon;
    TextView name, opis, followers, posts;
    Button b;
    List<String[]> posts_list;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    String id, id_login;
    int br = 0;
    TabLayout tab;
    FragmentManager fManager;
    Fragment f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        b = findViewById(R.id.button6);
        b.setVisibility(View.INVISIBLE);
        id = getIntent().getStringExtra("id");

        if (user != null) {
            if (id.equals(user.getUid())) {
                b.setVisibility(View.VISIBLE);
                b.setText("Edit");
            } else {
                check_type();
            }
        }

        db = FirebaseFirestore.getInstance();
        root = findViewById(R.id.root3);
        icon = findViewById(R.id.imageView3);
        name = findViewById(R.id.textView8);
        opis = findViewById(R.id.textView9);
        followers = findViewById(R.id.textView12);
        posts = findViewById(R.id.textView17);
        tab = findViewById(R.id.tab_view5);

        getPosts(this);
        getUserData(this);
        getNumberOfFollowers(this);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (b.getText().equals("Follow")) {
                    Map<String, Object> follow = new HashMap<>();
                    follow.put("User-ID", user.getUid());
                    follow.put("Followed User-ID", id);
                    db.collection("Follow").add(follow).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                b.setText("Following");
                                b.setBackgroundColor(getResources().getColor(R.color.teal_200));
                                getNumberOfFollowers(UserProfile.this);
                            } else {
                                Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if (b.getText().equals("Following")) {
                    db.collection("Follow").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                String doc_id = null;
                                QuerySnapshot queryDocumentSnapshots = task.getResult();
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    if (doc.getString("User-ID").equals(user.getUid()) && doc.getString("Followed User-ID").equals(id)) {
                                        doc_id = doc.getId();
                                        break;
                                    }
                                }
                                db.collection("Follow").document(doc_id).delete();
                                b.setText("Follow");
                                b.setBackgroundColor(getResources().getColor(R.color.purple_500));
                                getNumberOfFollowers(UserProfile.this);
                            } else {
                                Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    update_data();
                }
            }
        });
    }

    void init_button() {
        if (b.getVisibility() == View.VISIBLE && !b.getText().equals("Uredi")) {
            db.collection("Follow").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (doc.getString("User-ID").equals(user.getUid()) && doc.getString("Followed User-ID").equals(id)) {
                                b.setText("Following");
                                b.setBackgroundColor(getResources().getColor(R.color.teal_200));
                                break;
                            }
                        }
                    } else {
                        Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    void getPosts(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("Posts");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getBoolean("Approved") && doc.getString("User-ID").equals(id)) i++;
                    }
                    posts.setText(Integer.toString(i));
                    posts_list = new ArrayList<>(i);
                    i = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getBoolean("Approved") && doc.getString("User-ID").equals(id)) {
                            String[] pom = new String[5];
                            pom[0] = doc.getString("Image URL");
                            pom[1] = doc.getId();
                            pom[2] = doc.getString("Date");
                            pom[3] = String.valueOf(doc.getGeoPoint("GeoTag").getLatitude());
                            pom[4] = String.valueOf(doc.getGeoPoint("GeoTag").getLongitude());
                            posts_list.add(pom);
                            i++;
                        }
                    }

                    sort(context);
                } else {
                    Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void sort(Context context) {
        Collections.sort(posts_list, new Comparator<String[]>(){
            public int compare(String[] obj1, String[] obj2) {
                return obj2[2].compareToIgnoreCase(obj1[2]);
            }
        });
        tab_init();
    }

    void tab_init() {
        fManager = getSupportFragmentManager();
        f = new UserPostsFragment(UserProfile.this, posts_list, root);
        fManager.beginTransaction().replace(R.id.root3, f).commit();
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        f = new UserPostsFragment(UserProfile.this, posts_list, root);
                        fManager.beginTransaction().replace(R.id.root3, f).commit();
                        return;
                    case 1:
                        f = new UserMapFragment(UserProfile.this, posts_list);
                        fManager.beginTransaction().replace(R.id.root3, f).commit();
                        return;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    void getUserData(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("Users");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("ID").equals(id)) {
                            Picasso.with(context).load(Uri.parse(doc.getString("Image URL"))).into(icon);
                            name.setText(doc.getString("Full name"));
                            opis.setText(doc.getString("Description"));
                            opis.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void check_type() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("Users");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("ID").equals(user.getUid())) {
                            id_login = doc.getString("Type");
                            if (!id_login.equals("1")) b.setVisibility(View.VISIBLE);
                            init_button();
                            break;
                        }
                    }
                } else {
                    Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void getNumberOfFollowers(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference colRef = db.collection("Follow");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    br = 0;
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("Followed User-ID").equals(id)) br++;
                    }
                    followers.setText(Integer.toString(br));
                } else {
                    Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void update_data() {
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference colRef = db.collection("Users");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("ID").equals(user.getUid())) {
                            String name = doc.getString("Full name");
                            String email = doc.getString("E-mail");
                            String ID = doc.getId();
                            String url = doc.getString("Image URL");
                            String desc = doc.getString("Description");
                            String tip = doc.getString("Type");
                            Intent i = new Intent(UserProfile.this, User_data.class)
                                    .putExtra("act", "profile");
                            i.putExtra("Ime", name);
                            i.putExtra("E-mail", email);
                            i.putExtra("ID", ID);
                            i.putExtra("Tip", tip);
                            i.putExtra("Image-URL", url);
                            i.putExtra("Description", desc);
                            startActivity(i);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(UserProfile.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}