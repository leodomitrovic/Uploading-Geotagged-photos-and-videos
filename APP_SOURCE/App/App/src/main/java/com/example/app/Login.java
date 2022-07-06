package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    EditText e, p;
    Button b;
    FirebaseAuth auth;
    ProgressBar pr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        e = findViewById(R.id.editTextTextEmailAddress2);
        p = findViewById(R.id.editTextTextPassword2);
        b = findViewById(R.id.button2);
        pr = findViewById(R.id.progressBar2);
        pr.setVisibility(View.INVISIBLE);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pr.getVisibility() == View.VISIBLE) return;
                auth = FirebaseAuth.getInstance();
                String email = e.getText().toString().trim();
                String pass = p.getText().toString().trim();
                if (!email.equals("") && !pass.equals("")) {
                    pr.setVisibility(View.VISIBLE);
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    finish();
                                    startActivity(new Intent(Login.this, Check.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                } else {
                                    Toast.makeText(Login.this, "E-mail address not verified", Toast.LENGTH_LONG).show();
                                    pr.setVisibility(View.INVISIBLE);
                                    auth.signOut();
                                }
                            } else {
                                Toast.makeText(Login.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                pr.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });

        TextView t = findViewById(R.id.textView2);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Register.class);
                i.putExtra("Tip", "2");
                startActivity(i);
            }
        });
    }
}