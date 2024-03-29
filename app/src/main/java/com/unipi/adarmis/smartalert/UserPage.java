package com.unipi.adarmis.smartalert;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;

public class UserPage extends AppCompatActivity implements View.OnClickListener, LocationListener {
    Button submitButton, signOutButton;
    LocationManager locationManager;
    Thread triggerService;
    Double longitude,latitude;

    TextView earthquakeCounter, fireCounter, typhoonCounter, floodCounter, tsunamiCounter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static int MY_FINE_LOCATION_REQUEST = 99;
    private static int MY_BACKGROUND_LOCATION_REQUEST = 100;

    LocationService mLocationService = new LocationService();
    Intent mServiceIntent;

    private String cur_uid;


    @Override
    public void onStart()
    {
        super.onStart();
        //addLocationListener();

        db.collection("statistics").document("statistics")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null) {
                            Log.w(TAG, "Statistics listen failed.", error);
                        } else {
                            if(value.exists()) {
                                Toast.makeText(UserPage.this, getApplicationContext().getString(R.string.fetched_statistics),Toast.LENGTH_SHORT).show();
                                earthquakeCounter.setText(String.valueOf(value.getDouble("Earthquake").intValue()));
                                fireCounter.setText(String.valueOf(value.getDouble("Fire").intValue()));
                                typhoonCounter.setText(String.valueOf(value.getDouble("Typhoon").intValue()));
                                floodCounter.setText(String.valueOf(value.getDouble("Flood").intValue()));
                                tsunamiCounter.setText(String.valueOf(value.getDouble("Tsunami").intValue()));
                            } else {
                                Log.d("STATISTICS","Statistics not found");
                                Toast.makeText(UserPage.this, getApplicationContext().getString(R.string.unable_to_fetch_statistics),Toast.LENGTH_SHORT).show();
                                earthquakeCounter.setText(getApplicationContext().getString(R.string.failed));
                                fireCounter.setText(getApplicationContext().getString(R.string.failed));
                                typhoonCounter.setText(getApplicationContext().getString(R.string.failed));
                                floodCounter.setText(getApplicationContext().getString(R.string.failed));
                                tsunamiCounter.setText(getApplicationContext().getString(R.string.failed));
                            }
                        }
                    }
                });

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
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

        earthquakeCounter = findViewById(R.id.earthquakeCounter);
        fireCounter = findViewById(R.id.fireCounter);
        typhoonCounter = findViewById(R.id.typhoonCounter);
        floodCounter = findViewById(R.id.floodCounter);
        tsunamiCounter = findViewById(R.id.tsunamiCounter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        cur_uid = currentUser.getUid();

        askNotificationPermission();

        permissionsTricky();
        //starServiceFunc();

        updateToken();

        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);

        signOutButton = findViewById(R.id.logOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopServiceFunc();
                deleteToken();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserPage.this,MainActivity.class);
                startActivity(intent);
            }
        });

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(Constants.CHANNEL_ID,Constants.CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
        mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);

        mNotificationManager.createNotificationChannel(mChannel);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this,SubmitIncident.class);
        startActivity(intent);
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });


    private void deleteToken() {
        DocumentReference docRef = db.collection("users").document(cur_uid);
        docRef.update("token","x")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d("TOKENUPDATE","Deleted user token.");
                        } else {
                            Log.d("TOKENUPDATE","Failed to delete user token.");
                        }
                    }
                });
    }

    private void updateToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()) {
                            String device_token = task.getResult();
                            DocumentReference docRef = db.collection("users").document(cur_uid);
                            docRef.update("token",device_token)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Log.d("TOKENUPDATE","Updated user token.");
                                            } else {
                                                Log.d("TOKENUPDATE","Token update failed.");
                                                //Toast.makeText(UserPage.this,"User info update failed. Notifications may not work! Please try logging in and out again or check your internet connection!",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Log.d("TOKENUDPATE","Failed to get token");
                            //Toast.makeText(UserPage.this,"User info update failed. Notifications may not work! Please try logging in and out again or check your internet connection!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        longitude = location.getLongitude();
        latitude = location.getLatitude();

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
                        Toast.makeText(UserPage.this,getApplicationContext().getString(R.string.failed),Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(UserPage.this,getApplicationContext().getString(R.string.failed),Toast.LENGTH_SHORT).show();
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
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,UserPage.this,null);
                    starServiceFunc();
                }
            }else{
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,UserPage.this,null);
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
        mServiceIntent = new Intent(UserPage.this, mLocationService.getClass());
        mServiceIntent.putExtra("uid",cur_uid);
        if (!Util.isMyServiceRunning(mLocationService.getClass(), this)) {
            startService(mServiceIntent);
            Toast.makeText(this, getApplicationContext().getString(R.string.started_location_service), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getApplicationContext().getString(R.string.service_already_running), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getApplicationContext().getString(R.string.service_stopped), Toast.LENGTH_SHORT).show();
            //saveLocation(); // explore it by your self
        } else {
            Toast.makeText(this, getApplicationContext().getString(R.string.service_already_stopped), Toast.LENGTH_SHORT).show();
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

        //Toast.makeText(this, Integer.toString(requestCode), Toast.LENGTH_LONG).show();

        if ( requestCode == MY_FINE_LOCATION_REQUEST){

            if (grantResults.length !=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission();
                }

            } else {
                Toast.makeText(this, getApplicationContext().getString(R.string.fine_location_denied), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, getApplicationContext().getString(R.string.background_location_permission_granted), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getApplicationContext().getString(R.string.background_location_permission_denied), Toast.LENGTH_LONG).show();
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