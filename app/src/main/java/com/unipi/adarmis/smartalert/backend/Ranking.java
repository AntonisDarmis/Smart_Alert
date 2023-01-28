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
    public static List<Location> ranking(List<Location> locations, int radius)
    {
        if(locations.isEmpty())
        {
            return new ArrayList<>();
        }
        int size = locations.size();
        double [][] distances = new double[size][size];
        for(int i=0;i<size;i++)
        {
            for(int j=0;j<size;j++)
            {
                //compute the distance in meters
                distances[i][j] = locations.get(i).distanceTo(locations.get(j));
            }
        }
        //radius in meters to search closest incidents

        int[] scores = new int[size];
        List<IncidentPoint> incidentPoints = new ArrayList<IncidentPoint>();
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
            incidentPoints.add(new IncidentPoint(neighbours,i));
        }
        List<IncidentPoint> groups = new ArrayList<>();
        while(!NoMorePoints(incidentPoints))
        {
            IncidentPoint currMax = Collections.max(incidentPoints, Comparator.comparing(c -> c.getNeighbours().size()));
            groups.add(currMax);
            incidentPoints.remove(currMax);
            for (IncidentPoint point : incidentPoints) {
                for (Integer i : currMax.getNeighbours()) {
                    point.getNeighbours().remove(i);
                }
            }
        }
        Log.d("Groups",String.valueOf(groups.size()));
        List<Location> centers =  computeCenters(groups,locations);
        Log.d("Longitude",String.valueOf(centers.get(0).getLongitude()));
        Log.d("Latitude",String.valueOf(centers.get(0).getLatitude()));
        return centers;

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
