package com.example.mybestlocation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private List<Position> positionList;
    private Context context;

    public PositionAdapter(List<Position> positionList, Context context) {
        this.positionList = positionList;
        this.context = context; // Assign context here
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_position, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positionList.get(position);
        holder.tvPseudo.setText(currentPosition.getPseudo());
        holder.tvCoordinates.setText(
                "Coordinates: (" + currentPosition.getLatitude() + ", " + currentPosition.getLongitude() + ")"
        );

        // Use the context here to start the activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapActivity.class);
            intent.putExtra("latitude", currentPosition.getLatitude());
            intent.putExtra("longitude", currentPosition.getLongitude());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return positionList.size();
    }

    public static class PositionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPseudo, tvCoordinates;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPseudo = itemView.findViewById(R.id.tvPseudo);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
        }
    }
}
