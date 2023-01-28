package com.unipi.adarmis.smartalert.backend;

import java.util.List;

public class IncidentPoint
{
    int point;
    List<Integer> neighbours;

    IncidentPoint(List<Integer>neighbours,int point)
    {
        this.point = point;
        this.neighbours = neighbours;
    }


    public List<Integer> getNeighbours(){return neighbours;}

}
