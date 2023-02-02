package com.unipi.adarmis.smartalert;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
    private String category = "Earthquake";
    private boolean display = true;
    private List<IncidentGroup> groups;

    private Map<String,Integer> timeMap = Map.of("Earthquake",12,"Typhoon",5,"Flood",12,"Fire",7,"Tsunami",2);
    private Map<String,Integer> radiusMap = Map.of("Earthquake",20000,"Typhoon",10000,"Flood",8000,"Fire",10000,"Tsunami",20000);
    private Map<String,TimeUnit> unitMap = Map.of("Earthquake",TimeUnit.HOURS,"Typhoon",TimeUnit.HOURS,"Flood",TimeUnit.HOURS,"Fire",TimeUnit.DAYS,"Tsunami",TimeUnit.HOURS);

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
        Log.d("Event","Item select");

        db.collection("incidents")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                        } else {
                            Log.d("Event",value.getQuery().toString());
                            List<IncidentPoint> points = new ArrayList<>();
                            int index = 0;
                            for(QueryDocumentSnapshot d : value) {
                                if(d.getString("type").equals(category)) {
                                    Date docDate = d.getTimestamp("timestamp").toDate();
                                    Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                    if(getDateDiff(docDate,today,unitMap.get(category))<timeMap.get(category)) {
                                        String id = d.getId();
                                        String comment = d.getString("comment");
                                        String incType = d.getString("type");
                                        Double longitude = d.getDouble("longitude");
                                        Double latitude = d.getDouble("latitude");
                                        Location loc = new Location("location");
                                        loc.setLongitude(longitude);
                                        loc.setLatitude(latitude);
                                        //locations.add(loc);
                                        IncidentPoint point = new IncidentPoint(incType,loc,index,comment,id,docDate);
                                        points.add(point);
                                        index++;

                                    }
                                }
                            }
                            Toast.makeText(Incidents.this,"New incident uploaded, retrieving changes...",Toast.LENGTH_LONG).show();
                            clearTable();
                            groups = Ranking.rank(points,radiusMap.get(category));
                            try {
                                fillTable(groups);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });

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
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            Log.d("Event","Completed query");
                            //List<Location> locations = new ArrayList<>();
                            List<IncidentPoint> points = new ArrayList<>();
                            int index = 0;
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                Date docDate = document.getTimestamp("timestamp").toDate();
                                Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                if(getDateDiff(docDate,today,unitMap.get(category))<timeMap.get(category)) {
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
                            //Log.d("INCIDENTS",String.valueOf(points.size()));
                            groups = Ranking.rank(points,radiusMap.get(category));
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
                            Bundle extras = new Bundle();
                            extras.putParcelable("group",g);
                            Intent intent = new Intent(Incidents.this,IncidentDetails.class);
                            intent.putExtras(extras);
                            startActivity(intent);
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
        Log.d("Event","item select");
        if(!display) {
            //groups = new ArrayList();
            clearTable();
            computeCenters();
        }
        display = false;
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