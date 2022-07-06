package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditPost extends AppCompatActivity {
    FirebaseFirestore db;
    String post_id, tip_objave, desc_old, loc_old;
    EditText loc, desc;
    Button b;
    LinearLayout c;
    boolean pom = false;
    float[] latlong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_posting);
        c = findViewById(R.id.root1);
        loc = findViewById(R.id.editTextTextPersonName4);
        desc = findViewById(R.id.editTextTextMultiLine);
        b = findViewById(R.id.button4);

        post_id = getIntent().getStringExtra("Post-ID");
        db.collection("Posts").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    tip_objave = task.getResult().getString("Type");
                    desc_old = task.getResult().getString("Description");
                    desc.setText(desc_old);
                    GeoPoint g = task.getResult().getGeoPoint("GeoTag");
                    latlong = new float[2];
                    latlong[0] = Float.valueOf(String.valueOf(g.getLatitude()));
                    latlong[1] = Float.valueOf(String.valueOf(g.getLongitude()));
                    loc_old = getLocationName(g.getLatitude(), g.getLongitude());
                    loc.setText(loc_old);
                    loc.setVisibility(View.VISIBLE);
                    show(Uri.parse(task.getResult().getString("Image URL")));
                }
            }
        });

        b.setText("Uredi");

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loc.getText().toString().equals("")) {
                    Toast.makeText(EditPost.this, "Location field is required.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!loc_old.equals(loc.getText().toString())) {
                    latlong = getGeoTag(loc.getText().toString());
                    if (latlong[0] == 0 && latlong[1] == 0) {
                        Toast.makeText(EditPost.this, "Location not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (!desc_old.equals(desc.getText().toString()) || !loc_old.equals(loc.getText().toString())) {
                    update_post_in_database();
                }
            }
        });
    }

    void show(Uri uri) {
        if (tip_objave.equals("image")) {
            ImageView k = new ImageView(EditPost.this);
            k.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            Picasso.with(EditPost.this).load(uri.toString()).into(k);
            c.addView(k);
        } else {
            VideoView k = new VideoView(EditPost.this);
            k.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            k.setVideoURI(uri);
            c.addView(k);
            k.start();
            MediaController m = new MediaController(EditPost.this);
            m.show();
            k.setMediaController(m);
        }
    }

    float[] getGeoTag(String location) {
        Locale l = new Locale("eng");
        Geocoder geo = new Geocoder(this, l);
        List<Address> loc = null;
        float[] geoTag = new float[2];
        try {
            if (!pom) {
                pom = true;
                return getGeoTag(location);
            }
            loc = geo.getFromLocationName(location, 1);
            if (loc.size() == 0) return getGeoTag(location);
            Address a = loc.get(0);
            geoTag[0] = Float.valueOf(String.valueOf(a.getLatitude()));
            geoTag[1] = Float.valueOf(String.valueOf(a.getLongitude()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return geoTag;
    }

    String getLocationName(double lat, double longi) {
        Locale l = new Locale("eng");
        Geocoder geo = new Geocoder(this, l);
        List<Address> loc = null;
        float[] geoTag = new float[2];
        Address a = null;
        try {
            if (!pom) {
                pom = true;
                return getLocationName(lat, longi);
            }
            loc = geo.getFromLocation(lat, longi, 1);
            if (loc.size() == 0) return getLocationName(lat, longi);
            a = loc.get(0);
            System.out.println(a.getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return a.getAddressLine(0);
    }

    void update_post_in_database() {
        Intent intent = getIntent();
        Map<String, Object> post = new HashMap<>();
        post.put("Description", desc.getText().toString());
        GeoPoint g = new GeoPoint(latlong[0], latlong[1]);
        post.put("GeoTag", g);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("Posts");
        colRef.document(post_id).update(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditPost.this, "Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditPost.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}