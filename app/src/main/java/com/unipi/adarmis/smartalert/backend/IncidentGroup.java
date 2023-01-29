package com.unipi.adarmis.smartalert.backend;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentGroup implements Parcelable {

    List<IncidentPoint> incidents;
    String type;
    Integer dangerScore;

    Date earliestDate;

    Location center;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public IncidentGroup(List<IncidentPoint> incidents) {
        this.incidents = incidents;
        this.type = incidents.get(0).getType();
        this.dangerScore = computeDangerScore();
        this.center = computeCenter();
        this.earliestDate = findEarliestDate();
    }

    public String getType() {
        return type;
    }

    public Integer getDangerScore() {
        return dangerScore;
    }

    public void addPoint(IncidentPoint point) {
        incidents.add(point);
        center = computeCenter();
    }

    public Date getEarliestDate() {
        return earliestDate;
    }

    public Integer getNumberOfReports() {
        return incidents.size();
    }

    public String getCenterFormat() {
        return "("+String.valueOf(center.getLongitude()).substring(0,5)+", "+String.valueOf(center.getLatitude()).substring(0,5)+")";
    }

    public String getDateFormat() {
        return earliestDate.toString().substring(0,10);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Date findEarliestDate() {
        return Collections.min(incidents, Comparator.comparing(c->c.getDate())).getDate();
    }

    private Integer computeDangerScore() {
        Integer reports = incidents.size();
        Integer weight = 0;
        switch(type) {
            case "Fire":
                weight = 3;
                break;
            case "Earthquake":
                weight = 4;
                break;
            case "Typhoon":
                weight = 6;
                break;
            case "Flood":
                weight = 2;
                break;
            case "Tsunami":
                weight = 5;
                break;
            default:
                weight = 1;
                break;
        }
        return weight*reports*reports;
    }

    private Location computeCenter() {

        List<Location> locations = new ArrayList<>();
        for(IncidentPoint p : incidents) {
            locations.add(p.getLocation());
        }

        float longit = 0;
        float lat = 0;
        for (Location l : locations) {
            longit += l.getLongitude();
            lat += l.getLatitude();
        }
        longit = longit/ locations.size();
        lat = lat / locations.size();
        Location center = new Location("");
        center.setLongitude(longit);
        center.setLatitude(lat);
        return center;
    }

    public List<IncidentPoint> getIncidents() {
        return incidents;
    }

    public Location getCenter() {
        return center;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeInt(dangerScore);
        dest.writeString(earliestDate.toString());
        dest.writeDouble(center.getLongitude());
        dest.writeDouble(center.getLatitude());
        dest.writeParcelableList(incidents,0);
    }

    public IncidentGroup(Parcel p) throws ParseException {
        this.type = p.readString();
        this.dangerScore = p.readInt();
        this.earliestDate = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy").parse(p.readString());
        Double longitude = p.readDouble();
        Double latitude = p.readDouble();
        Location center = new Location("");
        center.setLongitude(longitude);
        center.setLatitude(latitude);
        this.center = center;
        this.incidents = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            p.readParcelableList(incidents,IncidentPoint.class.getClassLoader());
        }
    }

    public static final Parcelable.Creator<IncidentGroup> CREATOR = new Parcelable.Creator<IncidentGroup>() {
        public IncidentGroup createFromParcel(Parcel in) {
            try {
                return new IncidentGroup(in);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public IncidentGroup[] newArray(int size) {
            return new IncidentGroup[0];
        }
    };

}
