package com.unipi.adarmis.smartalert;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SubmitIncident extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, LocationListener {
    Button submitButton, selectImage;
    private String category;

    private TextView imagePath;

    private TextView longitude;
    private TextView latitude;
    private EditText comment;

    private ProgressBar mProgressBar;
    private Uri selectedImage = null;

    LocationManager locationManager;
    FirebaseFirestore db;
    StorageReference storageReference;

    byte[] imgByteArray = null;


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

        setContentView(R.layout.activity_submit_incident);
        imagePath = findViewById(R.id.imgPath);
        submitButton = findViewById(R.id.submitIncident);
        selectImage = findViewById(R.id.selectImage);
        submitButton.setOnClickListener(this);
        selectImage.setOnClickListener(this);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        comment = findViewById(R.id.comment);
        mProgressBar = findViewById(R.id.progressBar);
        Spinner spinner = findViewById(R.id.incidentType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.types,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("images");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.selectImage)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,3);

        }
        else if(view.getId() == R.id.submitIncident)
        {
            Float x = Float.parseFloat(longitude.getText().toString());
            Float y = Float.parseFloat(latitude.getText().toString());
            String comm = comment.getText().toString();
            //category

            Map<String,Object> incident = new HashMap<>();
            incident.put("timestamp",LocalDateTime.now());
            incident.put("longitude",x);
            incident.put("latitude",y);
            incident.put("type",category);
            incident.put("comment",comm);
            //incident.put("image",null);

            db.collection("incidents").add(incident)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Incident successfully written!");
                            Toast.makeText(SubmitIncident.this, "Incident successfully reported",
                                    Toast.LENGTH_SHORT).show();
                            uploadFile(documentReference.getId().toString());
                            Intent intent = new Intent(SubmitIncident.this,UserPage.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                            Toast.makeText(SubmitIncident.this, "An error occurred, please retry.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadFile(String docId) {
        if(selectedImage!=null) {
            StorageReference fileReference = storageReference.child(docId+"."+getFileExtension(selectedImage));
            fileReference.putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(SubmitIncident.this,"Image uploaded.",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SubmitIncident.this,"Image upload failed.",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            mProgressBar.setProgress((int)progress);
                        }
                    });
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        longitude.setText(Double.toString(location.getLongitude()));
        latitude.setText(Double.toString(location.getLatitude()));
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK && data != null)
        {
            selectedImage = data.getData();
            imagePath.setText(selectedImage.getLastPathSegment());
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            imgByteArray =  getBitmapAsByteArray(bitmap);
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        category = "Earthquake";
    }


}

