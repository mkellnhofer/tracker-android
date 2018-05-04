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
import com.kellnhofer.tracker.presenter.CreateContract;

public class CreateFragment extends Fragment implements OnMapReadyCallback, CreateContract.Observer {

    private static final String LOG_TAG = CreateFragment.class.getSimpleName();

    private static final String STATE_MAP_VIEW = "map_view";
    private static final String STATE_MAP_VIEW_ZOOMED_IN = "map_view_zoomed_in";

    private CreateActivity mActivity;
    private CreateContract.Presenter mPresenter;

    private MapView mMapView;
    private GoogleMap mMap;
    private boolean mMapZoomedIn = false;

    public void setPresenter(@NonNull CreateContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (CreateActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + CreateActivity.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(STATE_MAP_VIEW);
            mMapZoomedIn = savedInstanceState.getBoolean(STATE_MAP_VIEW_ZOOMED_IN);
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
    public void onMapReady(GoogleMap map) {
        mMap = map;
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
        outState.putBoolean(STATE_MAP_VIEW_ZOOMED_IN, mMapZoomedIn);
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

    // --- Presenter callback methods ---

    @Override
    public void onGpsLocationChanged(android.location.Location location) {
        if (mMap == null) {
            return;
        }

        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(pos));

        if (!mMapZoomedIn) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));
            mMapZoomedIn = true;
        }

        mActivity.onLocationChanged();
    }

}
