package com.unipi.adarmis.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.unipi.adarmis.smartalert.backend.IncidentGroup;

public class IncidentDetails extends AppCompatActivity {

    private TextView detailsType, detailsNumber, detailsLocation, detailsDate;
    private Button viewImagesButton;
    IncidentGroup group;

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

        viewImagesButton = findViewById(R.id.buttonViewImages);
        viewImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGroupImages();
            }
        });
    }

    private void openGroupImages() {
        Intent intent = new Intent(IncidentDetails.this,GroupImages.class);
        Bundle extras = new Bundle();
        extras.putParcelable("group",group);
        intent.putExtras(extras);
        startActivity(intent);
    }

}