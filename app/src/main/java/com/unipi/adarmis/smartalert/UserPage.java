package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class UserPage extends AppCompatActivity implements View.OnClickListener, LocationListener {
    Button submitButton, signOutButton;
    LocationManager locationManager;
    Thread triggerService;
    Double longitude,latitude;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    public void onStart()
    {
        super.onStart();

        addLocationListener();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);

        signOutButton = findViewById(R.id.logOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserPage.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this,SubmitIncident.class);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String cur_uid = currentUser.getUid();
        DocumentReference docRef = db.collection("users").document(cur_uid);
        docRef.update("longitude",longitude)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserPage.this,"Failed",Toast.LENGTH_SHORT).show();
                    }
                });

        docRef.update("latitude",latitude)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserPage.this,"Failed",Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addLocationListener() {
        triggerService = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                try {
                    Looper.prepare();//Initialise the current thread as a looper.

                    Criteria c = new Criteria();
                    c.setAccuracy(Criteria.ACCURACY_COARSE);



                    if (ActivityCompat.checkSelfPermission(UserPage.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserPage.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        ActivityCompat.requestPermissions(UserPage.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 0, UserPage.this);
                        final String PROVIDER = locationManager.getBestProvider(c, true);
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    //lm.requestLocationUpdates(PROVIDER, 600000, 0, MainActivity.this);
                    Log.d("LOC_SERVICE", "Service RUNNING!");
                    Looper.loop();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }, "LocationThread");
        triggerService.start();
    }
}