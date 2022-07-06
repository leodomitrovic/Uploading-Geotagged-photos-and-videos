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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore db;
    FragmentManager fManager;
    Fragment f;
    TabLayout tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        tab = findViewById(R.id.tab_view);
        fManager = getSupportFragmentManager();
        if (auth.getCurrentUser() == null) {
            ((ViewGroup) tab.getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
            tab.getTabAt(1).select();
            f = new MapsFragment(MainActivity.this);
            fManager.beginTransaction().replace(R.id.container, f).commit();
        } else {
            f = new PostsFragment(MainActivity.this, getIntent().getStringExtra("tip"));
            fManager.beginTransaction().replace(R.id.container, f).commit();
        }
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        f = new PostsFragment(MainActivity.this, getIntent().getStringExtra("tip"));
                        fManager.beginTransaction().replace(R.id.container, f).commit();
                        return;
                    case 1:
                        f = new MapsFragment(MainActivity.this);
                        fManager.beginTransaction().replace(R.id.container, f).commit();
                        return;
                    case 2:
                        f = new PopularFragment(MainActivity.this);
                        fManager.beginTransaction().replace(R.id.container, f).commit();
                        return;
                    case 3:
                        f = new BestRateFragment(MainActivity.this);
                        fManager.beginTransaction().replace(R.id.container, f).commit();
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
        if (auth.getCurrentUser() == null) {
            inflater.inflate(R.menu.menu_no_login, menu);
            return true;
        } else if (getIntent().getStringExtra("tip").equals("2")) {
            inflater.inflate(R.menu.menu_user, menu);
            return true;
        } else if (getIntent().getStringExtra("tip").equals("3")) {
            inflater.inflate(R.menu.menu, menu);
            return true;
        }
        inflater.inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.prijava:
                startActivity(new Intent(getApplicationContext(), Login.class));
                return true;
            case R.id.sign_out:
                auth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true;
            case R.id.user_data:
                update_data();
                return true;

            case R.id.profile:
                startActivity(new Intent(getApplicationContext(), UserProfile.class).putExtra("id", auth.getCurrentUser().getUid()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void update_data() {
        CollectionReference colRef = db.collection("Users");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getString("ID").equals(auth.getCurrentUser().getUid())) {
                            Intent i = new Intent(getApplicationContext(), User_data.class);
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
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}