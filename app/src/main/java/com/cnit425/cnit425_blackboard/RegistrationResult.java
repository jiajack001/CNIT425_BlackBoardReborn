package com.cnit425.cnit425_blackboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationResult extends AppCompatActivity {
    private DatabaseReference userRef;
    private DatabaseReference geoRef;

    private String location;
    private String location_name;
    private String address;
    private String date;
    private String time;
    private String email;
    private Boolean completed;
    private String uid;

    private ValueEventListener userValueListener;
    private ValueEventListener locationValueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_result);

        //get reference to user info and location info
        userRef = FirebaseDatabase.getInstance().getReference("user");
        geoRef = FirebaseDatabase.getInstance().getReference("location");

        //get userID for the correct user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        userRef = userRef.child(uid);
        userValueListener = new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get email & location_serial & date & time from user/info
                email = snapshot.child("Email").getValue(String.class);
                Integer count = snapshot.child("Vaccination")
                        .child("VaccineCount").getValue(Integer.class);
                String dose = String.format("Dose%d",count+1);
                if(!snapshot.child("Vaccination").hasChild(dose)){
                       dose = String.format("Dose%d",count);
                       if(!snapshot.child("Vaccination").hasChild(dose)){
                           Toast.makeText(getApplicationContext(),
                                   "You have not reserved any vaccination, please reserve first!",
                                   Toast.LENGTH_LONG).show();
                           RegistrationResult.this.finish();
                           return;
                       }
                }
                location = snapshot.child("Vaccination").child(dose).child("location").getValue(String.class);
                date = snapshot.child("Vaccination").child(dose).child("date").getValue(String.class);
                time = snapshot.child("Vaccination").child(dose).child("time").getValue(String.class);
                completed = snapshot.child("Vaccination").child(dose).child("completed").getValue(Boolean.class);

                //find the location info based on location_serial
                if(locationValueListener!= null){
                    geoRef.child(location).addValueEventListener(locationValueListener);
                }

                //setText to display the info on Activity
                TextView txtDate = findViewById(R.id.txtDate_Result);
                TextView txtTime = findViewById(R.id.txtTime_Result);
                TextView txtEmail = findViewById(R.id.txtEmail_Result);
                RadioButton radCompleted = findViewById(R.id.radCompleted);
                txtDate.setText(date);
                txtTime.setText(time);
                txtEmail.setText(email);
                radCompleted.setChecked(completed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        locationValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //retrieve the location_name & address from location_serial node
                location_name = snapshot.child("name").getValue(String.class);
                address = snapshot.child("address").getValue(String.class);
                //setText to display location name and address on activity
                TextView txtLocation = findViewById(R.id.txtLocation_Result);
                TextView txtAddress = findViewById(R.id.txtAddress_Result);
                txtLocation.setText(location_name);
                txtAddress.setText(address);
                //create the QRCode based on the info
                createQRCode();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public void createQRCode(){
        //create a JSON object to embed its String into QR code
        String jsonString = "";
        try {
            JSONObject mJSON = new JSONObject();
            mJSON.put("location",location_name);
            mJSON.put("address", address);
            mJSON.put("date", date);
            mJSON.put("time", time);
            mJSON.put("email",email);
            mJSON.put("completed",completed);
            jsonString = mJSON.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //convert the String to QRcode
        try{
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(jsonString, BarcodeFormat.QR_CODE,400,400);
            ImageView imgView = findViewById(R.id.imgView_Registration_QRCode);
            imgView.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void btnResultBackToMenuOnClick(View view){
        startActivity(new Intent(this,Menu.class));
    }

    @Override
    public void onResume(){
        userRef.addValueEventListener(userValueListener);
        super.onResume();
    }

    @Override
    public void onPause(){
        userRef.removeEventListener(userValueListener);
        if(location!=null){
            geoRef.child(location).removeEventListener(locationValueListener);
        }
        super.onPause();
    }
}