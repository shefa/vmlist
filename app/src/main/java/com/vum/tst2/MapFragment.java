package com.vum.tst2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

abstract class LinkMarkerLongClickListener implements GoogleMap.OnMarkerDragListener {

    private int previousIndex = -1;

    private Marker cachedMarker = null;
    private LatLng cachedDefaultPostion = null;

    private List<Marker> markerList;
    private List<LatLng> defaultPostions;

    public LinkMarkerLongClickListener(List<Marker> markerList) {
        this.markerList = new ArrayList<>(markerList);
        defaultPostions = new ArrayList<>(markerList.size());
        for (Marker marker : markerList) {
            defaultPostions.add(marker.getPosition());
            marker.setDraggable(true);
        }
    }

    public abstract void onLongClickListener(Marker marker);

    @Override
    public void onMarkerDragStart(Marker marker) {
        onLongClickListener(marker);
        setDefaultPostion(markerList.indexOf(marker));
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        setDefaultPostion(markerList.indexOf(marker));
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        setDefaultPostion(markerList.indexOf(marker));
    }


    private void setDefaultPostion(int markerIndex) {
        if (previousIndex == -1 || previousIndex != markerIndex) {
            cachedMarker = markerList.get(markerIndex);
            cachedDefaultPostion = defaultPostions.get(markerIndex);
            previousIndex = markerIndex;
        }
        cachedMarker.setPosition(cachedDefaultPostion);
    }
}

public class MapFragment extends SupportMapFragment implements
        OnMapReadyCallback {

    private final LatLng VUM = new LatLng(43.21194485250614, 27.90917068719864);

    private GoogleMap mMap;

    public MapFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        ((MainActivity) getActivity()).checkLocationPermission();
        ((MainActivity) getActivity()).updateMap();
    }

    public void updateMap(ArrayList<String> names, ArrayList<LatLng> points, ArrayList<Integer> ids) {
        ArrayList<Marker> l = new ArrayList<Marker>();

        mMap.clear();
        for (int i = 0; i < names.size(); i++) {
            Marker tmp = mMap.addMarker(new MarkerOptions().position(points.get(i)).title(names.get(i)).draggable(true));
            tmp.setTag(ids.get(i));
            l.add(tmp);
        }

        mMap.setOnMarkerDragListener(new LinkMarkerLongClickListener(l) {
            @Override
            public void onLongClickListener(Marker marker) {
                ((MainActivity) getActivity()).onMarkerClick(marker);
            }
        });
    }


    private void setUpMap() {

        if (ContextCompat.checkSelfPermission(((MainActivity)getActivity()),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(((MainActivity)getActivity()),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity().getApplicationContext() , R.raw.style_json));
            if (!success) Log.e("MyMap", "Style parsing failed.");
        } catch (Resources.NotFoundException e) {
            Log.e("MyMap", "Can't find style. Error: ", e);
        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                ((MainActivity)getActivity()).onMapClick(point);
            }
        });


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VUM, 10));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }
}