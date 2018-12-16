package com.kellnhofer.tracker.view;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.presenter.LocationsContract;
import com.kellnhofer.tracker.presenter.LocationsPresenter;
import com.kellnhofer.tracker.service.LocationSyncError;

public class LocationsActivity extends AppCompatActivity implements LocationsContract.Observer,
        ErrorDialogFragment.Listener {

    private static final String FRAGMENT_TAG_LOCATIONS = "locations_fragment";
    private static final String DIALOG_FRAGMENT_TAG_ERROR = "error_dialog_fragment";

    private LocationsContract.Presenter mPresenter;

    private LocationsFragment mFragment;
    private ErrorDialogFragment mErrorDialogFragment;

    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new LocationsPresenter(this, Injector.getLocationRepository(this),
                Injector.getLocationService(this));

        setContentView(R.layout.activity_locations);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.container_coordinator);

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
            mErrorDialogFragment = (ErrorDialogFragment) getSupportFragmentManager().findFragmentByTag(
                    DIALOG_FRAGMENT_TAG_ERROR);
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

    }

    @Override
    public void onSyncFinished() {

    }

    @Override
    public void onSyncFailed(LocationSyncError error) {
        if (error == LocationSyncError.COMMUNICATION_ERROR) {
            showErrorSnackBar(error);
        } else {
            showErrorDialog(error);
        }
    }

    // --- Dialog methods ---

    private void showErrorDialog(LocationSyncError error) {
        if (mErrorDialogFragment != null) {
            return;
        }

        mErrorDialogFragment = ErrorDialogFragment.newInstance(R.string.dialog_title_error,
                error.getTextResId(), false);
        mErrorDialogFragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_ERROR);
    }

    @Override
    public void onErrorDialogRetry(String tag) {
        mErrorDialogFragment = null;
    }

    @Override
    public void onErrorDialogCancel(String tag) {
        mErrorDialogFragment = null;
    }

    // --- SnackBar methods ---

    private void showErrorSnackBar(LocationSyncError error) {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, error.getTextResId(),
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.executeLocationSync();
            }
        });
        snackbar.show();
    }

}
