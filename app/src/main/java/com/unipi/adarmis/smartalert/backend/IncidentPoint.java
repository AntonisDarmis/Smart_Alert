package com.unipi.adarmis.smartalert.backend;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IncidentPoint implements Parcelable
{
    int point;
    List<Integer> neighbours;
    Location location;
    String comment;
    String type;
    String id;

    Date date;

    public IncidentPoint(String type,Location location,int point,String comment, String id, Date date)
    {
        this.type = type;
        this.location = location;
        this.point = point;
        this.comment = comment;
        this.id = id;
        this.date = date;
    }

    public void setNeighbours(List<Integer> neighbours) {
        this.neighbours = neighbours;
    }

    public Location getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public List<Integer> getNeighbours(){return neighbours;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(point);
        dest.writeList(neighbours);
        dest.writeString(comment);
        dest.writeString(type);
        dest.writeString(id);
        dest.writeString(date.toString());
        dest.writeDouble(location.getLongitude());
        dest.writeDouble(location.getLatitude());
    }

    public IncidentPoint(Parcel p) throws ParseException {
        this.point = p.readInt();
        this.neighbours = new ArrayList<>();
        p.readList(neighbours,Integer.class.getClassLoader());
        //this.neighbours = p.readArrayList(null);
        this.comment = p.readString();
        this.type = p.readString();
        this.id = p.readString();
        this.date = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy").parse(p.readString());
        Double longitude = p.readDouble();
        Double latitude = p.readDouble();
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        this.location=loc;

    }

    public static final Parcelable.Creator<IncidentPoint> CREATOR = new Parcelable.Creator<IncidentPoint>() {
        public IncidentPoint createFromParcel(Parcel in) {
            try {
                return new IncidentPoint(in);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public IncidentPoint[] newArray(int size) {
            return new IncidentPoint[size];
        }
    };
}
