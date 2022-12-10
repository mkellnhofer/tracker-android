package com.kellnhofer.tracker.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.presenter.LocationsContract;
import com.kellnhofer.tracker.presenter.LocationsPresenter;
import com.kellnhofer.tracker.service.KmlExportError;
import com.kellnhofer.tracker.service.LocationSyncError;
import com.kellnhofer.tracker.util.ExportUtils;

public class LocationsActivity extends AppCompatActivity implements LocationsContract.Observer,
        ProgressBarDialogFragment.Listener, ErrorDialogFragment.Listener,
        InfoDialogFragment.Listener {

    private static final String FRAGMENT_TAG_LOCATIONS = "locations_fragment";
    private static final String DIALOG_FRAGMENT_TAG_SYNC_ERROR = "sync_error_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_KML_EXPORT = "kml_export_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_KML_EXPORT_ERROR =
            "kml_export_error_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_HELP = "help_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_API_KEY = "api_key_dialog_fragment";

    private static final int REQUEST_CODE_CREATE_KML_EXPORT_FILE = 0;

    private LocationsContract.Presenter mPresenter;

    private ErrorDialogFragment mSyncErrorDialogFragment;
    private ProgressBarDialogFragment mKmlExportDialogFragment;
    private ErrorDialogFragment mKmlExportErrorDialogFragment;

    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new LocationsPresenter(this, Injector.getLocationRepository(this),
                Injector.getLocationService(this), Injector.getExportService(this));

        setContentView(R.layout.activity_locations);

        mCoordinatorLayout = findViewById(R.id.container_coordinator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startCreateActivity();
            }
        });

        LocationsFragment fragment = (LocationsFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG_LOCATIONS);
        if (fragment == null) {
            fragment = new LocationsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_content, fragment, FRAGMENT_TAG_LOCATIONS)
                    .commit();
        }
        fragment.setPresenter(mPresenter);

        if (savedInstanceState != null) {
            mSyncErrorDialogFragment = (ErrorDialogFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_FRAGMENT_TAG_SYNC_ERROR);
            mKmlExportDialogFragment = (ProgressBarDialogFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_FRAGMENT_TAG_KML_EXPORT);
            mKmlExportErrorDialogFragment = (ErrorDialogFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_FRAGMENT_TAG_KML_EXPORT_ERROR);
        }

        if (savedInstanceState == null && isApiKeyMissing()) {
            showApiKeyErrorDialog();
        }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                mPresenter.startSearchActivity();
                return true;
            case R.id.action_settings:
                mPresenter.startSettingsActivity();
                return true;
            case R.id.action_kml_export:
                createKmlExportFile();
                return true;
            case R.id.action_help:
                showHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createKmlExportFile() {
        String fileName = ExportUtils.generateKmlExportFileName();
        String fileMimeType = ExportUtils.getKmlExportMimeType();

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        intent.setType(fileMimeType);
        startActivityForResult(intent, REQUEST_CODE_CREATE_KML_EXPORT_FILE);
    }

    // --- Activity result callback methods ---

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent returnIntent) {
        super.onActivityResult(requestCode, resultCode, returnIntent);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CREATE_KML_EXPORT_FILE:
                Uri fileUri = returnIntent.getData();
                mPresenter.executeKmlExport(fileUri);
                break;
            default:
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
            showSyncErrorSnackBar(error);
        } else {
            showSyncErrorDialog(error);
        }
    }

    @Override
    public void onKmlExportStarted() {
        showKmlExportDialog();
    }

    @Override
    public void onKmlExportProgress(int current, int total) {
        updateKmlExportDialog(current, total);
    }

    @Override
    public void onKmlExportFinished(int total) {
        updateKmlExportDialog(total, total);
    }

    @Override
    public void onKmlExportFailed(KmlExportError error) {
        showKmlExportErrorDialog(error);
    }

    // --- Dialog methods ---

    private void showSyncErrorDialog(LocationSyncError error) {
        if (mSyncErrorDialogFragment != null) {
            return;
        }

        mSyncErrorDialogFragment = ErrorDialogFragment.newInstance(R.string.dialog_title_error,
                error.getTextResId(), false);
        mSyncErrorDialogFragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_SYNC_ERROR);
    }

    private void showKmlExportDialog() {
        if (mKmlExportDialogFragment != null) {
            return;
        }

        mKmlExportDialogFragment = ProgressBarDialogFragment.newInstance(
                R.string.dialog_title_kml_export);
        mKmlExportDialogFragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_KML_EXPORT);
    }

    private void updateKmlExportDialog(int current, int total) {
        if (mKmlExportDialogFragment == null) {
            return;
        }

        mKmlExportDialogFragment.updateProgress(current, total);
    }

    private void showKmlExportErrorDialog(KmlExportError error) {
        if (mKmlExportErrorDialogFragment != null) {
            return;
        }

        mKmlExportErrorDialogFragment = ErrorDialogFragment.newInstance(R.string.dialog_title_error,
                error.getTextResId(), false);
        mKmlExportErrorDialogFragment.show(getSupportFragmentManager(),
                DIALOG_FRAGMENT_TAG_KML_EXPORT_ERROR);
    }

    private void showHelpDialog() {
        InfoDialogFragment.newInstance(R.string.dialog_title_help, R.string.dialog_message_help)
                .show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_HELP);
    }

    private void showApiKeyErrorDialog() {
        ApiKeyErrorDialogFragment.newInstance()
                .show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_API_KEY);
    }

    // --- Dialog callback methods ---

    @Override
    public void onProgressBarDialogOk(String tag) {
        switch (tag) {
            case DIALOG_FRAGMENT_TAG_KML_EXPORT:
                mKmlExportDialogFragment = null;
                break;
            default:
        }
    }

    @Override
    public void onProgressBarDialogCancel(String tag) {
        switch (tag) {
            case DIALOG_FRAGMENT_TAG_KML_EXPORT:
                mKmlExportDialogFragment = null;
                mPresenter.cancelKmlExport();
                break;
            default:
        }
    }

    @Override
    public void onErrorDialogRetry(String tag) {
        switch (tag) {
            case DIALOG_FRAGMENT_TAG_SYNC_ERROR:
                mSyncErrorDialogFragment = null;
                mPresenter.executeLocationSync();
                break;
            case DIALOG_FRAGMENT_TAG_KML_EXPORT_ERROR:
                mKmlExportErrorDialogFragment = null;
                break;
            default:
        }
    }

    @Override
    public void onErrorDialogCancel(String tag) {
        switch (tag) {
            case DIALOG_FRAGMENT_TAG_SYNC_ERROR:
                mSyncErrorDialogFragment = null;
                break;
            case DIALOG_FRAGMENT_TAG_KML_EXPORT_ERROR:
                mKmlExportErrorDialogFragment = null;
                break;
            default:
        }
    }

    @Override
    public void onInfoDialogOk(String tag) {

    }

    // --- SnackBar methods ---

    private void showSyncErrorSnackBar(LocationSyncError error) {
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

    // --- Helper methods ---

    private boolean isApiKeyMissing() {
        String key = getString(R.string.google_maps_api_key);
        return key == null || key.isEmpty();
    }

}
