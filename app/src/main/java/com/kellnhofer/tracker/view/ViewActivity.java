package com.kellnhofer.tracker.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.presenter.ViewContract;
import com.kellnhofer.tracker.presenter.ViewPresenter;
import com.kellnhofer.tracker.util.DateUtils;

public class ViewActivity extends AppCompatActivity implements ViewContract.Observer,
        DecisionDialogFragment.Listener {

    private static final String FRAGMENT_TAG_VIEW = "view_fragment";
    private static final String DIALOG_FRAGMENT_TAG_DELETE = "delete_dialog_fragment";

    public static final String EXTRA_LOCATION_ID = "location_id";

    private TrackerApplication mApplication;
    private ViewContract.Presenter mPresenter;

    private TextView mNameTextView;
    private TextView mDateTextView;
    private FloatingActionButton mFab;
    private ViewFragment mFragment;

    private long mLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (TrackerApplication) getApplication();

        mPresenter = new ViewPresenter(this, Injector.getLocationRepository(this),
                Injector.getLocationService(this));

        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_LOCATION_ID)) {
            throw new IllegalStateException("Extras '" + EXTRA_LOCATION_ID + "' must be provided!");
        }
        mLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, 0L);

        setContentView(R.layout.activity_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mNameTextView = (TextView) toolbar.findViewById(R.id.view_location_name);
        mDateTextView = (TextView) toolbar.findViewById(R.id.view_location_date);

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

        Location location = mPresenter.getLocation(mLocationId);
        updateToolbar(location.getName(), location.getDate());

        mPresenter.addObserver(this);
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPresenter.onPause();
        mPresenter.removeObserver(this);
    }

    private void updateToolbar(String name, Date date) {
        mNameTextView.setText(name);
        mDateTextView.setText(DateUtils.toUiFormat(date));
    }

    private void onFabClicked() {
        mPresenter.startEditActivity(mLocationId);
    }

    // --- Action bar callback methods ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_view_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:
                showDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // --- Dialog methods ---

    private void showDeleteDialog() {
        DecisionDialogFragment fragment = DecisionDialogFragment.newInstance(
                R.string.dialog_title_delete, R.string.dialog_message_delete, R.string.action_delete);
        fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_DELETE);
    }

    @Override
    public void onDecisionDialogOk(String tag) {
        switch (tag) {
            case DIALOG_FRAGMENT_TAG_DELETE:
                mPresenter.deleteLocation(mLocationId);
                finish();
                break;
            default:
        }
    }

    @Override
    public void onDecisionDialogCancel(String tag) {

    }

}
