package com.example.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.ui.IconGenerator;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {
    Activity activity;
    private GoogleMap mMap;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    ProgressBar p;
    FloatingActionButton b;
    protected LocationManager locationManager;
    int count, br;
    Location l;

    public MapsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public MapsFragment(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        b = view.findViewById(R.id.floatingActionButton2);
        p = view.findViewById(R.id.progressBar);
        p.setVisibility(View.INVISIBLE);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p.getVisibility() == View.VISIBLE) return;
                p.setVisibility(View.VISIBLE);
                if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 99);

                }
                locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MapsFragment.this);
            }
        });
        return view;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            p.setVisibility(View.INVISIBLE);
            LatLng latLong = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLong));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 8));
            locationManager.removeUpdates(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        auth = FirebaseAuth.getInstance();

        CollectionReference colRef = db.collection("Posts");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    count = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getBoolean("Approved")) {
                            if (auth.getCurrentUser() == null) count++;
                            else {
                                if (!(doc.getString("User-ID").equals(auth.getCurrentUser().getUid()))) count++;
                            }
                        }
                    }
                    if (count == 0) return;
                    br = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        p.setVisibility(View.VISIBLE);
                        if (doc.getBoolean("Approved")) {
                            if (auth.getCurrentUser() == null) {
                                show_on_map(doc.getGeoPoint("GeoTag"), doc.getString("Image URL"), doc.getId());
                            }
                            else {
                                if (!(doc.getString("User-ID").equals(auth.getCurrentUser().getUid()))) {
                                    show_on_map(doc.getGeoPoint("GeoTag"), doc.getString("Image URL"), doc.getId());
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(activity.getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void show_on_map(GeoPoint g, String url, String id) {
        Uri uri = Uri.parse(url);
        LatLng location = new LatLng(g.getLatitude(), g.getLongitude());
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(activity.getApplicationContext(), R.color.black));
        Glide.with(activity.getApplicationContext()).load(uri.toString())
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                        p.setVisibility(View.INVISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        br++;
                        p.setVisibility(View.VISIBLE);
                        BitmapDrawable d = (BitmapDrawable) resource;
                        Bitmap b = Bitmap.createScaledBitmap(d.getBitmap(), 84, 84, false);
                        ImageView mImageView = new ImageView(activity.getApplicationContext());
                        IconGenerator mIconGenerator = new IconGenerator(activity.getApplicationContext());
                        mImageView.setImageBitmap(b);
                        mIconGenerator.setContentView(mImageView);
                        Bitmap iconBitmap = mIconGenerator.makeIcon();
                        mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).snippet(id));
                        if (br == count) p.setVisibility(View.INVISIBLE);
                        return true;
                    }
                })
                .placeholder(cd).centerCrop().preload();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        startActivity(new Intent(activity.getApplicationContext(), Post.class).putExtra("ID", marker.getSnippet()));
        return false;
    }
}