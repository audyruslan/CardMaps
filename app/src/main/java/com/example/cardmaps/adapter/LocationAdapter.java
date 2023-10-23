package com.example.cardmaps.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardmaps.R;
import com.example.cardmaps.RuteActivity;
import com.example.cardmaps.model.LocationModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private ArrayList<LocationModel> locationList;
    private GoogleMap mMap;
    private Map<Marker, Integer> markerPositionMap;

    public LocationAdapter(ArrayList<LocationModel> locationList, GoogleMap map) {
        this.locationList = locationList;
        this.mMap = map;
        this.markerPositionMap = new HashMap<>();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNamaLokasi;
        public RatingBar ratingBar;
        public TextView tvRating;
        public TextView rutePerjalan;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNamaLokasi = itemView.findViewById(R.id.tvNamaLokasi);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvRating = itemView.findViewById(R.id.tvRating);
            rutePerjalan = itemView.findViewById(R.id.rutePerjalan);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationModel location = locationList.get(position);

        holder.tvNamaLokasi.setText(location.getNamaLokasi());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng lokasiMarker = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiMarker, 15));
                showMarker(location, lokasiMarker);
            }
        });

        holder.rutePerjalan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng lokasiMarker = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiMarker, 15));
                showMarker(location, lokasiMarker);

                Intent intent = new Intent(v.getContext(), RuteActivity.class);
                intent.putExtra("nama_posko", location.getNamaLokasi());
                intent.putExtra("latitude", location.getLatitude());
                intent.putExtra("longitude", location.getLongitude());
                v.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return locationList.size();
    }

    private void showMarker(LocationModel location, LatLng latLng) {

        for (Marker marker : markerPositionMap.keySet()) {
            marker.remove();
        }
        markerPositionMap.clear();

        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(location.getNamaLokasi()));
        markerPositionMap.put(marker, locationList.indexOf(location));
    }
}
