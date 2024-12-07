package com.example.mybestlocation;

import android.os.Bundle;
import android.util.Log;

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

        Log.d("MapActivity", "Latitude: " + latitude + ", Longitude: " + longitude);

        // Use SupportMapFragment for compatibility with AppCompatActivity
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mMap = googleMap;

            // Définir la position et ajouter un marqueur
            LatLng position = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(position).title("Position"));
            Log.d("MapActivity", "Camera is moving to: " + position.toString());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        } else {
            // Optionnel : Gérer le cas où GoogleMap est null
            throw new IllegalStateException("Erreur lors de l'initialisation de Google Map !");
        }
    }

}
