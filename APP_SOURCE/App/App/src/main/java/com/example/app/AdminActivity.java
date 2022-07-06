package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    FragmentManager fManager;
    Fragment f;
    TabLayout tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        tab = findViewById(R.id.tab_view1);
        fManager = getSupportFragmentManager();
        f = new UncheckedFragment(AdminActivity.this);
        fManager.beginTransaction().replace(R.id.container1, f).commit();
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        f = new UncheckedFragment(AdminActivity.this);
                        fManager.beginTransaction().replace(R.id.container1, f).commit();
                        return;
                    case 1:
                        f = new UsersFragment(AdminActivity.this);
                        fManager.beginTransaction().replace(R.id.container1, f).commit();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.user_data:
                update_data();
                return true;
            case R.id.add_admin:
                Intent i = new Intent(AdminActivity.this, Register.class);
                i.putExtra("Tip", "1");
                startActivity(i);
                return true;
            case R.id.sign_out:
                auth.signOut();
                finish();
                startActivity(new Intent(AdminActivity.this, Check.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void update_data() {
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("ID").equals(user.getUid())) {
                            Intent i = new Intent(AdminActivity.this, User_data.class);
                            i.putExtra("Ime", doc.getString("Full name"));
                            i.putExtra("E-mail", doc.getString("E-mail"));
                            i.putExtra("ID", doc.getId());
                            i.putExtra("Tip", doc.getString("Type"));
                            i.putExtra("Image-URL", doc.getString("Image URL"));
                            i.putExtra("Description", "qlo");
                            startActivity(i);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(AdminActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}