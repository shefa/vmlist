package com.vum.tst2.ViewModels;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MapViewModel {
    public ArrayList<String> names;
    public ArrayList<LatLng> points;
    public ArrayList<Integer> ids;
    public MapViewModel(ArrayList<String> n, ArrayList<LatLng> p, ArrayList<Integer> i)
    {
        names=n;
        points=p;
        ids=i;
    }
}
