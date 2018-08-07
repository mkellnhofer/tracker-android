package com.kellnhofer.tracker.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.ViewContract;
import com.kellnhofer.tracker.presenter.ViewPresenter;
import com.kellnhofer.tracker.util.DateUtils;

public class ViewActivity extends AppCompatActivity implements ViewContract.Observer {

    private static final String FRAGMENT_TAG_VIEW = "view_fragment";

    public static final String EXTRA_LOCATION_ID = "location_id";

    private TrackerApplication mApplication;
    private ViewContract.Presenter mPresenter;

    private FloatingActionButton mFab;
    private ViewFragment mFragment;

    private long mLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (TrackerApplication) getApplication();

        mPresenter = new ViewPresenter(this, Injector.getLocationRepository(this));

        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_LOCATION_ID)) {
            throw new IllegalStateException("Extras '" + EXTRA_LOCATION_ID + "' must be provided!");
        }
        mLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, 0L);

        Location location = mPresenter.getLocation(mLocationId);

        setContentView(R.layout.activity_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (savedInstanceState == null) {
            TextView textViewName = (TextView) toolbar.findViewById(R.id.view_location_name);
            textViewName.setText(location.getName());
            TextView textViewDate = (TextView) toolbar.findViewById(R.id.view_location_date);
            textViewDate.setText(DateUtils.toUiFormat(location.getDate()));
        }

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
            Bundle args = new Bundle();
            args.putLong(ViewFragment.BUNDLE_KEY_LOCATION_ID, mLocationId);

            mFragment = new ViewFragment();
            mFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_VIEW)
                    .commit();
        } else {
            mFragment = (ViewFragment) getSupportFragmentManager().findFragmentByTag(
                    FRAGMENT_TAG_VIEW);
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

    private void onFabClicked() {
        mPresenter.startEditActivity(mLocationId);
        finish();
    }

}
