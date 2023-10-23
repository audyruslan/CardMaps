package com.example.cardmaps;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.cardmaps.adapter.LocationAdapter;
import com.example.cardmaps.model.LocationModel;
import com.example.cardmaps.api.Url;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RecyclerView recyclerView;
    private LocationAdapter locationAdapter;
    private ArrayList<LocationModel> locationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        recyclerView = findViewById(R.id.rvListLocation);
        locationList = new ArrayList<>();

        // Inisialisasi peta
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fetchLocationDataFromServer();
    }

    private void fetchLocationDataFromServer() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Url.TAMPIL_LOKASI, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            Log.d("MyApp", "Response: " + response.toString());

                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String namaLokasi = jsonObject.getString("nama_posko");
                                double latitude = jsonObject.getDouble("latitude");
                                double longitude = jsonObject.getDouble("longitude");

                                LocationModel location = new LocationModel(namaLokasi, latitude, longitude);
                                locationList.add(location);

                                LatLng latLng = new LatLng(latitude, longitude);
                                mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(namaLokasi));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        locationAdapter = new LocationAdapter(locationList, mMap);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MapsActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        recyclerView.setAdapter(locationAdapter);

                        if (!locationList.isEmpty()) {
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (LocationModel location : locationList) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                builder.include(latLng);
                            }
                            LatLngBounds bounds = builder.build();
                            int padding = 50;
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            mMap.animateCamera(cameraUpdate);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapsActivity.this, "Kesalhan" + error, Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }
}
