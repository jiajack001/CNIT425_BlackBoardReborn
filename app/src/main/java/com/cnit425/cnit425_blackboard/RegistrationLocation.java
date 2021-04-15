package com.cnit425.cnit425_blackboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RegistrationLocation extends AppCompatActivity implements OnMapReadyCallback {

    DatabaseReference ref;
    ValueEventListener refListener;

    GoogleMap iMap;

    ArrayList<String> AvailableLocationName;
    HashMap<String,String> AvailableLocationMap;
    HashMap<String,Double[]> AvailableGeoLocation;

    ArrayAdapter lv_adapter;
    String location_selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_location);

        //Initialize map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment!= null){
            mapFragment.getMapAsync(this);
        }else{
            Toast.makeText(this,"Google Maps Initialization Failed!",Toast.LENGTH_SHORT).show();
        }

        //Initialize ListView
        AvailableLocationName = new ArrayList<>();
        AvailableLocationMap = new HashMap<>();
        AvailableGeoLocation = new HashMap<>();
        lv_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                AvailableLocationName);
        ListView lv = findViewById(R.id.listViewLocation);
        lv.setAdapter(lv_adapter);

        //populate the ListView
        ref = FirebaseDatabase.getInstance().getReference("location");
        refListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get location info and update the ListView
                if(AvailableLocationName!=null){
                    AvailableLocationName.clear();
                    AvailableLocationMap.clear();
                    AvailableGeoLocation.clear();
                }

                for (DataSnapshot snap: dataSnapshot.getChildren()){
                    //get the location info from database
                    String name = snap.child("name").getValue(String.class);
                    String address = snap.child("address").getValue(String.class);
                    Double latitude = snap.child("latitude").getValue(Double.class);
                    Double longitude = snap.child("longitude").getValue(Double.class);
                    //update the ArrayList and ArrayAdapter
                    AvailableLocationName.add(name);
                    AvailableLocationMap.put(name,address);
                    AvailableGeoLocation.put(name,new Double[]{latitude,longitude});
                }
                lv_adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("RegistrationLocation", "refListener:onCancelled", databaseError.toException());
            }
        };

        //set ListView item onClick Event
        lv.setOnItemClickListener((parent, view, position, id) -> {
            if(iMap!= null){
                //retrieve location info from ArrayList and HashMap
                location_selected = AvailableLocationName.get(position);
                String address = AvailableLocationMap.get(location_selected);
                Double[] geo = AvailableGeoLocation.get(location_selected);

                //set up a new Marker
                assert geo != null;
                LatLng location = new LatLng(geo[0],geo[1]);
                MarkerOptions marker = new MarkerOptions().position(location).title(location_selected).snippet(address);

                //clear previous markers and add a new one
                iMap.clear();
                iMap.addMarker(marker).showInfoWindow();
                iMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,16.0f));

                //enable next button
                findViewById(R.id.btnLocationToTime).setEnabled(true);
            }
        });
    }

    //while onResume, add listener to update info from database
    @Override
    protected void onResume() {
        ref.addValueEventListener(refListener);
        if(location_selected==null){
            findViewById(R.id.btnLocationToTime).setEnabled(false);
        }
        super.onResume();
    }
    //while onPause, remove the listener that updates info from database
    @Override
    protected void onPause() {
        ref.removeEventListener(refListener);
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        iMap = googleMap;
        iMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void btnLocationToTimeOnClick(View view){
        Intent next = new Intent(this, RegistrationTimeTicket.class);
        next.putExtra("location",location_selected);
        next.putExtra("address",AvailableLocationMap.get(location_selected));
        startActivity(next);
    }
}