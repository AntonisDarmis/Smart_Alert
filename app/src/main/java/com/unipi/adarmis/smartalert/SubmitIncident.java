package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;

public class SubmitIncident extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, LocationListener {
    Button submitButton, selectImage;
    private String category;

    private TextView imagePath;

    private TextView longitude;
    private TextView latitude;
    private EditText comment;

    LocationManager locationManager;
    RequestQueue mRequestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        setContentView(R.layout.activity_submit_incident);
        imagePath = findViewById(R.id.imgPath);
        submitButton = findViewById(R.id.submitIncident);
        selectImage = findViewById(R.id.selectImage);
        submitButton.setOnClickListener(this);
        selectImage.setOnClickListener(this);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        comment = findViewById(R.id.comment);
        Spinner spinner = findViewById(R.id.incidentType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.types,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.selectImage)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,3);

        }
        else
        {
            //call post incident
            String url = "http://192.168.2.2:8080/api/incidents/";

            mRequestQueue.add(postIncident(url));


            //Intent intent = new Intent(this,UserPage.class);
            //startActivity(intent);



        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.v("TAG","In location changed");
        longitude.setText(Double.toString(location.getLongitude()));
        latitude.setText(Double.toString(location.getLatitude()));
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            imagePath.setText(selectedImage.getPath());

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        category = "Earthquake";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    JsonObjectRequest postIncident(String url) { //IMAGE BLOB???????
        String longit = longitude.getText().toString();
        String lat = latitude.getText().toString();
        String comm = comment.getText().toString();
        String imageResult = imagePath.getText().toString(); // make into blob and save to db??????????

        JSONObject incident_data = new JSONObject();
        try {
            incident_data.put("date", LocalDate.now());
            incident_data.put("latitude",Double.parseDouble(lat));
            incident_data.put("longitude",Double.parseDouble(longit));
            incident_data.put("type",category.toUpperCase());
            incident_data.put("comment",comm);
            //incident_data.put(); IMAGE PATH
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return new JsonObjectRequest(
                Request.Method.POST,
                url,
                incident_data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getInt("statusCode")==201) { //http status CREATED
                                Toast.makeText(getApplicationContext(), "Successful incident submission", Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(SubmitIncident.this, UserPage.class));
                            } else {
                                //toast error msg
                                Toast.makeText(getApplicationContext(), "Error submitting incident, please try again!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error submitting incident, please try again!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error submitting incident, please try again!", Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }
        );
    }

}