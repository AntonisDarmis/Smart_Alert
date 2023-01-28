package com.unipi.adarmis.smartalert.backend;

import java.util.List;

public class IncidentPoint
{

    List<Integer> neighbours;

    IncidentPoint(List<Integer>neighbours)
    {
        this.neighbours = neighbours;
    }


    public List<Integer> getNeighbours(){return neighbours;}

}
