package com.cnit425.cnit425_blackboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class RegistrationTimeTicket extends AppCompatActivity {
    //
    private String location_selected;
    private String address_selected;
    private String date_selected;
    private String date_begin;
    private String date_end;

    private DatabaseReference mRef;
    private ValueEventListener availabilityTracker;

    private ArrayList<String> AvailabilityTime;
    private HashMap<String,Integer> AvailabilitySlot;
    private TimeSlotArrayAdapter adapter;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_time);

        //display the location that was selected previously
        location_selected = getIntent().getStringExtra("location");
        address_selected = getIntent().getStringExtra("address");
        ((TextView)findViewById(R.id.tagSelectedLocation)).append(location_selected);

        //Define the EventListener for Date changed
        availabilityTracker = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(AvailabilityTime!= null){
                    AvailabilityTime.clear();
                    AvailabilitySlot.clear();
                    for(DataSnapshot snap: snapshot.getChildren()){
                        AvailabilityTime.add(snap.getKey());
                        AvailabilitySlot.put(snap.getKey(),snap.child("Count").getValue(Integer.class));
                    }
                    adapter.notifyDataSetChanged();
                    if(AvailabilityTime.isEmpty()){
                        Toast.makeText(getApplicationContext(),
                                "Vaccination is not available on the selected date, please select another date.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        //connect to database -> find the databaseReference based on the address name
        mRef = FirebaseDatabase.getInstance().getReference("location");
        mRef.orderByChild("name").equalTo(location_selected).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    for(DataSnapshot data: Objects.requireNonNull(task.getResult()).getChildren()){
                        mRef = data.getRef();

                        CalendarView cal = findViewById(R.id.calendarRegister);
                        //limit the CalendarView to select the max date that exists on the database
                        mRef.child("time").orderByKey().limitToLast(1).get().addOnCompleteListener(
                                task12 -> {
                                    if (task12.isSuccessful()){
                                        for (DataSnapshot date: task12.getResult().getChildren()){
                                            date_end = date.getKey();
                                        }
                                        try {
                                            cal.setMaxDate(simpleDateFormat.parse(date_end).getTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        );
                        //limit the CalendarView to select the min date that exists on the database
                        mRef.child("time").orderByKey().limitToFirst(1).get().addOnCompleteListener(
                                task1 -> {
                                    if (task1.isSuccessful()){
                                        for (DataSnapshot date: task1.getResult().getChildren()){
                                            date_begin = date.getKey();
                                        }
                                        try {
                                            long date = simpleDateFormat.parse(date_begin).getTime();
                                            long today = (new Date()).getTime();
                                            if(today>date){
                                                date = today;
                                            }
                                            cal.setMinDate(date);
                                            cal.setDate(date);

                                            //set default selected date while onCreate
                                            date_selected = date_begin;
                                            mRef.child("time").child(date_selected).
                                                    addValueEventListener(availabilityTracker);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        );
                    }
                }else{
                    Log.i("RegistrationTimeTicket",task.getException().getMessage());
                }
            }
        });

        //Initialize ListView
        AvailabilityTime = new ArrayList<>();
        AvailabilitySlot = new HashMap<>();
        adapter = new TimeSlotArrayAdapter(this,AvailabilityTime,AvailabilitySlot);
        ((ListView)findViewById(R.id.listViewTime)).setAdapter(adapter);

        //define CalendarView behavior
        CalendarView calendar = findViewById(R.id.calendarRegister);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                try {
                    //if Database reference is not ready, return;
                    if(Objects.equals(mRef.getKey(), "location")){
                        return;
                    }
                    //if a date was previously selected, remove the EventListener on it
                    if (date_selected!=null){
                        mRef.child("time").child(date_selected).removeEventListener(availabilityTracker);
                    }
                    //Format the date into String & add the EventListener to the Date
                    Date date = simpleDateFormat.parse(String.format("%d-%d-%d",year,month+1,dayOfMonth));
                    date_selected = simpleDateFormat.format(date);
                    mRef.child("time").child(date_selected).addValueEventListener(availabilityTracker);
                    adapter.checkedPosition = 0;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPause(){
        if (date_selected!=null){
            mRef.child("time").child(date_selected).removeEventListener(availabilityTracker);
        }
        super.onPause();
    }

    public void btnNextToConfirmationOnClick(View view){
        if (location_selected != null
                && date_selected != null
                && !Objects.equals(mRef.getKey(), "location")){
            Intent mIntent = new Intent(this,RegistrationConfirmation.class);
            mIntent.putExtra("DatabaseRef", mRef.getKey());
            mIntent.putExtra("location", location_selected);
            mIntent.putExtra("address",address_selected);
            mIntent.putExtra("date", date_selected);
            mIntent.putExtra("time", AvailabilityTime.get(adapter.checkedPosition));
            startActivity(mIntent);
        }else{
            Toast.makeText(
                    this,"Please select a date and time before proceeding!",Toast.LENGTH_LONG).show();
        }
    }
}