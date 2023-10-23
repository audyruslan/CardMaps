package com.example.cardmaps;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.graphics.Point;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cardmaps.api.Url;
import com.example.cardmaps.model.PoskoModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PoskoActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvPosko, tvJarak, tvPath;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private double latitudePosko = 0.0;
    private double longitudePosko = 0.0;
    private ArrayList<PoskoModel> poskoList = new ArrayList<>();
    private PoskoModel poskoTerdekat;
    private double jarakTerdekat = Double.MAX_VALUE;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posko);

        tvPosko = findViewById(R.id.tvPosko);
        tvJarak = findViewById(R.id.tvJarak);
        tvPath = findViewById(R.id.tvPath);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPosko);
        mapFragment.getMapAsync(this);


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Url.TAMPIL_POSKO, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject poskoObject = response.getJSONObject(i);
                            String namaPosko = poskoObject.getString("nama_posko");
                            double latitude = poskoObject.getDouble("latitude");
                            double longitude = poskoObject.getDouble("longitude");

                            PoskoModel posko = new PoskoModel(namaPosko, latitude, longitude);
                            poskoList.add(posko);
                        }

                        hitungPoskoTerdekat();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(PoskoActivity.this, "Kesalahan " + e, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PoskoActivity.this, "Kesalahan " + error, Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonArrayRequest);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void hitungPoskoTerdekat() {
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();

            // Hitung posko terdekat
            for (PoskoModel posko : poskoList) {
                double jarak = hitungJarak(latitude, longitude, posko.getLatitude(), posko.getLongitude());
                if (jarak < jarakTerdekat) {
                    jarakTerdekat = jarak;
                    poskoTerdekat = posko;

                    latitudePosko = posko.getLatitude();
                    longitudePosko = posko.getLongitude();
                }
            }
            tampilkanInfoPoskoTerdekat();

            if (poskoTerdekat != null) {
                direction();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            LatLng myLocation = new LatLng(latitude, longitude);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Lokasi saya"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                            marker.showInfoWindow();

                            direction();
                        }
                    });
        }

    }

    private double hitungJarak(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void tampilkanInfoPoskoTerdekat() {
        if (poskoTerdekat != null) {
            tvPosko.setText(poskoTerdekat.getNamaPosko());
            tvPath.setText("Posko Terdekat: " + poskoTerdekat.getNamaPosko());
            DecimalFormat df = new DecimalFormat("#.##");
            String jarakTerdekatStr = df.format(jarakTerdekat);
            tvJarak.setText("Jarak ke Posko Terdekat: " + jarakTerdekatStr + " km");
            //tvJarak.setText("LatLng PoskoModel : " + latitudePosko + "," + longitudePosko);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    onMapReady(mMap);
                }
            }
        }
    }

    private void direction() {
        if (poskoTerdekat != null) {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "destination=" + latitudePosko + "," + longitudePosko +
                    "&origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                    "&mode=driving" +
                    "&alternatives=true" +
                    "&key=AIzaSyCvzH9sSlNBWmb3KUjvVKe1eJEymF9W0cM";

            Toast.makeText(PoskoActivity.this, "Directions :" + latitudePosko + "," + longitudePosko, Toast.LENGTH_SHORT).show();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            String status = response.getString("status");
                            if (status.equals("OK")) {
                                JSONArray routes = response.getJSONArray("routes");

                                for (int i = 0; i < routes.length(); i++) {
                                    ArrayList<LatLng> points = new ArrayList<>();
                                    PolylineOptions polylineOptions = new PolylineOptions();

                                    JSONObject route = routes.getJSONObject(i);
                                    JSONArray legs = route.getJSONArray("legs");

                                    for (int j = 0; j < legs.length(); j++) {
                                        JSONObject leg = legs.getJSONObject(j);
                                        JSONArray steps = leg.getJSONArray("steps");

                                        for (int k = 0; k < steps.length(); k++) {
                                            JSONObject step = steps.getJSONObject(k);
                                            String polyline = step.getJSONObject("polyline").getString("points");
                                            List<LatLng> list = decodePoly(polyline);

                                            for (int l = 0; l < list.size(); l++) {
                                                points.add(list.get(l));
                                            }
                                        }

                                        polylineOptions.addAll(points);
                                        polylineOptions.width(10f);
                                        polylineOptions.color(ContextCompat.getColor(this, R.color.colorLine));
                                        polylineOptions.geodesic(true);
                                    }

                                    mMap.addPolyline(polylineOptions);
                                }

                                mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title("Lokasi Saya"));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(latitudePosko, longitudePosko)).title(poskoTerdekat.getNamaPosko()));

                                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                                boundsBuilder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                                boundsBuilder.include(new LatLng(latitudePosko, longitudePosko));

                                Point displaySize = new Point();
                                getWindowManager().getDefaultDisplay().getSize(displaySize);

                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 230));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> error.printStackTrace());

            int timeout = 30000;
            int maxRetries = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;
            float backoffMultiplier = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(timeout, maxRetries, backoffMultiplier));
            requestQueue.add(jsonObjectRequest);
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int len = encoded.length();
        int index = 0;
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
                index++;
            } while (b >= 0x20);
            lat += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
                index++;
            } while (b >= 0x20);
            lng += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            double latitude = lat / 1E5;
            double longitude = lng / 1E5;
            poly.add(new LatLng(latitude, longitude));
        }
        return poly;
    }
}