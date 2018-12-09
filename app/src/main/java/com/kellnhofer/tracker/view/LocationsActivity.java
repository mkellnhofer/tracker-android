package com.kellnhofer.tracker.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.presenter.LocationsContract;
import com.kellnhofer.tracker.presenter.LocationsPresenter;
import com.kellnhofer.tracker.service.LocationSyncError;

public class LocationsActivity extends AppCompatActivity implements LocationsContract.Observer {

    private static final String LOG_TAG = LocationsActivity.class.getSimpleName();

    private static final String FRAGMENT_TAG_LOCATIONS = "locations_fragment";

    private LocationsContract.Presenter mPresenter;

    private LocationsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new LocationsPresenter(this, Injector.getLocationRepository(this),
                Injector.getLocationService(this));

        setContentView(R.layout.activity_locations);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startCreateActivity();
            }
        });

        if (savedInstanceState == null) {
            mFragment = new LocationsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_LOCATIONS)
                    .commit();
        } else {
            mFragment = (LocationsFragment) getSupportFragmentManager().findFragmentByTag(
                    FRAGMENT_TAG_LOCATIONS);
        }

        mFragment.setPresenter(mPresenter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.addObserver(this);
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPresenter.onPause();
        mPresenter.removeObserver(this);
    }

    // --- Action bar callback methods ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_locations_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                mPresenter.startSettingsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // --- Presenter callback methods ---

    @Override
    public void onLocationsChanged() {

    }

    @Override
    public void onSyncStarted() {
        Log.d(LOG_TAG, "onSyncStarted");
        // TODO!!!
    }

    @Override
    public void onSyncFinished() {
        Log.d(LOG_TAG, "onSyncFinished");
        // TODO!!!
    }

    @Override
    public void onSyncFailed(LocationSyncError error) {
        Log.d(LOG_TAG, "onSyncFailed");
        // TODO!!!
    }

}
