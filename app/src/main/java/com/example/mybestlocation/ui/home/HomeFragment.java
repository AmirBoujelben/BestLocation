package com.example.mybestlocation.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.PositionAdapter;
import com.example.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private List<Position> data = new ArrayList<>();
    private PositionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PositionAdapter(data, getActivity());  // Passing the context here
        binding.recyclerView.setAdapter(adapter);

        binding.btnDownload.setOnClickListener(view -> {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class DownloadTask extends AsyncTask<Void, Void, Void> {
        AlertDialog alert;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Download");
            dialog.setMessage("Please wait...");
            alert = dialog.create();
            alert.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.Url_GetAll);

            if (response != null) {
                try {
                    int success = response.getInt("success");
                    if (success > 0) {
                        JSONArray tab = response.getJSONArray("positions");

                        // Clear existing data to avoid duplicates
                        data.clear();

                        for (int i = 0; i < tab.length(); i++) {
                            JSONObject lignes = tab.getJSONObject(i);
                            int idposition = lignes.getInt("idposition");
                            String pseudo = lignes.getString("pseudo");
                            String longitude = lignes.getString("longitude");
                            String latitude = lignes.getString("latitude");

                            if (longitude != null && latitude != null) {
                                data.add(new Position(idposition, pseudo, longitude, latitude));
                            }
                        }
                    } else {
                        // Handle unsuccessful response here
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Optionally show a message to the user about the error
                }
            } else {
                // Handle case where response is null (e.g., no internet)
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged();
            alert.dismiss();
        }
    }
}
