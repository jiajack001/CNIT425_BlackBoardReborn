package com.cnit425.cnit425_blackboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getInstance
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        SharedPreferences pref = getSharedPreferences("PREF", Context.MODE_PRIVATE);
        ((TextView)findViewById(R.id.txtEmail)).setText(pref.getString("id",""));
        ((TextView)findViewById(R.id.txtPassword)).setText(pref.getString("pw",""));
    }

    //when the app opens, auto-login
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
    }

    //Sign up a new user on database
    public void btnSignUpOnClick(View view){
        String email = ((TextView)findViewById(R.id.txtEmail)).getText().toString();
        String password = ((TextView)findViewById(R.id.txtPassword)).getText().toString();
        //input check
        if(inputCheckNull(email,password)){
            Toast.makeText(this,"id or password can't be null",Toast.LENGTH_SHORT).show();
            return;
        }
        //sign up with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Signed up success, update UI with the signed-up user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            new Thread(() -> {
                                DatabaseReference mDataRef = database.getReference("user");
                                String uid = user.getUid();
                                mDataRef.child(uid).child("Email").setValue(email);
                                mDataRef.child(uid).child("Vaccination").child("Vaccinated").setValue(false);
                                mDataRef.child(uid).child("Vaccination").child("VaccineCount").setValue(0);
                            }).start();
                            saveCredential(email,password);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this,
                                    Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    //Sign-In with current credentials on database
    public void btnSignInOnClick(View view){
        String email = ((TextView)findViewById(R.id.txtEmail)).getText().toString();
        String password = ((TextView)findViewById(R.id.txtPassword)).getText().toString();
        //input check
        if(inputCheckNull(email,password)){
            Toast.makeText(this,"id or password can't be null",Toast.LENGTH_SHORT).show();
            return;
        }

        //sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveCredential(email,password);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this,
                                    Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }

    //store id and pw into shared_pref
    public void saveCredential(String id, String pw){
        SharedPreferences pref = getSharedPreferences("PREF",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("id",id);
        edit.putString("pw",pw);
        edit.apply();
    }

    //input check func, return true if id or pw is null/empty
    public boolean inputCheckNull(String id, String pw){
        return (id == null || id.equals("") || pw == null || pw.equals(""));
    }

    //navigate to Menu if the user is not null (has login)
    private void updateUI(FirebaseUser u){
        if (u != null){
            Intent mIntent = new Intent(this, Menu.class);
            startActivity(mIntent);
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing when back button is pressed
    }
}