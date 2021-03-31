package com.cnit355.cnit425_blackboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class SignUpActivity extends AppCompatActivity {
    CalendarView signUpCalendarView;
    Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_page);

        signUpCalendarView = findViewById(R.id.signUpCalendarView);
        signUpCalendarView.setClickable(true);

        Date date = new Date();
        signUpCalendarView.setDate(date.getTime());






    }
}
