package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    Button loginButton;
    LocationManager locationManager;
    private RequestQueue mRequestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());


        //set location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //request permissions
        requestGPSPermission();
    }
    @Override
    public void onClick(View view)
    {
        //login function
        TextView name = findViewById(R.id.username);
        TextView pass = findViewById(R.id.password);
        String username = name.getText().toString();
        String password = pass.getText().toString();
        if(username.equals(""))
        {
            Toast.makeText(this,"Username is required!",Toast.LENGTH_SHORT).show();
        }
        else if(password.equals(""))
        {
            Toast.makeText(this,"Password is required!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            String url = "http://192.168.2.2:8080/api/users/login";
            JsonObjectRequest loginRequest = createLoginRequest(url,username,password);
            mRequestQueue.add(loginRequest);
        }

    }

    public void requestGPSPermission()
    {
        //request gps permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    private JsonObjectRequest createLoginRequest(String url, String username, String password) {
        JSONObject login_data = new JSONObject();
        try {
            login_data.put("username",username);
            login_data.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                login_data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("Rest response:",response.toString());

                            if(response.getInt("statusCode")==200) {
                                JSONObject user = response.getJSONObject("data").getJSONObject("user");
                                String role = user.getString("role");
                                if (role.equals("USER")) {
                                    Toast.makeText(getApplicationContext(), "Επιτυχής σύνδεση!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(MainActivity.this,UserPage.class);
                                    startActivity(intent);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Λάθος στοιχεία!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Internal server error. Please try again.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Internal server error. Please try again.", Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }
        );
        return jsonObjectRequest;
    }

}