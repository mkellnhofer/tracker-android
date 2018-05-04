package com.kellnhofer.tracker.view;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.Date;

import com.kellnhofer.tracker.Constants;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.CreateContract;
import com.kellnhofer.tracker.presenter.CreatePresenter;

public class CreateActivity extends AppCompatActivity implements CreateContract.Observer,
        CreateDialogFragment.CreateDialogListener {

    private static final String FRAGMENT_TAG_CREATE = "create_fragment";
    private static final String FRAGMENT_TAG_CREATE_DIALOG = "create_dialog_fragment";

    private static final String STATE_REQUESTED_PERMISSIONS = "requested_permissions";
    private static final String STATE_CREATE_LOCATION = "create_location";

    private static final int REQUEST_CODE_PERMISSIONS = 1;

    private TrackerApplication mApplication;
    private CreateContract.Presenter mPresenter;

    private FloatingActionButton mFab;
    private CreateFragment mFragment;

    private boolean mRequestedPermissions = false;

    private double[] mCreateLocation = new double[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (TrackerApplication) getApplication();

        mPresenter = new CreatePresenter(this, Injector.getLocationService(this));

        setContentView(R.layout.activity_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClicked();
            }
        });
        if (savedInstanceState == null) {
            setFabEnabled(false);
        }

        if (savedInstanceState == null) {
            mFragment = new CreateFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_CREATE)
                    .commit();
        } else {
            mFragment = (CreateFragment) getSupportFragmentManager().findFragmentByTag(
                    FRAGMENT_TAG_CREATE);
        }

        mFragment.setPresenter(mPresenter);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRequestedPermissions = savedInstanceState.getBoolean(STATE_REQUESTED_PERMISSIONS);
        mCreateLocation = savedInstanceState.getDoubleArray(STATE_CREATE_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.removeObserver(this);
        mPresenter.onResume();

        requestGpsPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPresenter.onPause();
        mPresenter.removeObserver(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_REQUESTED_PERMISSIONS, mRequestedPermissions);
        outState.putDoubleArray(STATE_CREATE_LOCATION, mCreateLocation);
    }

    private void requestGpsPermissions() {
        if (mRequestedPermissions) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (!mApplication.hasGpsPermissions()) {
            requestPermissions(Constants.GPS_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        mRequestedPermissions = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.message_gps_permissions, Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void onFabClicked() {
        android.location.Location location = mPresenter.getGpsLocation();
        mCreateLocation[0] = location.getLatitude();
        mCreateLocation[1] = location.getLongitude();
        showCreateDialog();
    }

    private void setFabEnabled(boolean status) {
        mFab.setEnabled(status);

        int colorId = status ? R.color.color_accent : R.color.fab_bg_color_disabled;
        mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorId)));
    }

    // --- Fragment callback methods ---

    public void onLocationChanged() {
        setFabEnabled(true);
    }

    // --- Dialog methods ---

    private void showCreateDialog() {
        CreateDialogFragment fragment = CreateDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), FRAGMENT_TAG_CREATE_DIALOG);
    }

    @Override
    public void onCreateDialogOk(String name) {
        createLocation(name, mCreateLocation[0], mCreateLocation[1]);
    }

    @Override
    public void onCreateDialogCancel() {

    }

    // --- Presenter callback methods ---

    @Override
    public void onGpsLocationChanged(android.location.Location location) {

    }

    // --- Executor methods ---

    private void createLocation(String name, double lat, double lng) {
        Location location = new Location();
        location.setName(name);
        location.setDate(new Date());
        location.setLatitude(lat);
        location.setLongitude(lng);

        mPresenter.createLocation(location);

        finish();
    }

}
