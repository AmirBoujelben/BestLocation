package com.example.mybestlocation.ui.gallery;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentGalleryBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GalleryFragment extends Fragment implements OnMapReadyCallback {
    private FragmentGalleryBinding binding;
    private GoogleMap mMap;
    private LatLng selectedPosition; // Holds the selected position
    private EditText inputPseudo; // Input field for pseudo

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize pseudo input field
        inputPseudo = root.findViewById(R.id.input_pseudo);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set click listener for the "Add Position" button
        binding.btnAddPosition.setOnClickListener(view -> {
            String pseudo = inputPseudo.getText().toString();
            if (pseudo.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a pseudo!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPosition != null) {
                String latitude = String.valueOf(selectedPosition.latitude);
                String longitude = String.valueOf(selectedPosition.longitude);

                // Start task to add the position
                AddPositionTask task = new AddPositionTask(pseudo, latitude, longitude);
                task.execute();
            } else {
                Toast.makeText(getContext(), "Please select a position on the map!", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set initial camera position (centered on a default location, e.g., Paris)
        LatLng defaultLocation = new LatLng(48.8566, 2.3522); // Paris
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // Add a click listener to place a marker
        mMap.setOnMapClickListener(latLng -> {
            // Clear previous markers
            mMap.clear();

            // Add a marker at the clicked position
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Position"));

            // Save the selected position
            selectedPosition = latLng;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // AsyncTask for adding a position
    private class AddPositionTask extends AsyncTask<Void, Void, JSONObject> {
        private final String pseudo;
        private final String latitude;
        private final String longitude;
        private AlertDialog alert;

        public AddPositionTask(String pseudo, String latitude, String longitude) {
            this.pseudo = pseudo;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Adding Position");
            dialog.setMessage("Please wait...");
            alert = dialog.create();
            alert.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONParser parser = new JSONParser();
            HashMap<String, String> params = new HashMap<>();
            params.put("pseudo", pseudo);
            params.put("latitude", latitude);
            params.put("longitude", longitude);

            return parser.makeHttpRequest(Config.Url_AddPosition, "POST", params);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            alert.dismiss();
            if (result != null) {
                try {
                    int success = result.getInt("success");
                    String message = result.getString("message");

                    if (success == 1) {
                        Toast.makeText(getContext(), "Position added successfully: " + message, Toast.LENGTH_SHORT).show();
                        // Clear inputs
                        inputPseudo.setText("");
                        selectedPosition = null;
                        mMap.clear(); // Clear the marker from the map
                    } else {
                        Toast.makeText(getContext(), "Failed to add position: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "An error occurred!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error: No response from server!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
