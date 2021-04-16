package com.cnit425.cnit425_blackboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

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

        //define Vaccine Value Listener
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

        //btnSignOut onClick: sign out and send user back to Login Menu
        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
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

    //btnScan onClick: Scan QR code
    public void btnScanOnClick(View view){
        new IntentIntegrator(this).initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //check null before continuing
        if(result == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if(result.getContents() == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            return;
        }

        //if !null, convert to JSON object
        try {
            JSONObject mJSON = new JSONObject(result.getContents());
            //check if the JSON object is generated from this app
            if(!mJSON.has("ProtectPurdueType")){
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_LONG).show();
                return;
            }
            //use the email info to check if the user has been vaccinated
            String email = mJSON.getString("email");
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("user");
            mRef.orderByChild("Email").equalTo(email).get().addOnCompleteListener(task -> {
                Boolean vaccinated_scan = false;
                //check if task successful -> true: get if the person_scanned has been vaccinated
                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                    return;
                }
                for(DataSnapshot shot: Objects.requireNonNull(task.getResult()).getChildren()){
                    vaccinated_scan = shot.child("Vaccination").child("Vaccinated").getValue(Boolean.class);
                }
                //display different dialog based on vaccinated_scan
                if(vaccinated_scan){
                    displayDialog("Protect Purdue",
                            "Confirmed! The person has been vaccinated!",
                            "Great!");
                }else{
                    displayDialog("Protect Purdue",
                            "The person has NOT been vaccinated!",
                            "Ok");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //btnMenuToRegister onClick:  Reserve a vaccination
    public void btnMenuToRegisterOnClick(View view){
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
    }

    //btnReport onClick: open new activity to report
    public void btnReportIssuesOnClick(View view){
        startActivity(new Intent(this, ReportIssues.class));
    }

    //btnStatus onClick: if 100% vaccinated -> vaccination; otherwise -> registration window
    public void btnStatusOnClick(View view){
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
    }

    //btnOpenProtectPurdue onClick: open Protect Purdue website
    public void btnOpenProtectPurdueOnClick(View view){
        String uri = "https://protect.purdue.edu/";
        Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(mIntent);
    }

    //display dialog with title, msg, btnText
    public void displayDialog(String title, String message,String btnMsg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(btnMsg, (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    /*
    ignore: Function to add date & time based on Addr to database
     */
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