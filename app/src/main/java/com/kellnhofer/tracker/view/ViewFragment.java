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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.ViewContract;

public class ViewFragment extends Fragment implements OnMapReadyCallback, ViewContract.Observer {

    private static final String STATE_MAP_VIEW = "map_view";
    private static final String STATE_MAP_VIEW_INITIALIZED = "map_view_initialized";

    public static final String BUNDLE_KEY_LOCATION_ID = "location_id";

    private ViewActivity mActivity;
    private ViewContract.Presenter mPresenter;

    private MapView mMapView;
    private boolean mMapInitialized = false;

    private long mLocationId;

    public void setPresenter(@NonNull ViewContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (ViewActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + ViewActivity.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null || !arguments.containsKey(BUNDLE_KEY_LOCATION_ID)) {
            throw new IllegalStateException("Extras '" + BUNDLE_KEY_LOCATION_ID
                    + "' must be provided!");
        }
        mLocationId = arguments.getLong(BUNDLE_KEY_LOCATION_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(STATE_MAP_VIEW);
            mMapInitialized = savedInstanceState.getBoolean(STATE_MAP_VIEW_INITIALIZED);
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
        if (mMapInitialized) {
            return;
        }

        Location location = mPresenter.getLocation(mLocationId);

        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

        map.addMarker(new MarkerOptions().position(pos));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));

        mMapInitialized = true;
    }

}
