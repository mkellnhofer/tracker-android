package com.kellnhofer.tracker.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.CreateEditContract;
import com.kellnhofer.tracker.presenter.LatLng;

public class CreateEditFragment extends Fragment implements OnMapReadyCallback, OnMapClickListener,
        CreateEditContract.Observer {

    private static final String STATE_MAP_VIEW = "map_view";
    private static final String STATE_MAP_VIEW_INITIALIZED = "map_view_initialized";
    private static final String STATE_MAP_VIEW_CENTERED = "map_view_centered";

    public static final String BUNDLE_KEY_LOCATION_ID = "location_id";

    private CreateEditActivity mActivity;
    private CreateEditContract.Presenter mPresenter;

    private MapView mMapView;
    private GoogleMap mMap;
    private boolean mMapInitialized = false;
    private boolean mMapCentered = false;

    private long mLocationId;

    public void setPresenter(@NonNull CreateEditContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (CreateEditActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + CreateEditActivity.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mLocationId = arguments.getLong(BUNDLE_KEY_LOCATION_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_edit, container, false);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(STATE_MAP_VIEW);
            mMapInitialized = savedInstanceState.getBoolean(STATE_MAP_VIEW_INITIALIZED);
            mMapCentered = savedInstanceState.getBoolean(STATE_MAP_VIEW_CENTERED);
        }

        mMapView = (MapView) view.findViewById(R.id.map);
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

        mPresenter.addObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mPresenter.removeObserver(this);

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

    // --- Map callback methods ---

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        map.setOnMapClickListener(this);

        if (mMapInitialized) {
            return;
        }

        if (mLocationId == 0L) {
            return;
        }

        Location location = mPresenter.getLocation(mLocationId);

        com.google.android.gms.maps.model.LatLng pos = new com.google.android.gms.maps.model.LatLng(
                location.getLatitude(), location.getLongitude());

        map.addMarker(new MarkerOptions().position(pos));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));

        mMapInitialized = true;
        mMapCentered = true;
    }

    @Override
    public void onMapClick(com.google.android.gms.maps.model.LatLng pos) {
        if (mMap == null) {
            return;
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(pos));

        mActivity.onMapLocationClicked(new LatLng(pos.latitude, pos.longitude));
    }

    // --- Activity methods ---

    public void recenterMap() {
        mMapCentered = false;
    }

    // --- Presenter callback methods ---

    @Override
    public void onLocationCreated() {

    }

    @Override
    public void onLocationUpdated() {

    }

    @Override
    public void onGpsLocationChanged(LatLng latLng) {
        if (mMap == null) {
            return;
        }

        com.google.android.gms.maps.model.LatLng pos = new com.google.android.gms.maps.model.LatLng(
                latLng.lat, latLng.lng);

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(pos));

        if (mMapInitialized && mMapCentered) {
            return;
        }

        if (!mMapInitialized) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        }

        mMapInitialized = true;
        mMapCentered = true;
    }

}
