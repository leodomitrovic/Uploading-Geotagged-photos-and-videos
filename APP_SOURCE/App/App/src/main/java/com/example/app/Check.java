package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Check extends AppCompatActivity {
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(Check.this, MainActivity.class));
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference colRef1 = db.collection("Users");
        colRef1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String ID = doc.getString("ID");
                        if (doc.getString("ID").equals(auth.getCurrentUser().getUid())) {
                            String tip = doc.getString("Type");
                            finish();
                            if (tip.equals("1")) {
                                startActivity(new Intent(Check.this, AdminActivity.class).putExtra("tip", tip)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                startActivity(new Intent(Check.this, MainActivity.class).putExtra("tip", tip).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        }
                    }
                } else {
                    Toast.makeText(Check.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }
}