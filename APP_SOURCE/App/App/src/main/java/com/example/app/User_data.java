package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class User_data extends AppCompatActivity {
    EditText e, p, n, opis;
    Button b;
    FirebaseAuth auth;
    FirebaseUser user;
    int a = 0, c = 0;
    String name, name_old, email, email_old, url, url_old, desc, desc_old;
    ProgressBar pr;
    ImageView v;
    boolean flag = false;
    public static final int GALLERY_ACTIVITY_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        b = findViewById(R.id.button3);
        e = findViewById(R.id.editTextTextEmailAddress3);
        p = findViewById(R.id.editTextTextPassword3);
        n = findViewById(R.id.editTextTextPersonName2);
        pr = findViewById(R.id.progressBar4);
        v = findViewById(R.id.imageView5);
        opis = findViewById(R.id.editTextTextMultiLine3);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Intent intent = getIntent();
        email = intent.getStringExtra("E-mail");
        email_old = email;
        name = intent.getStringExtra("Ime");
        name_old = name;
        url = intent.getStringExtra("Image-URL");
        url_old = url;
        e.setText(email);
        n.setText(name);
        desc = intent.getStringExtra("Description");
        desc_old = intent.getStringExtra("Description");
        opis = findViewById(R.id.editTextTextMultiLine3);
        if (intent.getStringExtra("Tip").equals("3")) {
            opis.setVisibility(View.VISIBLE);
            opis.setText(desc);
        }
        Glide.with(User_data.this).load(Uri.parse(url)).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Toast.makeText(User_data.this, "Avatar download failed", Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Photo/Video"), GALLERY_ACTIVITY_REQUEST_CODE);
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pr.getVisibility() == View.VISIBLE) return;
                pr.setVisibility(View.VISIBLE);
                email = e.getText().toString();
                name = n.getText().toString();
                if (opis.getVisibility() == View.VISIBLE) desc = opis.getText().toString();
                if (!email.equals(email_old)) a++;
                if (!name.equals(name_old)) a++;
                if (!p.getText().toString().equals("")) a++;
                if (!url.equals(url_old)) a++;
                if (!desc.equals(desc_old)) a++;
                if  (!email.equals(email_old) || !name.equals(name_old) ||
                        !p.getText().toString().equals("") || !url.equals(url_old) || !desc.equals(desc_old)) {
                    pr.setVisibility(View.VISIBLE);
                    if (!url.equals(url_old)) {
                        if (!check_size()) {
                            p.setVisibility(View.INVISIBLE);
                            return;
                        }
                    }
                    if (!p.getText().toString().equals("")) {
                        user.updatePassword(p.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    c++;
                                    poruka();
                                } else {
                                    Toast.makeText(User_data.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        });
                    }
                    if (!email.equals(email_old)) {
                        user.updateEmail(e.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    c++;
                                    poruka();
                                    user.sendEmailVerification();
                                } else {
                                    Toast.makeText(User_data.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        });
                    }
                    if (!url.equals(url_old)) {
                        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
                        StringBuilder sb = new StringBuilder(10);
                        for (int i = 0; i < 10; i++) {
                            int index = (int)(AlphaNumericString.length() * Math.random());
                            sb.append(AlphaNumericString.charAt(index));
                        }
                        flag = true;
                        upload_avatar(sb);
                    }
                    if (!name.equals(name_old) || !email.equals(email_old) || !url.equals(url_old) || !desc.equals(desc_old)) {
                        if (!flag) update_user_in_database();
                    }
                }
            }
        });
    }

    void poruka() {
        if (a == c) {
            Toast.makeText(User_data.this, "Saved", Toast.LENGTH_SHORT).show();
            pr.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        super.onActivityResult(requestCode, resultCode, data);
        url = data.getData().toString();
        Picasso.with(User_data.this).load(data.getData()).into(v);
    }

    boolean check_size() {
        int dataSize=0;
        try {
            InputStream file = getApplicationContext().getContentResolver().openInputStream(Uri.parse(url));
            dataSize = file.available();
            dataSize /= 1024;
            dataSize /= 1024;
            file.close();
            String mimeType = getContentResolver().getType(Uri.parse(url));
            if ((mimeType.startsWith("image") && dataSize > 9) /*|| (mimeType.startsWith("video") && dataSize > 10)*/) {
                Toast.makeText(User_data.this, "Picture is too big", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    void upload_avatar(StringBuilder sb) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference mountainsRef = storageRef.child(sb.toString());
        StorageReference mountainImagesRef = storageRef.child(email);
        mountainsRef.getName().equals(mountainImagesRef.getName());
        mountainsRef.getPath().equals(mountainImagesRef.getPath());
        UploadTask uploadTask = mountainsRef.putFile(Uri.parse(url));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(User_data.this, exception.toString(), Toast.LENGTH_LONG).show();
                p.setVisibility(View.INVISIBLE);
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    task.getResult().getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                c++;
                                poruka();
                                url = task.getResult().toString();
                                if (!name.equals(n.getText().toString()) || !email.equals(email_old) || url != url_old) {
                                    update_user_in_database();
                                }
                            } else {
                                Toast.makeText(User_data.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                p.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else {
                    Toast.makeText(User_data.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                    p.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    void update_user_in_database() {
        Intent intent = getIntent();
        String ID = intent.getStringExtra("ID");
        String tip = intent.getStringExtra("Tip");
        user = auth.getCurrentUser();
        name_old = n.getText().toString();
        email_old = e.getText().toString();
        url_old = url;

        Map<String, Object> u = new HashMap<>();
        u.put("Full name", name);
        u.put("E-mail", email);
        u.put("ID", user.getUid());
        u.put("Type", tip);
        u.put("Image URL", url);
        if (opis.getVisibility() == View.VISIBLE) u.put("Description", opis.getText().toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("Users");
        colRef.document(ID).update(u).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(User_data.this, "Saved", Toast.LENGTH_SHORT).show();
                    pr.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(User_data.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    p.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}