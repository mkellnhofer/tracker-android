package com.kellnhofer.tracker.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kellnhofer.tracker.Constants;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.presenter.CreateEditContract;
import com.kellnhofer.tracker.presenter.CreateEditPresenter;
import com.kellnhofer.tracker.presenter.LatLng;

public class CreateEditActivity extends AppCompatActivity implements CreateEditContract.Observer,
        CreateEditDialogFragment.Listener {

    private static final String FRAGMENT_TAG_CREATE_EDIT = "create_edit_fragment";
    private static final String DIALOG_FRAGMENT_TAG_CREATE_EDIT = "create_edit_dialog_fragment";

    private static final String STATE_LOCATION = "location";
    private static final String STATE_LOCATION_PERSONS = "location_persons";
    private static final String STATE_USE_GPS_LOCATION = "use_gps_location";
    private static final String STATE_REQUESTED_PERMISSIONS = "requested_permissions";

    private static final int REQUEST_CODE_PERMISSIONS = 1;

    public static final String EXTRA_LOCATION_ID = "location_id";

    private TrackerApplication mApplication;
    private CreateEditContract.Presenter mPresenter;

    private FloatingActionButton mGpsFab;
    private FloatingActionButton mOkFab;
    private CreateEditFragment mFragment;

    private long mLocationId;
    private Location mLocation;
    private List<Person> mLocationPersons;

    private boolean mUseGpsLocation = false;
    private boolean mRequestedPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (TrackerApplication) getApplication();

        mPresenter = new CreateEditPresenter(this, Injector.getLocationDao(this),
                Injector.getPersonDao(this), Injector.getLocationService(this));

        Intent intent = getIntent();
        mLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, 0L);

        if (savedInstanceState == null && mLocationId == 0L) {
            mLocation = new Location(0L, 0L, false, false, "", new Date(), 0.0, 0.0, "");
            mLocationPersons = new ArrayList<>();
        }

        if (savedInstanceState == null && mLocationId == 0L) {
            mUseGpsLocation = true;
        }

        setContentView(R.layout.activity_create_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (savedInstanceState == null) {
            String createTile = getString(R.string.activity_title_create);
            String editTile = getString(R.string.activity_title_edit);
            toolbar.setTitle(mLocationId == 0L ? createTile : editTile);
        }

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mGpsFab = findViewById(R.id.fab_gps);
        mGpsFab.setOnClickListener(v -> onGpsFabClicked());

        mOkFab = findViewById(R.id.fab_ok);
        mOkFab.setOnClickListener(v -> onOkFabClicked());

        if (savedInstanceState == null && mLocationId == 0L) {
            setOkFabEnabled(false);
        }

        mFragment = (CreateEditFragment) getSupportFragmentManager().findFragmentByTag(
                FRAGMENT_TAG_CREATE_EDIT);
        if (mFragment == null) {
            Bundle args = new Bundle();
            args.putLong(CreateEditFragment.BUNDLE_KEY_LOCATION_ID, mLocationId);

            mFragment = new CreateEditFragment();
            mFragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_CREATE_EDIT)
                    .commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mLocation = savedInstanceState.getParcelable(STATE_LOCATION);
        mLocationPersons = savedInstanceState.getParcelableArrayList(STATE_LOCATION_PERSONS);
        mUseGpsLocation = savedInstanceState.getBoolean(STATE_USE_GPS_LOCATION);
        mRequestedPermissions = savedInstanceState.getBoolean(STATE_REQUESTED_PERMISSIONS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLocationId != 0L && mLocation == null) {
            mPresenter.getLocation(mLocationId).observe(this, (location) -> {
                mLocation = location;
                if (location != null) {
                    mPresenter.getLocationPersons(mLocationId).observe(this, (locationPersons) ->
                            mLocationPersons = locationPersons);
                    updateFragment();
                }
            });
        }

        mPresenter.addObserver(this);
        mPresenter.onResume();

        if (mUseGpsLocation) {
            mPresenter.requestGpsLocationUpdates();
            requestGpsPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mUseGpsLocation) {
            mPresenter.removeGpsLocationUpdates();
        }

        mPresenter.onPause();
        mPresenter.removeObserver(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_LOCATION, mLocation);
        outState.putParcelableArrayList(STATE_LOCATION_PERSONS, new ArrayList<>(mLocationPersons));
        outState.putBoolean(STATE_USE_GPS_LOCATION, mUseGpsLocation);
        outState.putBoolean(STATE_REQUESTED_PERMISSIONS, mRequestedPermissions);
    }

    private void updateFragment() {
        mFragment.setLatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    private void onGpsFabClicked() {
        mPresenter.requestGpsLocationUpdates();
        requestGpsPermissions();
    }

    private void setGpsFabColor(int color) {
        Drawable d = mGpsFab.getDrawable();
        DrawableCompat.setTint(d, getResources().getColor(color));
    }

    private void onOkFabClicked() {
        if (mUseGpsLocation) {
            LatLng latLng = mPresenter.getGpsLocation();
            saveLocationLatLngChanges(latLng.lat, latLng.lng);
        }
        showCreateEditDialog();
    }

    private void setOkFabEnabled(boolean status) {
        mOkFab.setEnabled(status);

        int colorId = status ? R.color.accent : R.color.fab_bg_disabled;
        mOkFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorId)));
    }

    // --- Action bar callback methods ---

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // --- Fragment callback methods ---

    public void onMapLocationClicked(double lat, double lng) {
        mPresenter.removeGpsLocationUpdates();
        mUseGpsLocation = false;
        setGpsFabColor(R.color.icon_dark);
        setOkFabEnabled(true);
        saveLocationLatLngChanges(lat, lng);
    }

    // --- Presenter callback methods ---

    @Override
    public void onGpsLocationChanged(LatLng latLng) {
        mFragment.updateLatLng(latLng.lat, latLng.lng, !mUseGpsLocation);
        setGpsFabColor(R.color.accent);
        setOkFabEnabled(true);
        mUseGpsLocation = true;
    }

    // --- Dialog methods ---

    private void showCreateEditDialog() {
        mPresenter.getPersons().observe(this, (persons -> {
            List<String> locationPersonNames = toPersonNameList(mLocationPersons);
            List<String> personNames = toPersonNameList(persons);

            CreateEditDialogFragment fragment;
            if (mLocationId == 0L) {
                fragment = CreateEditDialogFragment.newCreateInstance(mLocation.getName(),
                        mLocation.getDate(), mLocation.getDescription(), locationPersonNames,
                        personNames);
            } else {
                fragment = CreateEditDialogFragment.newEditInstance(mLocation.getName(),
                        mLocation.getDate(), mLocation.getDescription(), locationPersonNames,
                        personNames);
            }
            fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_CREATE_EDIT);
        }));
    }

    @Override
    public void onCreateEditDialogOk(String name, Date date, String description,
            ArrayList<String> personNames) {
        saveLocationChanges(name, date, description, toPersonList(personNames));
        if (mLocationId == 0L) {
            mPresenter.createLocation(mLocation, mLocationPersons);
            finish();
        } else {
            mPresenter.updateLocation(mLocation, mLocationPersons);
            finish();
        }
    }

    @Override
    public void onCreateEditDialogCancel(String name, Date date, String description,
            ArrayList<String> personNames) {
        saveLocationChanges(name, date, description, toPersonList(personNames));
    }

    // --- GPS permission methods ---

    private void requestGpsPermissions() {
        if (mRequestedPermissions) {
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

    // --- Helper methods ---

    private void saveLocationChanges(String name, Date date, String description,
            List<Person> persons) {
        mLocation.setName(name);
        mLocation.setDate(date);
        mLocation.setDescription(description);
        mLocationPersons = persons;
    }

    private void saveLocationLatLngChanges(double lat, double lng) {
        mLocation.setLatitude(lat);
        mLocation.setLongitude(lng);
    }

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

    private static List<Person> toPersonList(List<String> personNames) {
        if (personNames == null) {
            return null;
        }
        ArrayList<Person> persons = new ArrayList<>();
        for (String personName : personNames) {
            String[] names = personName.split(" +", 2);
            String firstName = names.length > 1 ? names[0] : "";
            String lastName = names.length > 1 ? names[1] : names[0];
            if (!firstName.isEmpty() || !lastName.isEmpty()) {
                persons.add(new Person(0, firstName, lastName));
            }
        }
        return persons;
    }

}
