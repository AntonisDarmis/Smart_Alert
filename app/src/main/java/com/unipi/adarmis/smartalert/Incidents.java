package com.unipi.adarmis.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class Incidents extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);
    }

    @Override
    public void onBackPressed() {   //REMOVE THIS LATER
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Incidents.this,MainActivity.class);
        startActivity(intent);
    }
}