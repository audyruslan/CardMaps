package com.example.cardmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class EvakuasiActivity extends AppCompatActivity {

    ImageView btnEvakuasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evakuasi);

        btnEvakuasi = findViewById(R.id.btnEvakuasi);

        btnEvakuasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EvakuasiActivity.this, PoskoActivity.class);
                startActivity(intent);
            }
        });
    }
}
