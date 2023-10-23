package com.example.cardmaps;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cardmaps.api.Url;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cardmaps.databinding.ActivityRuteBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvPosko, tvJarak, tvPath;
    private ActivityRuteBinding binding;
    private HashMap<String, LatLng> locationsMap = new HashMap<>();
    private HashMap<String, Double> edgeWeights = new HashMap<>();
    private List<String> lokasiList = new ArrayList<>();
    private String startLocationName = "Lokasi saya";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRuteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tvPosko = findViewById(R.id.tvPosko);
        tvJarak = findViewById(R.id.tvJarak);
        tvPath = findViewById(R.id.tvPath);

        String namaPosko = getIntent().getStringExtra("nama_posko");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapRute);
        mapFragment.getMapAsync(this);

        requestQueue = Volley.newRequestQueue(this);

        String tujuanPengguna = namaPosko;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url.TAMPIL_RUTE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String simpulAwal = jsonObject.getString("simpul_awal");
                        String simpulAkhir = jsonObject.getString("simpul_akhir");
                        double latAwal = jsonObject.getDouble("lat_awal");
                        double longAwal = jsonObject.getDouble("long_awal");
                        double latAkhir = jsonObject.getDouble("lat_akhir");
                        double longAkhir = jsonObject.getDouble("long_akhir");
                        double jarak = jsonObject.getDouble("jarak");

                        if (!lokasiList.contains(simpulAwal)) {
                            lokasiList.add(simpulAwal);
                        }

                        // Periksa apakah simpulAkhir sama dengan tujuanPengguna
                        if (simpulAkhir.equals(tujuanPengguna)) {
                            // Simpan lokasi tujuan sebagai lokasi akhir
                            locationsMap.put(simpulAkhir, new LatLng(latAkhir, longAkhir));
                        }

                        if (!lokasiList.contains(simpulAkhir)) {
                            lokasiList.add(simpulAkhir);
                        }

                        locationsMap.put(simpulAwal, new LatLng(latAwal, longAwal));
                        edgeWeights.put(simpulAwal + "-" + simpulAkhir, jarak);
                    }

                    // Panggil Method Algoritma Djisktra Kombinasi Node
                    List<String> path = calculateDijkstraPathWithNodeCombination(startLocationName, tujuanPengguna);
                    displayPathAndDistance(path);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
            }
        });

        requestQueue.add(stringRequest);
    }

    private List<String> calculateDijkstraPathWithNodeCombination(String start, String goal) {
        Set<String> unvisitedNodes = new HashSet<>(locationsMap.keySet());
        Set<String> visitedNodes = new HashSet<>();
        Map<String, Double> distance = new HashMap<>();
        Map<String, String> previousNode = new HashMap<>();

        // Inisialisasi jarak ke semua node dengan nilai tak hingga, kecuali node awal
        for (String node : locationsMap.keySet()) {
            distance.put(node, Double.POSITIVE_INFINITY);
        }
        distance.put(start, 0.0);

        while (!unvisitedNodes.isEmpty()) {
            // Temukan node dengan jarak terpendek yang belum dikunjungi
            String currentNode = null;
            double minDistance = Double.POSITIVE_INFINITY;

            for (String node : unvisitedNodes) {
                if (distance.get(node) < minDistance) {
                    currentNode = node;
                    minDistance = distance.get(node);
                }
            }

            // Jika node tujuan telah dikunjungi, maka jalur terpendek telah ditemukan
            if (currentNode.equals(goal)) {
                List<String> path = new ArrayList<>();
                String currentNodeInPath = goal;

                while (currentNodeInPath != null) {
                    path.add(currentNodeInPath);
                    currentNodeInPath = previousNode.get(currentNodeInPath);
                }

                Collections.reverse(path); // Balik urutan jalur
                return path;
            }

            unvisitedNodes.remove(currentNode);
            visitedNodes.add(currentNode);

            // Periksa tetangga-tetangga yang belum dikunjungi
            for (String neighbor : locationsMap.keySet()) {
                if (!visitedNodes.contains(neighbor)) {
                    String edgeKey = currentNode + "-" + neighbor;
                    double edgeWeight = edgeWeights.containsKey(edgeKey) ? edgeWeights.get(edgeKey) : Double.POSITIVE_INFINITY;
                    double tentativeDistance = distance.get(currentNode) + edgeWeight;

                    if (tentativeDistance < distance.get(neighbor)) {
                        distance.put(neighbor, tentativeDistance);
                        previousNode.put(neighbor, currentNode);
                    }
                }
            }
        }

        return new ArrayList<>(); // Tidak ditemukan jalur dari start ke goal
    }

    private void displayPathAndDistance(List<String> path) {
        mMap.clear();

        if (path.isEmpty()) {
            tvPath.setText("Tidak ada path yang ditemukan.");
            tvJarak.setText("");
        } else {
            StringBuilder pathText = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                pathText.append(path.get(i));
                if (i < path.size() - 1) {
                    pathText.append(" -> ");
                }
            }
            tvPath.setText("Path : " + pathText.toString());

            double totalDistance = 0.0;
            boolean pathValid = true;
            PolylineOptions polylineOptions = new PolylineOptions();

            for (int i = 0; i < path.size() - 1; i++) {
                String edgeKey = path.get(i) + "-" + path.get(i + 1);
                String reverseEdgeKey = path.get(i + 1) + "-" + path.get(i);
                if (edgeWeights.containsKey(edgeKey)) {
                    totalDistance += edgeWeights.get(edgeKey);

                    LatLng startLatLng = locationsMap.get(path.get(i));
                    LatLng endLatLng = locationsMap.get(path.get(i + 1));
                    polylineOptions.add(startLatLng, endLatLng);
                } else if (edgeWeights.containsKey(reverseEdgeKey)) {
                    totalDistance += edgeWeights.get(reverseEdgeKey);

                    LatLng startLatLng = locationsMap.get(path.get(i + 1));
                    LatLng endLatLng = locationsMap.get(path.get(i));
                    polylineOptions.add(startLatLng, endLatLng);
                } else {
                    pathValid = false;
                    break;
                }
            }

            mMap.addPolyline(polylineOptions);

            if (pathValid) {
                tvJarak.setText("Total Jarak : " + totalDistance);
            } else {
                tvJarak.setText("Jalur tidak valid atau Path tidak tersedia.");
            }

            // Membuat objek LatLngBounds.Builder untuk menyimpan batas
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // Menambahkan semua titik pada jalur ke dalam builder
            for (String nodeName : path) {
                LatLng latLng = locationsMap.get(nodeName);
                builder.include(latLng);
            }

            // Menambahkan marker lokasi awal dan akhir ke dalam builder
            String startLocation = path.get(0);
            String goalLocation = path.get(path.size() - 1);
            LatLng startLatLng = locationsMap.get(startLocation);
            LatLng goalLatLng = locationsMap.get(goalLocation);

            if (startLatLng != null && goalLatLng != null) {
                builder.include(startLatLng);
                builder.include(goalLatLng);
            }

            // Membuat objek LatLngBounds dari builder
            LatLngBounds bounds = builder.build();

            // Mengatur kamera agar fokus pada batas yang mencakup jalur, marker awal, dan marker akhir
            int padding = 100; // Padding (jarak) dari tepi peta ke jalur
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

            // Tampilkan marker lokasi awal dan akhir
            if (startLatLng != null) {
                mMap.addMarker(new MarkerOptions().position(startLatLng).title(startLocation));
            }
            if (goalLatLng != null) {
                mMap.addMarker(new MarkerOptions().position(goalLatLng).title(goalLocation));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        Double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        String namaPosko = getIntent().getStringExtra("nama_posko");

        LatLng lokasiPengguna = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions().position(lokasiPengguna).title(namaPosko));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiPengguna, 15));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiPengguna, 15));

        Marker marker = mMap.addMarker(new MarkerOptions().position(lokasiPengguna).title(namaPosko));
        marker.showInfoWindow();
    }
}
