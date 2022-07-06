package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post extends AppCompatActivity {
    String id, id_post, email, url, tip, id_rate, id_comment, id_login, login_type, login_name;
    TextView t, t_description, time;
    EditText comment;
    Button b;
    FirebaseFirestore db;
    FirebaseAuth auth;
    boolean pom = false, pom1 = false;
    RatingBar r;
    RecyclerView rv;
    boolean already_rated = false, on_open = false, already_commented = false;
    List<String[]> comments;
    AdapterComments ac;
    LinearLayout root;
    float avg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        LinearLayout c = findViewById(R.id.root1);
        t = findViewById(R.id.textView);
        t_description = findViewById(R.id.textView5);
        time = findViewById(R.id.textView10);
        r = findViewById(R.id.ratingBar);
        comment = findViewById(R.id.editTextTextPersonName3);
        b = findViewById(R.id.button7);
        rv = findViewById(R.id.rv8);
        root = findViewById(R.id.view);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        id_post = getIntent().getStringExtra("ID");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        get_rate();
        get_comments();

        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Post.this, UserProfile.class).putExtra("id", id));
            }
        });

        if (db.collection("Posts").document(id_post) == null) {
            finish();
            return;
        }

        if (auth.getCurrentUser() == null) {
            comment.setVisibility(View.GONE);
            b.setVisibility(View.GONE);
        }

        db.collection("Posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getId().equals(id_post)) {
                            Map<String, Object> image = new HashMap<>();
                            long views = doc.getLong("Views");
                            image.put("Views", views + 1);
                            db.collection("Posts").document(id_post).update(image);
                            t_description.setText(doc.getString("Description"));
                            time.setText(doc.getString("Date"));
                            url = doc.getString("Image URL");
                            if (doc.getString("Type").equals("image")) {
                                ImageView i = new ImageView(Post.this);
                                i.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                                Picasso.with(Post.this).load(doc.getString("Image URL")).into(i);
                                c.addView(i);
                            } else {
                                VideoView k = new VideoView(Post.this);
                                k.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                                k.setVideoURI(Uri.parse(doc.getString("Image URL")));
                                c.addView(k);
                                k.start();
                                MediaController m = new MediaController(Post.this);
                                m.show();
                                k.setMediaController(m);
                            }
                            id = doc.getString("User-ID");
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            get_user();
                            get_login();
                        }
                    }
                } else {
                    Toast.makeText(Post.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (auth.getCurrentUser() == null) r.setEnabled(false);

        r.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (on_open) {
                    on_open = false;
                    return;
                }
                if (auth.getCurrentUser() == null) {
                    r.setRating(avg);
                    Toast.makeText(Post.this, "Sign in", Toast.LENGTH_LONG).show();
                    return;
                }
                Map<String, Object> rating1 = new HashMap<>();
                rating1.put("User-ID", auth.getCurrentUser().getUid());
                rating1.put("Post-ID", id_post);
                rating1.put("Rate", rating);
                if (!already_rated) {
                    db.collection("Rating").add(rating1).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                id_rate = task.getResult().getId();
                                Toast.makeText(Post.this, "Rate added", Toast.LENGTH_LONG).show();
                                already_rated = true;
                            } else {
                                Toast.makeText(Post.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    db.collection("Rating").document(id_rate).update(rating1).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Post.this, "Rate updated", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Post.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comment.getText().toString().equals("")) return;
                Map<String, Object> c = new HashMap<>();
                c.put("User-ID", auth.getCurrentUser().getUid());
                c.put("Post-ID", id_post);
                c.put("Comment", comment.getText().toString());
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss");
                c.put("Date", format.format(date));

                db.collection("Comments").add(c).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            String[] pom = new String[5];
                            pom[0] = comment.getText().toString();
                            pom[1] = auth.getCurrentUser().getUid();
                            Date date = new Date();
                            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss");
                            pom[2] = format.format(date);
                            pom[3] = id_login;
                            pom[4] = login_type;
                            ac.addItem(pom);
                            rv.setAdapter(ac);
                            Toast.makeText(Post.this, "Comment added", Toast.LENGTH_LONG).show();
                            already_commented = true;

                        } else {
                            Toast.makeText(Post.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    void get_user() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("Users");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("ID").equals(id)) {
                            t.setText(doc.getString("Full name"));
                            email = doc.getString("E-mail");
                            tip = doc.getString("Type");
                            break;
                        }
                    }
                } else {
                    Toast.makeText(Post.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void setListener() {
        if (auth.getCurrentUser() != null) {
            if (id.equals(auth.getCurrentUser().getUid()) || login_type.equals("1")) {
                pom = true;
                invalidateOptionsMenu();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!pom) return false;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.edit:
                startActivity(new Intent(Post.this, EditPost.class).putExtra("Post-ID", id_post));
                return true;
            case R.id.delete:
                pom1 = true;
                delete();
                finish();
                startActivity(new Intent(Post.this, UserProfile.class)
                        .putExtra("id", id)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void delete() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReferenceFromUrl(url).delete();
        db.collection("Posts").document(getIntent().getStringExtra("ID")).delete();

        db.collection("Rating").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    if (queryDocumentSnapshots.size() == 0) return;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("Post-ID").equals(id_post)) {
                            db.collection("Rating").document(doc.getId()).delete();
                        }
                    }
                }
            }
        });

        db.collection("Comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    if (queryDocumentSnapshots.size() == 0) return;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("Post-ID").equals(id_post)) {
                            db.collection("Comments").document(doc.getId()).delete();
                        }
                    }
                }
            }
        });
    }

    void get_rate() {
        db.collection("Rating").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                double uk = 0;
                int br_rates = 0;
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    if (queryDocumentSnapshots.size() == 0) {
                        avg = 0;
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("Post-ID").equals(id_post)) {
                            if (auth.getCurrentUser() != null) {
                                if (doc.getString("User-ID").equals(auth.getCurrentUser().getUid())) {
                                    already_rated = true;
                                    on_open = true;
                                    uk = doc.getDouble("Rate");
                                    r.setRating((float) uk);
                                    id_rate = doc.getId();
                                    return;
                                }
                            }
                            uk += doc.getDouble("Rate");
                            br_rates++;
                        }
                    }
                    if (br_rates > 0) {
                        on_open = true;
                        avg = (float) uk / br_rates;
                        r.setRating(avg);
                    }
                }
            }
        });
    }

    void get_comments() {
        db.collection("Comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int br_comments = 0;
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    if (queryDocumentSnapshots.size() == 0) {
                        comments = new ArrayList<>(0);
                        ac = new AdapterComments(Post.this, root, comments);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("Post-ID").equals(id_post)) br_comments++;
                    }

                    if (br_comments == 0) {
                        comments = new ArrayList<>(0);
                        ac = new AdapterComments(Post.this, root, comments);
                        return;
                    }
                    comments = new ArrayList<>(br_comments);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("Post-ID").equals(id_post)) {
                            String[] pom = new String[5];
                            pom[0] = doc.getString("Comment");
                            pom[1] = doc.getString("User-ID");
                            pom[2] = doc.getString("Date");
                            comments.add(pom);
                        }
                    }
                    getUsers();
                }
            }
        });
    }

    void get_login() {
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (auth.getCurrentUser() != null) {
                            if (doc.getString("ID").equals(auth.getCurrentUser().getUid())) {
                                id_login = doc.getString("Full name");
                                login_type = doc.getString("Type");
                                if (login_type.equals("1")) {
                                    comment.setVisibility(View.GONE);
                                    b.setVisibility(View.GONE);
                                    r.setEnabled(false);
                                }
                                setListener();
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(Post.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void getUsers() {
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        for (int k = 0; k < comments.size(); k++) {
                            if (doc.getString("ID").equals(comments.get(k)[1])) {
                                comments.get(k)[3] = doc.getString("Full name");
                                comments.get(k)[4] = doc.getString("Type");
                            }
                        }
                    }

                    if (comments.size() > 1) {
                        sort(Post.this);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ac = new AdapterComments(Post.this, root, comments);
                                rv.setAdapter(ac);
                            }
                        });
                    }
                } else {
                    Toast.makeText(Post.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void sort(Context context) {
        Collections.sort(comments, new Comparator<String[]>(){
            public int compare(String[] obj1, String[] obj2) {
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss");
                Date d1 = null, d2 = null;
                try {
                    d1 = format.parse(obj1[2]);
                    d2 = format.parse(obj2[2]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d2.compareTo(d1);
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ac = new AdapterComments(Post.this, root, comments);
                rv.setAdapter(ac);
            }
        });
    }
}