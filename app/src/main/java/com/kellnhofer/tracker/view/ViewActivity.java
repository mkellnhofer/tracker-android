package com.kellnhofer.tracker.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.presenter.ViewContract;
import com.kellnhofer.tracker.presenter.ViewPresenter;
import com.kellnhofer.tracker.util.DateUtils;

public class ViewActivity extends AppCompatActivity implements ViewContract.Observer,
        DecisionDialogFragment.Listener {

    private static final String FRAGMENT_TAG_VIEW = "view_fragment";
    private static final String DIALOG_FRAGMENT_TAG_VIEW = "view_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_DELETE = "delete_dialog_fragment";

    private static final String STATE_LOCATION = "location";
    private static final String STATE_LOCATION_PERSONS = "location_persons";

    public static final String EXTRA_LOCATION_ID = "location_id";

    private ViewContract.Presenter mPresenter;

    private ViewFragment mFragment;

    private TextView mNameTextView;
    private TextView mDateTextView;

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

        Toolbar toolbar = findViewById(R.id.toolbar);

        mNameTextView = toolbar.findViewById(R.id.view_location_name);
        mDateTextView = toolbar.findViewById(R.id.view_location_date);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
                updateToolbar();
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

    private void updateToolbar() {
        mNameTextView.setText(mLocation.getName());
        mDateTextView.setText(DateUtils.toUiFormat(mLocation.getDate()));
    }

    private void updateFragment() {
        mFragment.setLatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    private void onFabClicked() {
        showViewDialog();
    }

    // --- Action bar callback methods ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_view_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
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
