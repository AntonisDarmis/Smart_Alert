package com.unipi.adarmis.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SubmitIncident extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    Button submitButton,selectImage;
    private String category;

    private TextView imagePath;

    private TextView longitude;
    private TextView latitude;
    private EditText comment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.selectImage)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,3);

        }
        else
        {
            String longit = longitude.getText().toString();
            String lat = latitude.getText().toString();
            String comm = comment.getText().toString();
            String imageResult = imagePath.getText().toString(); // make into blob and save to db??????????
            Intent intent = new Intent(this,UserPage.class);
            startActivity(intent);


        }
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
}