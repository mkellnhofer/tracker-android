package com.kellnhofer.tracker.view;

import java.util.ArrayList;
import java.util.Date;

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

    public static final String EXTRA_LOCATION_ID = "location_id";

    private ViewContract.Presenter mPresenter;

    private TextView mNameTextView;
    private TextView mDateTextView;

    private long mLocationId;
    private String mLocationName;
    private Date mLocationDate;
    private String mLocationDescription;
    private ArrayList<String> mLocationPersons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new ViewPresenter(this, Injector.getLocationRepository(this),
                Injector.getPersonRepository(this), Injector.getLocationService(this));

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

        ViewFragment fragment = (ViewFragment) getSupportFragmentManager().findFragmentByTag(
                FRAGMENT_TAG_VIEW);
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putLong(ViewFragment.BUNDLE_KEY_LOCATION_ID, mLocationId);

            fragment = new ViewFragment();
            fragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_content, fragment, FRAGMENT_TAG_VIEW)
                    .commit();
        }

        fragment.setPresenter(mPresenter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Location location = mPresenter.getLocation(mLocationId);
        ArrayList<Person> locationPersons = mPresenter.getLocationPersons(mLocationId);

        mLocationName = location.getName();
        mLocationDate = location.getDate();
        mLocationDescription = location.getDescription();
        mLocationPersons = new ArrayList<>();
        for (Person locationPerson : locationPersons) {
            String name;
            if (!locationPerson.getFirstName().isEmpty() &&
                    !locationPerson.getLastName().isEmpty()) {
                name = locationPerson.getFirstName() + " " + locationPerson.getLastName();
            } else {
                name = locationPerson.getFirstName() + locationPerson.getLastName();
            }
            mLocationPersons.add(name);
        }

        updateToolbar(mLocationName, mLocationDate);

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
        ViewDialogFragment fragment = ViewDialogFragment.newInstance(mLocationName, mLocationDate,
                mLocationDescription, mLocationPersons);
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

}
