package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
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

    private static int MY_FINE_LOCATION_REQUEST = 99;
    private static int MY_BACKGROUND_LOCATION_REQUEST = 100;

    LocationService mLocationService = new LocationService();
    Intent mServiceIntent;


    @Override
    public void onStart()
    {
        super.onStart();
        //addLocationListener();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_page);

        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestGPSPermission();
        } */

        permissionsTricky();
        starServiceFunc();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);

        signOutButton = findViewById(R.id.logOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopServiceFunc();
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
        Log.d("Location Changed","Location has changed");
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

    public void permissionsTricky() {
        if (ActivityCompat.checkSelfPermission(UserPage.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (ActivityCompat.checkSelfPermission(UserPage.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {


                    AlertDialog alertDialog = new AlertDialog.Builder(UserPage.this).create();
                    alertDialog.setTitle("Background permission");
                    alertDialog.setMessage("Permission dialog");

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Start service anyway",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    starServiceFunc();
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Grant background Permission",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    requestBackgroundLocationPermission();
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();


                }else if (ActivityCompat.checkSelfPermission(UserPage.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    starServiceFunc();
                }
            }else{
                starServiceFunc();
            }

        }else if (ActivityCompat.checkSelfPermission(UserPage.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(UserPage.this, Manifest.permission.ACCESS_FINE_LOCATION)) {


                AlertDialog alertDialog = new AlertDialog.Builder(UserPage.this).create();
                alertDialog.setTitle("ACCESS_FINE_LOCATION");
                alertDialog.setMessage("Location permission required");

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                requestFineLocationPermission();
                                dialog.dismiss();
                            }
                        });


                alertDialog.show();

            } else {
                requestFineLocationPermission();
            }
        }

    }

    private void starServiceFunc(){
        mLocationService = new LocationService();
        mServiceIntent = new Intent(this, mLocationService.getClass());
        if (!Util.isMyServiceRunning(mLocationService.getClass(), this)) {
            startService(mServiceIntent);
            Toast.makeText(this, "Started service", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service already running", Toast.LENGTH_SHORT).show();
        }
    }


    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                MY_BACKGROUND_LOCATION_REQUEST);
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, MY_FINE_LOCATION_REQUEST);
    }

    private void stopServiceFunc(){
        mLocationService = new LocationService();
        mServiceIntent = new Intent(this, mLocationService.getClass());
        if (Util.isMyServiceRunning(mLocationService.getClass(), this)) {
            stopService(mServiceIntent);
            Toast.makeText(this, "Service stopped!!", Toast.LENGTH_SHORT).show();
            //saveLocation(); // explore it by your self
        } else {
            Toast.makeText(this, "Service is already stopped!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void requestGPSPermission() {
        //request gps permission

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Toast.makeText(this, Integer.toString(requestCode), Toast.LENGTH_LONG).show();

        if ( requestCode == MY_FINE_LOCATION_REQUEST){

            if (grantResults.length !=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission();
                }

            } else {
                Toast.makeText(this, "ACCESS_FINE_LOCATION permission denied", Toast.LENGTH_LONG).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                 /*   startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", this.getPackageName(), null),),);*/

                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:com.unipi.adarmis.smartalert")
                    ));

                }
            }
            return;

        }else if (requestCode == MY_BACKGROUND_LOCATION_REQUEST){

            if (grantResults.length!=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Background location Permission Granted", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Background location permission denied", Toast.LENGTH_LONG).show();
            }
            return;
        }

    }

    /*
    private void addLocationListener() {
        triggerService = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                try {

                    Looper.prepare();//Initialise the current thread as a looper.
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    Criteria c = new Criteria();
                    c.setAccuracy(Criteria.ACCURACY_COARSE);



                    if (ActivityCompat.checkSelfPermission(UserPage.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserPage.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        ActivityCompat.requestPermissions(UserPage.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
                        //final String PROVIDER = locationManager.getBestProvider(c, true);
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, UserPage.this);
                    //lm.requestLocationUpdates(PROVIDER, 600000, 0, MainActivity.this);
                    Log.d("LOC_SERVICE", "Service RUNNING!");
                    Looper.loop();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }, "LocationThread");
        triggerService.start();
    } */
}