package com.vum.tst2.Views;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vum.tst2.Activities.MainActivity;
import com.vum.tst2.R;
import com.vum.tst2.ViewModels.MapViewModel;

import java.util.ArrayList;

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
        ((MainActivity) getActivity()).mapActivity.updateMap();
    }

    public void updateMap(MapViewModel mvm) {
        ArrayList<Marker> l = new ArrayList<Marker>();

        mMap.clear();
        for (int i = 0; i < mvm.names.size(); i++) {
            Marker tmp = mMap.addMarker(new MarkerOptions().position(mvm.points.get(i)).title(mvm.names.get(i)).draggable(true));
            tmp.setTag(mvm.ids.get(i));
            l.add(tmp);
        }

        mMap.setOnMarkerDragListener(new LinkMarkerLongClickListener(l) {
            @Override
            public void onLongClickListener(Marker marker) {
                ((MainActivity) getActivity()).mapActivity.onMarkerClick(marker);
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
                ((MainActivity)getActivity()).mapActivity.onMapClick(point);
            }
        });


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VUM, 10));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }
}