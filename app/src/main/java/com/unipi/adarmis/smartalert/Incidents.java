package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Table;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.unipi.adarmis.smartalert.backend.IncidentGroup;
import com.unipi.adarmis.smartalert.backend.IncidentPoint;
import com.unipi.adarmis.smartalert.backend.Ranking;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Incidents extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private FirebaseFirestore db;
    private String category;

    private List<IncidentGroup> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidents);
        groups = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        Spinner spinner = findViewById(R.id.types);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.types,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {   //REMOVE THIS LATER
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Incidents.this, MainActivity.class);
        startActivity(intent);
    }




    public void computeCenters() {
        db.collection("incidents")
                .whereEqualTo("type",category)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            //List<Location> locations = new ArrayList<>();
                            List<IncidentPoint> points = new ArrayList<>();
                            int index = 0;
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                Date docDate = document.getTimestamp("timestamp").toDate();
                                Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                if(getDateDiff(docDate,today,TimeUnit.DAYS)<1) {
                                    String id = document.getId();
                                    String comment = document.getString("comment");
                                    String incType = document.getString("type");
                                    Double longitude = document.getDouble("longitude");
                                    Double latitude = document.getDouble("latitude");
                                    Location loc = new Location("location");
                                    loc.setLongitude(longitude);
                                    loc.setLatitude(latitude);
                                    //locations.add(loc);
                                    IncidentPoint point = new IncidentPoint(incType,loc,index,comment,id,docDate);
                                    points.add(point);
                                    index++;
                                }
                            }
                            groups = Ranking.rank(points,10000);
                            try {
                                fillTable(groups);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Toast.makeText(Incidents.this,"No incidents fetched.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void clearTable() {
        TableLayout table = findViewById(R.id.table);
        int childCount = table.getChildCount();
        int c = 0;
        for(int i=0;i<childCount;i++) {
            View child = table.getChildAt(i-c);
            if(child instanceof TableRow && child.getId()!=R.id.headerRow) {
                table.removeView(child);
                c+=1;
            }
        }

    }

    public void fillTable(List<IncidentGroup> groups) throws IOException {
            if(groups.size()>0) {
                TableLayout table = findViewById(R.id.table);
                for(IncidentGroup g: groups)
                {
                    TableRow tableRow = new TableRow(this);
                    tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    TextView type_tv = new TextView(this);
                    type_tv.setText(g.getType());
                    type_tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f));

                    TextView date_tv = new TextView(this);
                    date_tv.setText(g.getDateFormat());
                    date_tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f));

                    TextView loc_tv= new TextView(this);
                    loc_tv.setText(g.getCenterFormat());
                    loc_tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f));

                    TextView danger_tv = new TextView(this);
                    danger_tv.setText(String.valueOf(g.getDangerScore()));
                    danger_tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1.0f));

                    tableRow.addView(type_tv);
                    tableRow.addView(date_tv);
                    tableRow.addView(loc_tv);
                    tableRow.addView(danger_tv);

                    tableRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //MAKE INCIDENTGROUP PARCELABLE
                            //Intent intent = new Intent(Incidents.this,IncidentDetails.class);
                            //intent.putExtras("group",g);
                        }
                    });
                    table.addView(tableRow);
                    //also set on click listener...
                }
            }

        else
        {
            Toast.makeText(Incidents.this,"No incidents available for selected category.",Toast.LENGTH_SHORT).show();
        }
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //category = parent.getItemAtPosition(position).toString();
        category = parent.getSelectedItem().toString();
        //compute centers for selected category
        clearTable();
        computeCenters();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        category = "Earthquake";
    }

    public String getGreaterArea(IncidentGroup grp) throws IOException {
        Location center = grp.getCenter();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(center.getLatitude(),center.getLongitude(),1);
        return addresses.get(0).getAddressLine(0);
    }

}