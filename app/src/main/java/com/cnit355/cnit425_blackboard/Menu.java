package com.cnit355.cnit425_blackboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ImageView btnMenuToRegister = findViewById(R.id.btnMenuToRegister);
        btnMenuToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationLocation.class));
        });

        ImageView btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

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