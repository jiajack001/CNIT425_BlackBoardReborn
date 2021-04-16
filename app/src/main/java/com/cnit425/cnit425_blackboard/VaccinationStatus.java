package com.cnit425.cnit425_blackboard;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class VaccinationStatus extends AppCompatActivity {

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination_status);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if(email!= null){
            createQRCode();
        }
    }

    public void createQRCode(){
        //create a JSON object to embed its String into QR code
        String jsonString = "";
        try {
            JSONObject mJSON = new JSONObject();
            mJSON.put("ProtectPurdueType","Vaccinated");
            mJSON.put("email",email);
            jsonString = mJSON.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //convert the String to QRcode
        try{
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(jsonString, BarcodeFormat.QR_CODE,400,400);
            ImageView imgView = findViewById(R.id.imgFinal);
            imgView.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}