package com.cnit355.cnit425_blackboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class RegistrationTimeTicket extends AppCompatActivity {
    private final static String[] timeSlot = new String[]{
            "8:00-9:00","9:00-10:00","10:00-11:00","11:00-12:00","12:00-13:00",
            "13:00-14:00","14:00-15:00","15:00-16:00","16:00-17:00"};
    private String dateSelected;

    private DatabaseReference mRef;

    private ValueEventListener availabilityTracker = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            String post = (String) dataSnapshot.getValue();
            // ..
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        dateSelected = "2021/05/01";

        /*
        mRef = FirebaseDatabase.getInstance().getReference("DateTime");
        mRef.addValueEventListener(postListener);



        for (String str : timeSlot){
            mRef.child("2021/05/01").child(userId).get();
        }

         */
        CalendarView calendar = (CalendarView) findViewById(R.id.calendarRegister);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            calendar.setDate(simpleDateFormat.parse("2021/05/01").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }




        //TimeSlotArrayAdapter lv_adapter = new TimeSlotArrayAdapter(this, )
    }


    public void updateAvailability(){

    }
}