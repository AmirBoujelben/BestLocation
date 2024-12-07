package com.example.mybestlocation.ui.gallery;

import android.annotation.SuppressLint;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.app.AlertDialog;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;

public class GalleryFragment extends Fragment implements OnMapReadyCallback {
    private FragmentGalleryBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private LatLng selectedPosition; // Holds the selected position
    private EditText inputPseudo;

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

        // Initialize the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

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
        // "Send SMS to Friend" button logic
        binding.btnSendSms.setOnClickListener(v -> showSmsPopup());

        return root;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


            mMap.setMyLocationEnabled(true);

            // Get the current location and move the camera
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } else {
                    // Handle case where location is null
                    Toast.makeText(getContext(), "Unable to fetch location!", Toast.LENGTH_SHORT).show();
                }
            });

        // Set click listener for map
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

    private void showSmsPopup() {
        // Create a popup dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        View popupView = getLayoutInflater().inflate(R.layout.dialog_send_sms, null);
        dialogBuilder.setView(popupView);

        // Initialize dialog components
        EditText phoneNumberInput = popupView.findViewById(R.id.input_phone_number);
        Button sendButton = popupView.findViewById(R.id.btn_send_sms_popup);
        Button cancelButton = popupView.findViewById(R.id.btn_cancel_sms);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        sendButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString();
            if (!phoneNumber.isEmpty()) {
                sendSms(phoneNumber);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a phone number!", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }
    private void sendSms(String phoneNumber) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "FINDFRIENDS: Envoyer moi votre position", null, null);
            Toast.makeText(getContext(), "SMS sent successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to send SMS. Please try again.", Toast.LENGTH_SHORT).show();
        }
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
            try {
                JSONParser parser = new JSONParser();
                HashMap<String, String> params = new HashMap<>();
                params.put("pseudo", pseudo);
                params.put("latitude", latitude);
                params.put("longitude", longitude);

                return parser.makeHttpRequest(Config.Url_AddPosition, "POST", params);
            } catch (Exception e) {
                e.printStackTrace();
                return null; // Retourne null en cas d'échec
            }
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
