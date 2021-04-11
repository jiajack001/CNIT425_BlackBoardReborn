package com.cnit355.cnit425_blackboard;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

public class RegistrationLocation extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap iMap;
    ArrayList<String> AvailableLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_location);

        //Initialize map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //Initialize ListView
        AvailableLocation = new ArrayList<>();
        ListView lv = findViewById(R.id.listViewLocation);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                        AvailableLocation));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        iMap = googleMap;
    }
}