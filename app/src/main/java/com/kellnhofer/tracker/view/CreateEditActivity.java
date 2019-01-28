package com.kellnhofer.tracker.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

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

    private static final String STATE_LOCATION_NAME = "location_name";
    private static final String STATE_LOCATION_DATE = "location_date";
    private static final String STATE_LAT_LNG = "lat_lng";
    private static final String STATE_LOCATION_DESCRIPTION = "location_description";
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
    private String mLocationName = "";
    private Date mLocationDate = new Date();
    private LatLng mLocationLatLng = new LatLng();
    private String mLocationDescription = null;
    private ArrayList<String> mLocationPersonNames = new ArrayList<>();

    private boolean mUseGpsLocation = false;
    private boolean mRequestedPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (TrackerApplication) getApplication();

        mPresenter = new CreateEditPresenter(this, Injector.getLocationRepository(this),
                Injector.getPersonRepository(this), Injector.getLocationService(this));

        Intent intent = getIntent();
        mLocationId = intent.getLongExtra(EXTRA_LOCATION_ID, 0L);

        if (savedInstanceState == null && mLocationId != 0L) {
            Location location = mPresenter.getLocation(mLocationId);
            mLocationName = location.getName();
            mLocationDate = location.getDate();
            mLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mLocationDescription = location.getDescription();
            ArrayList<Person> locationPersons = mPresenter.getLocationPersons(mLocationId);
            mLocationPersonNames = toPersonNameList(locationPersons);
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
        mGpsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGpsFabClicked();
            }
        });

        mOkFab = findViewById(R.id.fab_ok);
        mOkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkFabClicked();
            }
        });

        if (savedInstanceState == null && mLocationId == 0L) {
            setOkFabEnabled(false);
        }

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putLong(ViewFragment.BUNDLE_KEY_LOCATION_ID, mLocationId);

            mFragment = new CreateEditFragment();
            mFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_CREATE_EDIT)
                    .commit();
        } else {
            mFragment = (CreateEditFragment) getSupportFragmentManager().findFragmentByTag(
                    FRAGMENT_TAG_CREATE_EDIT);
        }

        mFragment.setPresenter(mPresenter);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mLocationName = savedInstanceState.getString(STATE_LOCATION_NAME);
        mLocationDate = new Date(savedInstanceState.getLong(STATE_LOCATION_DATE));
        double[] pos = savedInstanceState.getDoubleArray(STATE_LAT_LNG);
        if (pos != null && pos.length >= 2) {
            mLocationLatLng = new LatLng(pos[0], pos[1]);
        } else {
            mLocationLatLng = new LatLng();
        }
        mLocationDescription = savedInstanceState.getString(STATE_LOCATION_DESCRIPTION);
        mLocationPersonNames = savedInstanceState.getStringArrayList(STATE_LOCATION_PERSONS);
        mUseGpsLocation = savedInstanceState.getBoolean(STATE_USE_GPS_LOCATION);
        mRequestedPermissions = savedInstanceState.getBoolean(STATE_REQUESTED_PERMISSIONS);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_LOCATION_NAME, mLocationName);
        outState.putLong(STATE_LOCATION_DATE, mLocationDate.getTime());
        double[] pos = new double[]{mLocationLatLng.lng, mLocationLatLng.lat};
        outState.putDoubleArray(STATE_LAT_LNG, pos);
        outState.putString(STATE_LOCATION_DESCRIPTION, mLocationDescription);
        outState.putStringArrayList(STATE_LOCATION_PERSONS, mLocationPersonNames);
        outState.putBoolean(STATE_USE_GPS_LOCATION, mUseGpsLocation);
        outState.putBoolean(STATE_REQUESTED_PERMISSIONS, mRequestedPermissions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void onGpsFabClicked() {
        mFragment.recenterMap();
        mPresenter.requestGpsLocationUpdates();
        requestGpsPermissions();
    }

    private void setGpsFabColor(int color) {
        Drawable d = mGpsFab.getDrawable();
        DrawableCompat.setTint(d, getResources().getColor(color));
    }

    private void onOkFabClicked() {
        if (mUseGpsLocation) {
            mLocationLatLng = mPresenter.getGpsLocation();
        }
        showCreateEditDialog();
    }

    private void setOkFabEnabled(boolean status) {
        mOkFab.setEnabled(status);

        int colorId = status ? R.color.color_accent : R.color.fab_bg_color_disabled;
        mOkFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorId)));
    }

    // --- Fragment callback methods ---

    public void onMapLocationClicked(LatLng latLng) {
        mPresenter.removeGpsLocationUpdates();
        mUseGpsLocation = false;
        setGpsFabColor(R.color.color_icon_dark);
        setOkFabEnabled(true);
        mLocationLatLng = latLng;
    }

    // --- Dialog methods ---

    private void showCreateEditDialog() {
        ArrayList<Person> persons = mPresenter.getPersons();
        ArrayList<String> personNames = toPersonNameList(persons);

        CreateEditDialogFragment fragment;
        if (mLocationId == 0L) {
            fragment = CreateEditDialogFragment.newCreateInstance(personNames);
        } else {
            fragment = CreateEditDialogFragment.newEditInstance(mLocationName, mLocationDate,
                    mLocationDescription, mLocationPersonNames, personNames);
        }
        fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_CREATE_EDIT);
    }

    @Override
    public void onCreateEditDialogOk(String locationName, Date locationDate,
            String locationDescription, ArrayList<String> locationPersonNames) {
        if (mLocationId == 0L) {
            createLocation(locationName, locationDate, mLocationLatLng, locationDescription,
                    locationPersonNames);
        } else {
            updateLocation(locationName, locationDate, mLocationLatLng, locationDescription,
                    locationPersonNames);
        }
    }

    @Override
    public void onCreateEditDialogCancel() {

    }

    // --- Presenter callback methods ---

    @Override
    public void onLocationCreated() {
        finish();
    }

    @Override
    public void onLocationUpdated() {
        finish();
    }

    @Override
    public void onGpsLocationChanged(LatLng latLng) {
        mUseGpsLocation = true;
        setGpsFabColor(R.color.color_accent);
        setOkFabEnabled(true);
    }

    // --- Executor methods ---

    private void createLocation(String name, Date date, LatLng latLng, String description,
            ArrayList<String> personNames) {
        Location location = new Location();
        location.setName(name);
        location.setDate(date);
        location.setLatitude(latLng.lat);
        location.setLongitude(latLng.lng);
        location.setDescription(description);

        ArrayList<Person> persons = toPersonList(personNames);

        mPresenter.createLocation(location, persons);
    }

    private void updateLocation(String name, Date date, LatLng latLng, String description,
            ArrayList<String> personNames) {
        Location location = new Location();
        location.setId(mLocationId);
        location.setName(name);
        location.setDate(date);
        location.setLatitude(latLng.lat);
        location.setLongitude(latLng.lng);
        location.setDescription(description);

        ArrayList<Person> persons = toPersonList(personNames);

        mPresenter.updateLocation(location, persons);
    }

    // --- Helper methods ---

    private static ArrayList<String> toPersonNameList(ArrayList<Person> persons) {
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

    private static ArrayList<Person> toPersonList(ArrayList<String> personNames) {
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
