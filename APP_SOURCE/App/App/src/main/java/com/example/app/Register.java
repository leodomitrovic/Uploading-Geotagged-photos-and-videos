package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final int GALLERY_ACTIVITY_REQUEST_CODE = 2;
    EditText e, p, n;
    TextView opis;
    Button b;
    FirebaseAuth auth;
    ProgressBar progress;
    ImageView v;
    String link = "https://upload.wikimedia.org/wikipedia/commons/7/7c/Profile_avatar_placeholder_large.png";
    String email, pass, name, link1 = "default", tip = "2";
    Spinner s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        e = findViewById(R.id.editTextTextEmailAddress);
        p = findViewById(R.id.editTextTextPassword);
        b = findViewById(R.id.button);
        n = findViewById(R.id.editTextTextPersonName);
        progress = findViewById(R.id.progressBar3);
        opis = findViewById(R.id.editTextTextMultiLine2);
        s = findViewById(R.id.spinner);

        if (getIntent().getStringExtra("Tip").equals("1")) s.setVisibility(View.GONE);
        progress.setVisibility(View.INVISIBLE);
        auth = FirebaseAuth.getInstance();
        v = findViewById(R.id.imageView);
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
                if (progress.getVisibility() == View.VISIBLE) return;
                email = e.getText().toString().trim();
                pass = p.getText().toString().trim();
                name = n.getText().toString().trim();
                if (!email.equals("") && !pass.equals("") && !name.equals("")) {
                    progress.setVisibility(View.VISIBLE);
                    if (!check_size()) {
                        progress.setVisibility(View.INVISIBLE);
                        return;
                    }
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
                                StringBuilder sb = new StringBuilder(10);
                                for (int i = 0; i < 10; i++) {
                                    int index = (int)(AlphaNumericString.length() * Math.random());
                                    sb.append(AlphaNumericString.charAt(index));
                                }
                                if (link1.equals("default")) {
                                    save_user_to_database(link);
                                    return;
                                }
                                upload_avatar(sb);
                            } else {
                                Toast.makeText(Register.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                progress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else {
                    Toast.makeText(Register.this, "All fields are required", Toast.LENGTH_LONG).show();
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.type_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        super.onActivityResult(requestCode, resultCode, data);
        link = data.getData().toString();
        link1 = "new";
        Picasso.with(Register.this).load(data.getData()).into(v);
    }

    void upload_avatar(StringBuilder sb) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference mountainsRef = storageRef.child(sb.toString());
        StorageReference mountainImagesRef = storageRef.child(email);
        mountainsRef.getName().equals(mountainImagesRef.getName());
        mountainsRef.getPath().equals(mountainImagesRef.getPath());
        UploadTask uploadTask = mountainsRef.putFile(Uri.parse(link));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Register.this, exception.toString(), Toast.LENGTH_LONG).show();
                auth.getCurrentUser().delete();
                progress.setVisibility(View.INVISIBLE);
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
                                save_user_to_database(task.getResult().toString());
                            } else {
                                Toast.makeText(Register.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                progress.setVisibility(View.INVISIBLE);
                                auth.getCurrentUser().delete();
                            }
                        }
                    });
                } else {
                    Toast.makeText(Register.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                    progress.setVisibility(View.INVISIBLE);
                    auth.getCurrentUser().delete();
                }
            }
        });
    }

    void save_user_to_database(String url) {
        if (getIntent().getStringExtra("Tip").equals("1")) tip = "1";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("ID", auth.getCurrentUser().getUid());
        user.put("E-mail", email);
        user.put("Full name", name);
        user.put("Type", tip);
        user.put("Image URL", url);
        if (opis.getVisibility() == View.VISIBLE) user.put("Description", opis.getText().toString());
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss");
        user.put("Date", format.format(date));
        db.collection("Users").add(user).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                progress.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    Toast.makeText(Register.this, "Added successfully", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(Register.this, Check.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    auth.getCurrentUser().sendEmailVerification();
                } else {
                    Toast.makeText(Register.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    boolean check_size() {
        int dataSize=0;
        try {
            InputStream file = getApplicationContext().getContentResolver().openInputStream(Uri.parse(link));
            dataSize = file.available();
            dataSize /= 1024;
            dataSize /= 1024;
            file.close();
            String mimeType = getContentResolver().getType(Uri.parse(link));
            if ((mimeType.startsWith("image") && dataSize > 9) /*|| (mimeType.startsWith("video") && dataSize > 10)*/) {
                Toast.makeText(Register.this, "Avatar is too big", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            tip = "2";
            opis.setVisibility(View.GONE);
        } else {
            tip = "3";
            opis.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}