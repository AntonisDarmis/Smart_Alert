package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Incidents extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private FirebaseFirestore db;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);
        db = FirebaseFirestore.getInstance();
        Spinner spinner = findViewById(R.id.types);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.types,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        computeScores2();
    }

    @Override
    public void onBackPressed() {   //REMOVE THIS LATER
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Incidents.this, MainActivity.class);
        startActivity(intent);
    }






    public void computeScores() {
        //function to compute scores based on hierarchical search
        switch(category)
        {
            case "Fire":
            {
                db.collection("incidents")
                        .whereEqualTo("type",category)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    for(QueryDocumentSnapshot document:task.getResult())
                                    {

                                        if(true){
                                            Double longitude = document.getDouble("longitude");
                                            Double latitude = document.getDouble("latitude");


                                        }
                                    }
                                }
                            }
                        });
                break;
            }
        }

    }

    public void computeScores2() {
        db.collection("incidents")
                .whereEqualTo("type","Fire")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                Date docDate = document.getTimestamp("timestamp").toDate();
                                Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                if(getDateDiff(docDate,today,TimeUnit.DAYS)<1) {
                                    //Double longitude = document.getDouble("longitude");
                                    //Double latitude = document.getDouble("latitude");
                                    //String comment = document.getString("comment");
                                    //Toast.makeText(Incidents.this, comment, Toast.LENGTH_SHORT).show();
                                }
                                //TO SORT BY DATE USE GET DATE NOT GET DAY (THIS IS MONDAY,TUESDAY etc)
                                Toast.makeText(Incidents.this,String.valueOf(docDate.getDate()),Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(Incidents.this,"No incidents fetched.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
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