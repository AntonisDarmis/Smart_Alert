package com.unipi.adarmis.smartalert.backend;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Ranking {
    public static void ranking(List<Location> locations,int radius)
    {
        int size = locations.size();
        double [][] distances = new double[size][size];
        for(int i=0;i<=size;i++)
        {
            for(int j=0;j<=size;i++)
            {
                //compute the distance in meters
                distances[i][j] = locations.get(i).distanceTo(locations.get(j));
            }
        }
        //radius in meters to search closest incidents

        int[] scores = new int[size];
        List<IncidentPoint> incidentPoints = new ArrayList<IncidentPoint>();
        for(int i=0;i<=size;i++)
        {
            List<Integer> neighbours = new ArrayList<>();
            //find incident
            for(int j=0;j<=size;j++)
            {
                if(distances[i][j]<=radius)
                {
                    //save neighbours of incident(i) if they belong in the specified radius
                    neighbours.add(j);
                }
            }
            incidentPoints.add(new IncidentPoint(neighbours));
            Log.d("Incidents", String.valueOf(incidentPoints.get(0)));
            Log.d("Incidents",String.valueOf(incidentPoints.get(2)));
        }

    }
}
