package com.kellnhofer.tracker.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.SearchContract;

public class SearchFragment extends Fragment implements LocationsAdapter.LocationItemListener,
        SearchContract.Observer, LoaderManager.LoaderCallbacks<List<Location>> {

    private static final String STATE_SEARCH_STRING = "search_string";

    private static final int LOADER_LOCATIONS = 0;

    private SearchActivity mActivity;
    private SearchContract.Presenter mPresenter;

    private String mSearchString = "";

    private LocationsLoader mLoader;
    private LocationsAdapter mAdapter;

    private LinearLayout mInfoContainer;
    private ListView mListView;

    public void setPresenter(@NonNull SearchContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (SearchActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + SearchActivity.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSearchString = savedInstanceState.getString(STATE_SEARCH_STRING, "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mAdapter = new LocationsAdapter(mActivity, new ArrayList<Location>(0));
        mAdapter.setLocationItemListener(this);

        mInfoContainer = view.findViewById(R.id.container_info);

        mListView = view.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_SEARCH_STRING, mSearchString);
    }

    @Override
    public void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(LOADER_LOCATIONS);
    }

    public void onSearchUpdate(String searchString) {
        if (mLoader == null) {
            return;
        }

        mSearchString = searchString;
        getLoaderManager().restartLoader(LOADER_LOCATIONS, null, this);
    }

    // --- Adapter callback methods ---

    @Override
    public void onLocationClick(Location location) {
        mPresenter.startViewActivity(location.getId());
    }

    // --- Presenter callback methods ---

    @Override
    public void onLocationsChanged() {
        if (mLoader != null) {
            mLoader.onContentChanged();
        }
    }

    // --- Loader methods ---

    @Override
    public Loader<List<Location>> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_LOCATIONS) {
            mLoader = new LocationsLoader(mActivity, mPresenter, mSearchString);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Location>> loader, List<Location> data) {
        if (loader.getId() == LOADER_LOCATIONS) {
            mAdapter.replaceData(data);
            mListView.setVisibility(!data.isEmpty() ? View.VISIBLE : View.GONE);
            mInfoContainer.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Location>> loader) {
        if (loader.getId() == LOADER_LOCATIONS) {
            mAdapter.replaceData(new ArrayList<Location>());
        }
    }

    // --- Locations loader ---

    private static class LocationsLoader extends AsyncTaskLoader<List<Location>> {

        private SearchContract.Presenter mPresenter;

        private String mSearchString = "";

        private List<Location> mData;

        public LocationsLoader(Context context, SearchContract.Presenter presenter,
                String searchString) {
            super(context);
            mPresenter = presenter;
            mSearchString = searchString;
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
            return mPresenter.searchLocations(mSearchString);
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
