package com.unipi.adarmis.smartalert;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    Button loginButton;
    LocationManager lm;
    TextView register;
    Thread triggerService;
    Double longitude,latitude;
    private String token = null;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    @Override
    public void onStart() {
        super.onStart();


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        //Log.d(TAG, msg);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                    }
                });


        db = FirebaseFirestore.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && gps_enabled && network_enabled ) {
            Log.d("Network_enabled","True");
            Log.d("GPS_enabled","True");
            String cur_uid = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(cur_uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        Log.d("GPS STATE ON COMPLETE",String.valueOf(gps_enabled));
                        DocumentSnapshot document = task.getResult();
                        if (document.exists())
                        {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            if (Objects.requireNonNull(document.get("role")).toString().equals("ADMIN")) {
                                if(network_enabled)
                                {
                                    Intent intent = new Intent(MainActivity.this, Incidents.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, getApplicationContext().getString(R.string.check_internet_connection),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                            else if (Objects.requireNonNull(document.get("role")).toString().equals("USER")) {
                                if(network_enabled && gps_enabled) {
                                    Intent intent = new Intent(MainActivity.this, UserPage.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, getApplicationContext().getString(R.string.check_internet_and_gps),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });



            /*
            DocumentReference docRef = db.collection("users").document(cur_uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            if (Objects.requireNonNull(document.get("role")).toString().equals("ADMIN")) {
                                Intent intent = new Intent(MainActivity.this, Incidents.class);
                                startActivity(intent);
                                finish();
                            } else {
                                if(document.get("token").equals(token)) {
                                    Intent intent = new Intent(MainActivity.this, UserPage.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this,"You are already logged in in another device!",Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            }); */

        }
        else
        {
         //   Toast.makeText(MainActivity.this,"Please check GPS and internet connection!",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        register = findViewById(R.id.openRegister);
         lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        Log.d("GPS STATE ON CREATE",String.valueOf(gps_enabled));
        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
                finish();
            }
        });

        //set location manager
       // locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //request permissions
       // requestGPSPermission();
       // addLocationListener();
    }

    @Override
    public void onClick(View view) {
        //login function
        TextView name = findViewById(R.id.username);
        TextView pass = findViewById(R.id.password);
        String username = name.getText().toString();
        String password = pass.getText().toString();
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if (username.equals("")) {
            Toast.makeText(this, getApplicationContext().getString(R.string.username_is_required), Toast.LENGTH_SHORT).show();
        } else if (password.equals("")) {
            Toast.makeText(this, getApplicationContext().getString(R.string.password_is_required), Toast.LENGTH_SHORT).show();
        } else {
                mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                DocumentReference docRef = db.collection("users").document(user.getUid());
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful())
                                        {
                                            Log.d("GPS STATE ON COMPLETE",String.valueOf(gps_enabled));
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists())
                                            {
                                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                if (Objects.requireNonNull(document.get("role")).toString().equals("ADMIN")) {
                                                    if(network_enabled)
                                                    {
                                                        Intent intent = new Intent(MainActivity.this, Incidents.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(MainActivity.this, getApplicationContext().getString(R.string.please_check_internet),
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                                else if (Objects.requireNonNull(document.get("role")).toString().equals("USER")) {
                                                    if(network_enabled && gps_enabled) {
                                                        Intent intent = new Intent(MainActivity.this, UserPage.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(MainActivity.this, getApplicationContext().getString(R.string.check_internet_and_gps),
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }
                                            else {
                                                Log.d(TAG, "No such document");
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signIn:failure", task.getException());
                                Toast.makeText(MainActivity.this, getApplicationContext().getString(R.string.authentication_failed),
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }

    }



    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        ////
    }







}