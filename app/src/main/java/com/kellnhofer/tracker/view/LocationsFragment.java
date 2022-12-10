package com.kellnhofer.tracker.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.LocationsContract;
import com.kellnhofer.tracker.service.KmlExportError;
import com.kellnhofer.tracker.service.LocationSyncError;

public class LocationsFragment extends Fragment implements LocationsAdapter.LocationItemListener,
        LocationsContract.Observer, LoaderManager.LoaderCallbacks<List<Location>> {

    private static final String STATE_SCROLL_POSITION = "scroll_position";

    private static final int LOADER_LOCATIONS = 0;

    private LocationsActivity mActivity;
    private LocationsContract.Presenter mPresenter;

    private LocationsLoader mLoader;
    private LocationsAdapter mAdapter;

    private LinearLayout mInfoContainer;
    private ListView mListView;
    private int mScrollPosition = 0;
    private boolean mRestoreScrollPosition = false;

    public void setPresenter(@NonNull LocationsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mActivity = (LocationsActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement " +
                    LocationsActivity.class.getName() + "!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mScrollPosition = savedInstanceState.getInt(STATE_SCROLL_POSITION, 0);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations, container, false);

        mAdapter = new LocationsAdapter(mActivity, new ArrayList<>(0));
        mAdapter.setLocationItemListener(this);

        mInfoContainer = view.findViewById(R.id.container_info);

        mListView = view.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mRestoreScrollPosition = true;
        getLoaderManager().initLoader(LOADER_LOCATIONS, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.addObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mPresenter.removeObserver(this);

        mScrollPosition = mListView.getFirstVisiblePosition();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_SCROLL_POSITION, mScrollPosition);
    }

    @Override
    public void onStop() {
        super.onStop();

        getLoaderManager().destroyLoader(LOADER_LOCATIONS);
    }

    // --- Adapter callback methods ---

    @Override
    public void onLocationClick(Location location) {
        mPresenter.startViewActivity(location.getId());
    }

    // --- Presenter callback methods ---

    @Override
    public void onLocationsChanged() {
        if (mLoader == null) {
            return;
        }

        mScrollPosition = mListView.getFirstVisiblePosition();
        mRestoreScrollPosition = true;

        mLoader.onContentChanged();
    }

    @Override
    public void onSyncStarted() {

    }

    @Override
    public void onSyncFinished() {

    }

    @Override
    public void onSyncFailed(LocationSyncError error) {

    }

    @Override
    public void onKmlExportStarted() {

    }

    @Override
    public void onKmlExportProgress(int current, int total) {

    }

    @Override
    public void onKmlExportFinished(int total) {

    }

    @Override
    public void onKmlExportFailed(KmlExportError error) {

    }

    // --- Loader methods ---

    @Override
    public Loader<List<Location>> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_LOCATIONS) {
            mLoader = new LocationsLoader(mActivity, mPresenter);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Location>> loader, List<Location> data) {
        if (loader.getId() == LOADER_LOCATIONS) {
            mAdapter.replaceData(data);
            restoreScrollPosition();
            mListView.setVisibility(!data.isEmpty() ? View.VISIBLE : View.GONE);
            mInfoContainer.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void restoreScrollPosition() {
        if (!mRestoreScrollPosition) {
            return;
        }

        mListView.post(() -> {
            mListView.setSelection(mScrollPosition);
            mRestoreScrollPosition = false;
        });
    }

    @Override
    public void onLoaderReset(Loader<List<Location>> loader) {
        if (loader.getId() == LOADER_LOCATIONS) {
            mAdapter.replaceData(new ArrayList<>());
        }
    }

    // --- Locations loader ---

    private static class LocationsLoader extends AsyncTaskLoader<List<Location>> {

        private final LocationsContract.Presenter mPresenter;

        private List<Location> mData;

        LocationsLoader(Context context, LocationsContract.Presenter presenter) {
            super(context);
            mPresenter = presenter;
        }

        @Override
        protected void onStartLoading() {
            if (mData != null) {
                deliverResult(mData);
            } else {
                forceLoad();
            }
        }

        @Override
        public List<Location> loadInBackground() {
            return mPresenter.getNotDeletedLocationsByDateDesc();
        }

        @Override
        public void deliverResult(List<Location> data) {
            mData = data;
            if (isStarted()) {
                super.deliverResult(data);
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        protected void onReset() {
            cancelLoad();
        }

    }

}
