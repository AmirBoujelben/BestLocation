package com.example.mybestlocation;

import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import androidx.appcompat.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get the coordinates passed from the RecyclerView item
        latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));
        longitude = Double.parseDouble(getIntent().getStringExtra("longitude"));

        // Use SupportMapFragment for compatibility with AppCompatActivity
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Move the camera to the selected position and add a marker
        LatLng position = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(position).title("Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
    }
}
