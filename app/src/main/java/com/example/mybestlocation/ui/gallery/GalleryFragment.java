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
import com.example.mybestlocation.databinding.FragmentGalleryBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set click listener for the "Add Position" button
        binding.btnAddPosition.setOnClickListener(view -> {
            String pseudo = binding.inputPseudo.getText().toString();
            String latitude = binding.inputLatitude.getText().toString();
            String longitude = binding.inputLongitude.getText().toString();

            if (pseudo.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate latitude and longitude format
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);

                if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    Toast.makeText(getContext(), "Invalid latitude or longitude values!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Latitude and Longitude must be numbers!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start task to add the position
            AddPositionTask task = new AddPositionTask(pseudo, latitude, longitude);
            task.execute();
        });

        return root;
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
                        // Optionally, clear the inputs
                        binding.inputPseudo.setText("");
                        binding.inputLatitude.setText("");
                        binding.inputLongitude.setText("");
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
