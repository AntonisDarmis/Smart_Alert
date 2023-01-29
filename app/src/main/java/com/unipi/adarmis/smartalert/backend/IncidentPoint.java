package com.unipi.adarmis.smartalert.backend;

import android.location.Location;

import java.util.Date;
import java.util.List;

public class IncidentPoint
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

}
