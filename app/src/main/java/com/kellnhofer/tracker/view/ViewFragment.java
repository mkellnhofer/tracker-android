package com.kellnhofer.tracker.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kellnhofer.tracker.R;

public class ViewFragment extends Fragment implements OnMapReadyCallback {

    private static final String STATE_POS = "pos";
    private static final String STATE_MAP_VIEW = "map_view";

    private MapView mMapView;
    private GoogleMap mMap;
    private boolean mMapInitialized = false;

    private LatLng mPos;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mPos = savedInstanceState.getParcelable(STATE_POS);
            mapViewBundle = savedInstanceState.getBundle(STATE_MAP_VIEW);
        }

        mMapView = view.findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        mMapView.onResume();
        mMapView.getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(STATE_MAP_VIEW);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
        }

        mMapView.onSaveInstanceState(mapViewBundle);

        outState.putParcelable(STATE_POS, mPos);
        outState.putBundle(STATE_MAP_VIEW, mapViewBundle);
    }

    @Override
    public void onStop() {
        mMapView.onStop();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();

        super.onLowMemory();
    }

    // --- Activity methods ---

    public void setLatLng(double latitude, double longitude) {
        mPos = new LatLng(latitude, longitude);
        if (mMap != null) {
            initializeMap();
        }
    }

    // --- Map methods ---

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        mMap = map;

        if (mPos != null) {
            initializeMap();
        }
    }

    private void initializeMap() {
        if (mMapInitialized) {
            return;
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mPos));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPos, 14));

        mMapInitialized = true;
    }

}
