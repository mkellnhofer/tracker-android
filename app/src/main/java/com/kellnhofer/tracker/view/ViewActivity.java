package com.kellnhofer.tracker.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.presenter.ViewContract;
import com.kellnhofer.tracker.presenter.ViewPresenter;
import com.kellnhofer.tracker.util.DateUtils;

public class ViewActivity extends BaseActivity implements ViewContract.Observer,
        DecisionDialogFragment.Listener {

    private static final String FRAGMENT_TAG_VIEW = "view_fragment";
    private static final String DIALOG_FRAGMENT_TAG_VIEW = "view_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_DELETE = "delete_dialog_fragment";

    private static final String STATE_LOCATION = "location";
    private static final String STATE_LOCATION_PERSONS = "location_persons";

    public static final String EXTRA_LOCATION_ID = "location_id";

    private ViewContract.Presenter mPresenter;

    private MaterialToolbar mTopAppBar;
    private ViewFragment mFragment;

    private long mLocationId;
    private Location mLocation;
    private List<Person> mLocationPersons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new ViewPresenter(this, Injector.getLocationDao(this),
                Injector.getPersonDao(this), Injector.getLocationService(this));

        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_LOCATION_ID)) {
            throw new IllegalStateException("Extras '" + EXTRA_LOCATION_ID + "' must be provided!");
        }
        mLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, 0L);

        setContentView(R.layout.activity_view);

        AppBarLayout appBarLayout = findViewById(R.id.container_app_bar);
        appBarLayout.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));

        mTopAppBar = findViewById(R.id.top_app_bar);
        mTopAppBar.setNavigationOnClickListener(v -> finish());
        mTopAppBar.setOnMenuItemClickListener(this::onTopAppBarMenuItemClicked);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> onFabClicked());

        mFragment = (ViewFragment) getSupportFragmentManager().findFragmentByTag(
                FRAGMENT_TAG_VIEW);
        if (mFragment == null) {
            mFragment = new ViewFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_VIEW)
                    .commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mLocation = savedInstanceState.getParcelable(STATE_LOCATION);
        mLocationPersons = savedInstanceState.getParcelableArrayList(STATE_LOCATION_PERSONS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.getLocation(mLocationId).observe(this, (location) -> {
            mLocation = location;
            if (location != null) {
                mPresenter.getLocationPersons(mLocationId).observe(this, (locationPersons) ->
                        mLocationPersons = locationPersons);
                updateTopAppBarTitle();
                updateFragment();
            }
        });

        mPresenter.addObserver(this);
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPresenter.onPause();
        mPresenter.removeObserver(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_LOCATION, mLocation);
        outState.putParcelableArrayList(STATE_LOCATION_PERSONS, new ArrayList<>(mLocationPersons));
    }

    private boolean onTopAppBarMenuItemClicked(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                mPresenter.startEditActivity(mLocationId);
                return true;
            case R.id.action_delete:
                showDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateTopAppBarTitle() {
        mTopAppBar.setTitle(mLocation.getName());
        mTopAppBar.setSubtitle(DateUtils.toUiFormat(mLocation.getDate()));
    }

    private void updateFragment() {
        mFragment.setLatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    private void onFabClicked() {
        showViewDialog();
    }

    // --- Dialog methods ---

    private void showViewDialog() {
        if (mLocation == null || mLocationPersons == null) {
            return;
        }
        ViewDialogFragment fragment = ViewDialogFragment.newInstance(mLocation.getName(),
                mLocation.getDate(), mLocation.getDescription(), toPersonNameList(mLocationPersons));
        fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_VIEW);
    }

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

    // --- Helper methods ---

    private static List<String> toPersonNameList(List<Person> persons) {
        ArrayList<String> personNames = new ArrayList<>();
        for (Person person : persons) {
            String personName;
            if (!person.getFirstName().isEmpty() && !person.getLastName().isEmpty()) {
                personName = person.getFirstName() + " " + person.getLastName();
            } else {
                personName = person.getFirstName() + person.getLastName();
            }
            personNames.add(personName);
        }
        return personNames;
    }

}
