package com.kellnhofer.tracker.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kellnhofer.tracker.R;

public class CreateEditFragment extends Fragment implements OnMapReadyCallback, OnMapClickListener {

    private static final String STATE_POS = "pos";
    private static final String STATE_MAP_VIEW = "map_view";
    private static final String STATE_MAP_VIEW_INITIALIZED = "map_view_initialized";
    private static final String STATE_MAP_VIEW_CENTERED = "map_view_centered";

    public static final String BUNDLE_KEY_LOCATION_ID = "location_id";

    private CreateEditActivity mActivity;

    private MapView mMapView;
    private GoogleMap mMap;
    private boolean mMapInitialized = false;
    private boolean mMapCentered = false;

    private LatLng mPos;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mActivity = (CreateEditActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement " +
                    CreateEditActivity.class.getName() + "!");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_edit, container, false);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mPos = savedInstanceState.getParcelable(STATE_POS);
            mapViewBundle = savedInstanceState.getBundle(STATE_MAP_VIEW);
            mMapInitialized = savedInstanceState.getBoolean(STATE_MAP_VIEW_INITIALIZED);
            mMapCentered = savedInstanceState.getBoolean(STATE_MAP_VIEW_CENTERED);
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

        mMap = null;
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
        outState.putBoolean(STATE_MAP_VIEW_INITIALIZED, mMapInitialized);
        outState.putBoolean(STATE_MAP_VIEW_CENTERED, mMapCentered);
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

    public void updateLatLng(double latitude, double longitude, boolean center) {
        mPos = new LatLng(latitude, longitude);
        if (mMap != null) {
            updateMap(center);
        }
    }

    // --- Map callback methods ---

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        mMap = map;

        map.setOnMapClickListener(this);

        if (mPos != null) {
            initializeMap();
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng pos) {
        if (mMap == null) {
            return;
        }

        mPos = pos;

        updateMap(false);

        mActivity.onMapLocationClicked(pos.latitude, pos.longitude);
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

    private void updateMap(boolean center) {
        if (!mMapInitialized) {
            initializeMap();
            return;
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mPos));
        if (center) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mPos));
        }
    }

}
