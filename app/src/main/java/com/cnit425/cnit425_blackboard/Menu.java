package com.cnit425.cnit425_blackboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Menu extends AppCompatActivity {

    private String uid;
    private DatabaseReference mVaccineRef;
    private ValueEventListener mVaccineListener;

    private Boolean vaccinated = false;
    private boolean vaccination_data_ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        uid = FirebaseAuth.getInstance().getUid();
        mVaccineRef = FirebaseDatabase.getInstance().getReference("user").child(uid).child("Vaccination");
        mVaccineListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vaccinated = snapshot.child("Vaccinated").getValue(Boolean.class);
                vaccination_data_ready = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        //btnMenuToRegister onClick:  Reserve a vaccination
        ImageView btnMenuToRegister = findViewById(R.id.btnMenuToRegister);
        btnMenuToRegister.setOnClickListener(v -> {
            if (!vaccination_data_ready) {
                return;
            }
            //check if the user has completed its vaccination
            //if the user has completed the vaccination, prohibit it from reservation again
            if (vaccinated != null && vaccinated) {
                Toast.makeText(getApplicationContext(),
                        "Congrats! You have finished your vaccination!" +
                                "\nYou don't need to reserve another vaccination!\n" +
                                "If you have question about this, please contact us using Report Issues.",
                        Toast.LENGTH_LONG).show();
                //if the vaccinated data is lost, logcat the issue
            } else if (vaccinated == null) {
                Log.i("Menu.java", "Vaccinated history not founded!");
                //if the user has not finished the vaccination, allow it to reserve a spot
            } else {
                startActivity(new Intent(getApplicationContext(), RegistrationLocation.class));
            }
        });

        //btnSignOut onClick: sign out and send user back to Login Menu
        ImageView btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

        //btnStatus onClick: if 100% vaccinated -> vaccination; otherwise -> registration window
        ImageView btnStatus = findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(v -> {
            //check if the data is downloaded and ready
            if (!vaccination_data_ready) {
                return;
            }
            //vaccinated: true
            if (vaccinated != null && vaccinated) {
                startActivity(new Intent(getApplicationContext(), VaccinationStatus.class));
            //vaccinated: null
            } else if (vaccinated == null) {
                Log.i("Menu.java", "Vaccinated history not founded!");
            //vaccinated: false
            } else {
                startActivity(new Intent(getApplicationContext(), RegistrationResult.class));
            }
        });

    }

    //while onResume, add listener to update info from database
    @Override
    protected void onResume() {
        mVaccineRef.addValueEventListener(mVaccineListener);
        super.onResume();
    }
    //while onPause, remove the listener that updates info from database
    @Override
    protected void onPause() {
        mVaccineRef.removeEventListener(mVaccineListener);
        super.onPause();
    }









    //ignore: Function to add date & time based on Addr to database

    //        addNewRef("location1",
//                "France A. Córdova Recreational Sports Center",
//                "355 N Martin Jischke Dr, West Lafayette, IN 47906",
//                40.428488,-86.922363);
//        addNewRef("location2",
//                "Tippecanoe County Health Department Vital Records and Nursing",
//                "629 N 6th St, Lafayette, IN 47901",
//                40.423707,-86.890001);
//        addNewRef("location3",
//                "CVS Pharmacy",
//                "1725 Salem St, Lafayette, IN 47904",
//                40.425137,-86.878092);
    public void addNewRef(String addr,String name, String address,double lat, double longi){
        DatabaseReference mDateRef = FirebaseDatabase.getInstance().getReference("location").child(addr);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mDateRef.child("name").setValue(name);
        mDateRef.child("address").setValue(address);
        mDateRef.child("latitude").setValue(lat);
        mDateRef.child("longitude").setValue(longi);
        mDateRef = mDateRef.child("time");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
        try {
            Date mDate = simpleDateFormat.parse("2021-05-01");
            for (int i = 1; i<=31;i++){
                mDateRef.child(simpleDateFormat.format(mDate));
                Date mDate1 = simpleDateFormat1.parse("08:00");
                for (int j = 8;j<=16;j++){
                    mDate1.setHours(j);
                    String first = simpleDateFormat1.format(mDate1);
                    mDate1.setHours(j+1);
                    String second = simpleDateFormat1.format(mDate1);
                    mDateRef.child(simpleDateFormat.format(mDate)).child(first +"-"+second).child("Count").setValue(0);
                }
                mDate.setTime(mDate.getTime() + 86400000);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /*
    *
    * France A. Córdova Recreational Sports Center
    * 355 N Martin Jischke Dr, West Lafayette, IN 47906
    *
    * Tippecanoe County Health Department Vital Records and Nursing
    * 629 N 6th St, Lafayette, IN 47901
    *
    * CVS Pharmacy
    * 1725 Salem St, Lafayette, IN 47904
    *
    * */
}