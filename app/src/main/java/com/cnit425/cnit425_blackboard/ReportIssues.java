package com.cnit425.cnit425_blackboard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class ReportIssues extends AppCompatActivity {

    private DatabaseReference mReportRef;
    private String uid;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issues);

        //retrieve uid and email
        mReportRef = FirebaseDatabase.getInstance().getReference("issue");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    public void btnReportNowOnClick(View view){
        //if uid or email is null, do nothing
        if(uid == null || email == null){
            Toast.makeText(this,
                    "Email is invalid, please login and try again!",Toast.LENGTH_LONG).show();
            return;
        }
        //get Title and Message from TextView
        TextView txtTitle = findViewById(R.id.txtReportTitle);
        TextView txtMsg = findViewById(R.id.txtReportMessage);
        String title = txtTitle.getText().toString();
        String msg = txtMsg.getText().toString();
        long timelog = (new Date()).getTime();

        //create a new uid for report
        String report_id = email + timelog;

        //push data into database
        mReportRef.child(report_id).child("email").setValue(email);
        mReportRef.child(report_id).child("title").setValue(title);
        mReportRef.child(report_id).child("msg").setValue(msg);
        mReportRef.child(report_id).child("uid").setValue(uid);
    }
}