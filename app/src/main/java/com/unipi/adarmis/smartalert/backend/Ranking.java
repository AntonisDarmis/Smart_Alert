package com.unipi.adarmis.smartalert.backend;

import static android.content.ContentValues.TAG;

import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Ranking {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<IncidentGroup> rank(List<IncidentPoint> points, int radius)
    {
        if(points.isEmpty())
        {
            return new ArrayList<>();
        }

        //compute all pairwise distances
        int size = points.size();
        double [][] distances = new double[size][size];
        for(int i=0;i<size;i++)
        {
            for(int j=0;j<size;j++)
            {
                //compute the distance in meters
                distances[i][j] = points.get(i).getLocation().distanceTo(points.get(j).getLocation());
            }
        }
        //radius in meters to search closest incidents

        //int[] scores = new int[size];
        //List<IncidentPoint> incidentPoints = new ArrayList<IncidentPoint>();
        for(int i=0;i<size;i++)
        {
            List<Integer> neighbours = new ArrayList<>();
            //find incident
            for(int j=0;j<size;j++)
            {
                if(distances[i][j]<=radius)
                {
                    //save neighbours of incident(i) if they belong in the specified radius
                    neighbours.add(j);
                }
            }
            //update point's neighbours
            points.get(i).setNeighbours(neighbours);
        }

        //keep largest groups in greedy manner
        //List<IncidentPoint> groups = new ArrayList<>();
        List<IncidentGroup> groups = new ArrayList<>();
        while(!NoMorePoints(points))    //while unassigned points exist
        {
            //get point with the most neighbours
            IncidentPoint currMax = Collections.max(points, Comparator.comparing(c -> c.getNeighbours().size()));

            //add these neighbours to a group (neighbours include the point itself
            List<IncidentPoint> groupPoints = new ArrayList<>();
            for (Integer i : currMax.getNeighbours()) {
                groupPoints.add(points.get(i));
            }
            groups.add(new IncidentGroup(groupPoints)); //add group to list of groups

            //remove point with most neighbours from list of points
            points.remove(currMax);
            for (IncidentPoint point : points) {    //for each remaining point
                for (Integer i : currMax.getNeighbours()) { //remove neighbours that have already been assigned a group
                    point.getNeighbours().remove(i);
                }
            }
        }

        //print all centers
        for(IncidentGroup g : groups) {
            String cntr = "("+String.valueOf(g.getCenter().getLongitude())+", "+String.valueOf(g.getCenter().getLatitude())+")";
            Log.d("Center",cntr);
        }

        return groups;
    }




    public static boolean NoMorePoints(List<IncidentPoint> points)
    {
        for(IncidentPoint point: points)
        {
            if(point.getNeighbours().size() > 0 )
            {
                return false;
            }
        }
        return true;
    }

    public static List<Location> computeCenters(List<IncidentPoint> groups,List<Location> locations)
    {
        List<Location> centers = new ArrayList<>();
        for(IncidentPoint group:groups)
        {
            float longitude = 0;
            float latitude = 0;
            for(Integer i:group.getNeighbours())
            {
                longitude += locations.get(i).getLongitude();
                latitude += locations.get(i).getLatitude();
            }
            float centerLongitude = longitude / group.getNeighbours().size();
            float centerLatitude = latitude / group.getNeighbours().size();
            Location location = new Location("");
            location.setLongitude(centerLongitude);
            location.setLatitude(centerLatitude);
            centers.add(location);
        }
        return centers;
    }
}
