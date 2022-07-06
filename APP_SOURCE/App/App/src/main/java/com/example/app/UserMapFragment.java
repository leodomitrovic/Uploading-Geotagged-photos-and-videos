package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

public class UserMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    Activity activity;
    List<String[]> posts_list;
    ProgressBar p;
    FloatingActionButton b;
    private GoogleMap mMap;
    int br = 0, count;

    public UserMapFragment() {
        // Required empty public constructor
    }

    public UserMapFragment(Activity activity, List<String[]> posts_list) {
        this.activity = activity;
        this.posts_list = posts_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        b = view.findViewById(R.id.floatingActionButton2);
        b.setVisibility(View.INVISIBLE);
        p = view.findViewById(R.id.progressBar);
        p.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        startActivity(new Intent(activity.getApplicationContext(), Post.class).putExtra("ID", marker.getSnippet()));
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        count = posts_list.size();
        for (int i = 0; i < posts_list.size(); i++) {
            p.setVisibility(View.VISIBLE);
            GeoPoint g = new GeoPoint(Double.valueOf(posts_list.get(i)[3]), Double.valueOf(posts_list.get(i)[4]));
            show_on_map(g, posts_list.get(i)[0], posts_list.get(i)[1]);
        }
    }

    void show_on_map(GeoPoint g, String url, String post_id) {
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
                        mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).snippet(post_id));
                        if (br == count) p.setVisibility(View.INVISIBLE);
                        return true;
                    }
                })
                .placeholder(cd).centerCrop().preload();
    }
}