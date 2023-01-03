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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.SearchContract;

public class SearchFragment extends Fragment implements LocationsAdapter.LocationItemListener {

    private static final String STATE_SEARCH_STRING = "search_string";
    private static final String STATE_SCROLL_POSITION = "scroll_position";

    private SearchActivity mActivity;
    private SearchContract.Presenter mPresenter;

    private String mSearchString = "";

    private LocationsAdapter mAdapter;

    private LinearLayout mInfoContainer;
    private ListView mListView;
    private int mScrollPosition = 0;
    private boolean mRestoreScrollPosition = false;

    private LiveData<List<Location>> mLocations;
    private final Observer<List<Location>> mLocationsObserver = this::onLocationsLoaded;

    public void setPresenter(@NonNull SearchContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mActivity = (SearchActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement " +
                    SearchActivity.class.getName() + "!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSearchString = savedInstanceState.getString(STATE_SEARCH_STRING, "");
            mScrollPosition = savedInstanceState.getInt(STATE_SCROLL_POSITION, 0);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

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

        loadLocations();

        mRestoreScrollPosition = true;

        registerObservers();
    }

    @Override
    public void onPause() {
        super.onPause();

        mScrollPosition = mListView.getFirstVisiblePosition();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_SEARCH_STRING, mSearchString);
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterObservers();
    }

    public void onSearchUpdate(String searchString) {
        mSearchString = searchString;
        reloadLocations();
    }

    // --- Adapter callback methods ---

    @Override
    public void onLocationClick(Location location) {
        mPresenter.startViewActivity(location.getId());
    }

    // --- Loader methods ---

    private void registerObservers() {
        mLocations.observe(this, mLocationsObserver);
    }

    private void unregisterObservers() {
        mLocations.removeObserver(mLocationsObserver);
    }

    private void loadLocations() {
        mLocations = mPresenter.searchLocations(mSearchString);
    }

    private void reloadLocations() {
        if (isVisible()) {
            unregisterObservers();
        }
        loadLocations();
        if (isVisible()) {
            registerObservers();
        }
    }

    private void onLocationsLoaded(List<Location> locations) {
        mAdapter.replaceData(locations);
        restoreScrollPosition();
        mListView.setVisibility(!locations.isEmpty() ? View.VISIBLE : View.GONE);
        mInfoContainer.setVisibility(locations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void restoreScrollPosition() {
        if (!mRestoreScrollPosition) {
            return;
        }

        mListView.setSelection(mScrollPosition);
        mRestoreScrollPosition = false;
    }

}
