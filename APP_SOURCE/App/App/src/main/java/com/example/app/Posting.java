package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Posting extends AppCompatActivity {
    String tip_objave, description;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    EditText loc, desc;
    LinearLayout c;
    Button b;
    ProgressBar p;
    float[] latlong;
    boolean pom = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        c = findViewById(R.id.root1);
        b = findViewById(R.id.button4);
        loc = findViewById(R.id.editTextTextPersonName4);
        desc = findViewById(R.id.editTextTextMultiLine);
        latlong = new float[2];
        p = findViewById(R.id.progressBar6);
        p.setVisibility(View.INVISIBLE);
        Intent i = getIntent();
        Uri uri = Uri.parse(i.getStringExtra("uri"));
        tip_objave = i.getStringExtra("tip_objave");
        

        InputStream in = null;
        ExifInterface exif = null;
        try {
            in = getContentResolver().openInputStream(uri);
            exif = new ExifInterface(in);
            if(!exif.getLatLong(latlong)){
                loc.setVisibility(View.VISIBLE);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        show(uri);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p.getVisibility() == View.VISIBLE) return;
                if (loc.getVisibility() == View.VISIBLE) {
                    if (loc.getText().toString().equals("")) {
                        Toast.makeText(Posting.this, "Location field is required.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    latlong = getGeoTag(loc.getText().toString());
                    if (latlong[0] == 0 && latlong[1] == 0) {
                        Toast.makeText(Posting.this, "Location not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                p.setVisibility(View.VISIBLE);
                description = desc.getText().toString();
                upload(uri);
            }
        });
    }

    void upload(Uri uri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(10);
        for (int j = 0; j < 10; j++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }

        StorageReference mountainsRef = storageRef.child(sb.toString());
        StorageReference mountainImagesRef = storageRef.child(user.getEmail());
        mountainsRef.getName().equals(mountainImagesRef.getName());
        mountainsRef.getPath().equals(mountainImagesRef.getPath());
        UploadTask uploadTask = mountainsRef.putFile(uri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Posting.this, exception.toString(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            try {
                                InputStream in;
                                in = getContentResolver().openInputStream(uri);
                                ExifInterface exif = new ExifInterface(in);
                                Map<String, Object> image = new HashMap<>();
                                image.put("User-ID", auth.getCurrentUser().getUid());
                                image.put("Image URL", task.getResult().toString());
                                GeoPoint g = new GeoPoint(latlong[0], latlong[1]);
                                image.put("GeoTag", g);
                                Date date = new Date();
                                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss");
                                image.put("Date", format.format(date));
                                image.put("Approved", false);
                                image.put("Type", tip_objave);
                                image.put("Description", description);
                                image.put("Views", 0);
                                db.collection("Posts").add(image).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Posting.this, "Added successfully", Toast.LENGTH_LONG).show();
                                            p.setVisibility(View.INVISIBLE);
                                            finish();
                                            startActivity(new Intent(Posting.this, Check.class));
                                        } else {
                                            Toast.makeText(Posting.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(Posting.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
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

    void show(Uri uri) {
        if (tip_objave.equals("image")) {
            ImageView k = new ImageView(Posting.this);
            k.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            Picasso.with(Posting.this).load(uri.toString()).into(k);
            c.addView(k);
        } else {
            VideoView k = new VideoView(Posting.this);
            k.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            k.setVideoURI(uri);
            c.addView(k);
            k.start();
            MediaController m = new MediaController(Posting.this);
            m.show();
            k.setMediaController(m);
        }
    }
}