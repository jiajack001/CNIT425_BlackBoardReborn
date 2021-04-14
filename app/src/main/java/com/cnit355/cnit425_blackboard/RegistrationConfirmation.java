package com.cnit355.cnit425_blackboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegistrationConfirmation extends AppCompatActivity {

    //all data about registration
    private String location_serial;
    private String location;
    private String address;
    private String date;
    private String time;
    private String email;
    private String uid;

    //databaseReference
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_confirmation);

        //get the intent to retrieve desired registration info
        Intent mIntent = getIntent();

        //get info from Intent
        location_serial = mIntent.getStringExtra("DatabaseRef");
        location = mIntent.getStringExtra("location");
        address = mIntent.getStringExtra("address");
        date = mIntent.getStringExtra("date");
        time = mIntent.getStringExtra("time");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null){
            email = user.getEmail();
            uid = user.getUid();
        }

        //find TextView & setText to display info for confirmation
        TextView txtLocation = findViewById(R.id.txtLocation_Confirmation);
        TextView txtAddress = findViewById(R.id.txtAddress_Confirmation);
        TextView txtDate = findViewById(R.id.txtDate_Confirmation);
        TextView txtTime = findViewById(R.id.txtTime_Confirmation);
        TextView txtEmail = findViewById(R.id.txtEmail_Confirmation);
        txtLocation.setText(location);
        txtAddress.setText(address);
        txtDate.setText(date);
        txtTime.setText(time);
        txtEmail.setText(email);

        //direct the DatabaseReference into the location/<location_serial>/time/<date>/<time> in the database
        mRef = FirebaseDatabase.getInstance().
                getReference("location").child(location_serial).child("time").child(date).child(time);
    }

    public void btnConfirmOnClick(View view){
        //pre-check before logging info into the database
        if (email == null || uid == null){
            Toast.makeText(this,
                    "User info is not found!\nPlease re-login and try again!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (mRef == null){
            Toast.makeText(this,
                    "Database is disconnected.\nPlease close the app and try again later!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        //check the number of people has registered for this time slots
        mRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                return;
            }
            //if the retrieve was successful and count is less than 15, proceeds to next step
            Integer cnt = Objects.requireNonNull(task.getResult()).child("Count").getValue(Integer.class);
            if (cnt!=null && cnt < 15){
                //log the user under location/time/date/hours/
                mRef.child(uid).setValue(email);
                mRef.child("Count").setValue(cnt+1);

                //add data under /user/uid/Vaccination/Registration
                DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference("user");
                mDataRef = mDataRef.child(uid).child("Vaccination").child("Registration");
                mDataRef.child("location").setValue(location_serial);
                mDataRef.child("date").setValue(date);
                mDataRef.child("time").setValue(time);

                //display the result page
                Intent mIntent = new Intent(getApplicationContext(), RegistrationResult.class);
                startActivity(mIntent);
            }else{
                Toast.makeText(getApplicationContext(),
                        "Error: Please return and try again!\nThe selected time may no longer be available!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}