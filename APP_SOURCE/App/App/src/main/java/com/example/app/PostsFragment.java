package com.example.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PostsFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    RecyclerView rv;
    ConstraintLayout root;
    AdapterPostFollow ap;
    List<String[]> posts_list;
    List<String[]> following;
    Activity activity;
    FloatingActionButton b;
    String tip, mimeType;
    public static final int GALLERY_ACTIVITY_REQUEST_CODE = 2;

    public PostsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public PostsFragment (Activity activity, String tip) {
        this.activity = activity;
        this.tip = tip;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_posts, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        getFollowing(activity.getApplicationContext());
        rv = view.findViewById(R.id.rv7);
        root = view.findViewById(R.id.root7);
        rv.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        b = view.findViewById(R.id.floatingActionButton);
        if (auth.getCurrentUser() == null) b.setVisibility(View.INVISIBLE);
        else if (!tip.equals("3")) b.setVisibility(View.INVISIBLE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("*/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Photo/Video"), GALLERY_ACTIVITY_REQUEST_CODE);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_ACTIVITY_REQUEST_CODE) {
            if (resultCode != activity.RESULT_OK) return;
            if (!check(data.getData())) return;
            String mimeType = activity.getContentResolver().getType(data.getData());
            String tip_objave;
            if (mimeType.startsWith("image")) {
                tip_objave = "image";
            } else {
                tip_objave = "video";
            }
            Intent i = new Intent(activity.getApplicationContext(), Posting.class)
                    .putExtra("uri", data.getData().toString())
                    .putExtra("tip_objave", tip_objave);
            startActivity(i);
        }
    }

    void getPosts(Context context) {
        db.collection("Posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        for (int k = 0; k < following.size(); k++) {
                            if (doc.getString("User-ID").equals(following.get(k)[0])) {
                                i++;
                                break;
                            }
                        }
                    }
                    posts_list = new ArrayList<>(i);
                    i = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        for (int k = 0; k < following.size(); k++) {
                            if (doc.getString("User-ID").equals(following.get(k)[0])) {
                                String[] pom = new String[7];
                                pom[0] = doc.getString("Image URL");
                                pom[1] = doc.getString("Description");
                                pom[2] = doc.getId();
                                pom[3] = doc.getString("Type");
                                pom[4] = following.get(k)[0];
                                pom[5] = following.get(k)[1];
                                pom[6] = doc.getString("Date");
                                posts_list.add(pom);
                                i++;
                                break;
                            }
                        }
                    }
                    sort(context);
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getFollowing(Context context) {
        db.collection("Follow").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    int br = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("User-ID").equals(user.getUid())) br++;
                    }

                    following = new ArrayList<>(br);
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("User-ID").equals(user.getUid())) {
                            String[] pom = new String[2];
                            pom[0] = doc.getString("Followed User-ID");
                            following.add(pom);
                        }
                    }
                    getNames(context);
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void getNames(Context context) {
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        for (int k = 0; k < following.size(); k++) {
                            if (doc.getString("ID").equals(following.get(k)[0])) {
                                following.get(k)[1] = doc.getString("Full name");
                            }
                        }
                    }
                    getPosts(context);
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void sort(Context context) {
        Collections.sort(posts_list, new Comparator<String[]>(){
            public int compare(String[] obj1, String[] obj2) {
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss");
                Date d1 = null, d2 = null;
                try {
                    d1 = format.parse(obj1[6]);
                    d2 = format.parse(obj2[6]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return d2.compareTo(d1);
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (posts_list.size() > 0) {
                    ap = new AdapterPostFollow(activity, root, posts_list);
                    rv.setAdapter(ap);
                }
            }
        });
    }

    boolean check(Uri uri) {
        mimeType = activity.getContentResolver().getType(uri);
        try {
            if ((mimeType.startsWith("image"))) {
                if (check_size(uri) > 9) {
                    Toast.makeText(activity.getApplicationContext(), "Picture is too big", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (mimeType.startsWith("video")) {
            if (check_duration(uri) > 60000) {
                Toast.makeText(activity.getApplicationContext(), "Video is too long", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    float check_size(Uri uri) throws IOException {
        int dataSize=0;
        InputStream file = activity.getApplicationContext().getContentResolver().openInputStream(uri);
        dataSize = file.available();
        dataSize /= 1024;
        dataSize /= 1024;
        file.close();
        return dataSize;
    }

    long check_duration(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(activity.getApplicationContext(), uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();
        return timeInMillisec;
    }
}