package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unipi.adarmis.smartalert.backend.IncidentGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class IncidentDetails extends AppCompatActivity {

    private TextView detailsType, detailsNumber, detailsLocation, detailsDate;
    private Button viewImagesButton, notifyUsersButton;
    RequestQueue mRequestQueue;
    private FirebaseFirestore db;
    IncidentGroup group;

    //BAD IDEA TO PUSH THIS TO GITHUB
    private String API_KEY = "AAAAFfz7fBg:APA91bHaP_UYoWvEPMpBIVLHDIuPD57fI9TNNCtdthixb8qlhgmRXt1VwsDUzefj7JsiUC3Oedr_ECWo-ovLN5DDo6BuAnmMpnkNXfuM3Hb2UUUfTy0c8GH5XimIy9Kb1t3c6Fhek4tE";
    private String url = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_details);

        Bundle extras = getIntent().getExtras();
        group = (IncidentGroup) extras.get("group");
        detailsType = findViewById(R.id.detailsTypeTextview);
        detailsType.setText(group.getType());
        detailsNumber = findViewById(R.id.detailsNumberTextview);
        detailsNumber.setText(String.valueOf(group.getNumberOfReports()));
        detailsLocation = findViewById(R.id.detailsLocationTextview);
        detailsLocation.setText(group.getCenterFormat());
        detailsDate = findViewById(R.id.detailsDateTextview);
        detailsDate.setText(group.getDateFormat());

        db = FirebaseFirestore.getInstance();

        viewImagesButton = findViewById(R.id.buttonViewImages);
        viewImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGroupImages();
            }
        });


        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        notifyUsersButton = findViewById(R.id.notifyButton);
        notifyUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAndNotifyUsers();
                //notifyUsers("https://fcm.googleapis.com/fcm/send");
            }
        });
    }

    private void searchAndNotifyUsers() {
        db.collection("users")
                .whereEqualTo("role","USER")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                Double longitude = documentSnapshot.getDouble("longitude");
                                Double latitude = documentSnapshot.getDouble("latitude");
                                Location userLoc = new Location("");
                                userLoc.setLongitude(longitude);
                                userLoc.setLatitude(latitude);

                                Location centerLoc = group.getCenter();

                                double distance = centerLoc.distanceTo(userLoc);
                                if(distance <= 10000) {
                                    String token = documentSnapshot.getString("token");
                                    mRequestQueue.add(notifyUsers(url,token));
                                }
                            }
                        } else {
                            Toast.makeText(IncidentDetails.this,"Sending notification failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private StringRequest notifyUsers(String url, String targetToken) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Handle response from the server
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "key=" + API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("to", targetToken);

                    JSONObject data = new JSONObject();
                    data.put("title", "Smart Alert: ");
                    data.put("content", "content");

                    jsonObject.put("data", data);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return jsonObject.toString().getBytes();
            }
        };

        return request;
    }

    private void openGroupImages() {
        Intent intent = new Intent(IncidentDetails.this,GroupImages.class);
        Bundle extras = new Bundle();
        extras.putParcelable("group",group);
        intent.putExtras(extras);
        startActivity(intent);
    }

}