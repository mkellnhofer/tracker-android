package com.kellnhofer.tracker.view;

import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.kellnhofer.tracker.BuildConfig;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.TrackerSettings;
import com.kellnhofer.tracker.presenter.SettingsContract;
import com.kellnhofer.tracker.presenter.SettingsPresenter;
import com.kellnhofer.tracker.service.LocationSyncError;

public class SettingsActivity extends BaseActivity implements SettingsContract.Observer,
        ServerUrlDialogFragment.Listener, ServerPasswordDialogFragment.Listener,
        ErrorDialogFragment.Listener {

    private static final String FRAGMENT_TAG_SETTINGS = "settings_fragment";
    private static final String DIALOG_FRAGMENT_TAG_SERVER_URL = "server_url_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_SERVER_PW = "server_pw_dialog_fragment";
    private static final String DIALOG_FRAGMENT_TAG_SYNC_ERROR = "sync_error_dialog_fragment";

    private TrackerSettings mSettings;

    private SettingsContract.Presenter mPresenter;

    private SettingsFragment mFragment;
    private ErrorDialogFragment mSyncErrorDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TrackerApplication application = (TrackerApplication) getApplication();

        mSettings = application.getSettings();

        mPresenter = new SettingsPresenter(this, Injector.getLocationService(this));

        setContentView(R.layout.activity_settings);

        AppBarLayout appBarLayout = findViewById(R.id.container_app_bar);
        appBarLayout.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));

        MaterialToolbar topAppBar = findViewById(R.id.top_app_bar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        mFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(
                FRAGMENT_TAG_SETTINGS);
        if (mFragment == null) {
            mFragment = new SettingsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_SETTINGS)
                    .commit();
        }

        if (savedInstanceState != null) {
            mSyncErrorDialogFragment = (ErrorDialogFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_FRAGMENT_TAG_SYNC_ERROR);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        initServerUrlPreference();
        initServerPasswordPreference();
        initVersionPreference();
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

    private void initServerUrlPreference() {
        String url = mSettings.getServerUrl();
        mFragment.updatePreferenceSummary(TrackerSettings.PREF_KEY_SERVER_URL, url);
    }

    private void initServerPasswordPreference() {
        String password = mSettings.getServerPassword();
        mFragment.updatePreferenceSummary(TrackerSettings.PREF_KEY_SERVER_PASSWORD, password);
    }

    private void initVersionPreference() {
        String version = BuildConfig.VERSION_NAME;
        mFragment.updatePreferenceSummary(TrackerSettings.PREF_KEY_VERSION, version);
    }

    // --- Fragment callback methods ---

    public void onServerUrlClicked() {
        showServerUrlDialog();
    }

    public void onServerPasswordClicked() {
        showServerPasswordDialog();
    }

    // --- Presenter callback methods ---

    @Override
    public void onSyncStarted() {

    }

    @Override
    public void onSyncFinished() {

    }

    @Override
    public void onSyncFailed(LocationSyncError error) {
        showSyncErrorDialog(error);
    }

    // --- Dialog methods ---

    private void showServerUrlDialog() {
        String url = mSettings.getServerUrl();
        ServerUrlDialogFragment fragment = ServerUrlDialogFragment.newInstance(url);
        fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_SERVER_URL);
    }

    @Override
    public void onServerUrlDialogOk(String url) {
        mSettings.setServerUrl(url);
        mFragment.updatePreferenceSummary(TrackerSettings.PREF_KEY_SERVER_URL, url);
    }

    @Override
    public void onServerUrlDialogCancel() {

    }

    private void showServerPasswordDialog() {
        String password = mSettings.getServerPassword();
        ServerPasswordDialogFragment fragment = ServerPasswordDialogFragment.newInstance(password);
        fragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_SERVER_PW);
    }

    @Override
    public void onServerPasswordDialogOk(String password) {
        mSettings.setServerPassword(password);
        mFragment.updatePreferenceSummary(TrackerSettings.PREF_KEY_SERVER_PASSWORD, password);
    }

    @Override
    public void onServerPasswordDialogCancel() {

    }

    private void showSyncErrorDialog(LocationSyncError error) {
        if (mSyncErrorDialogFragment != null) {
            return;
        }

        boolean isCommunicationError = error == LocationSyncError.COMMUNICATION_ERROR;
        mSyncErrorDialogFragment = ErrorDialogFragment.newInstance(R.string.dialog_title_error,
                error.getTextResId(), isCommunicationError);
        mSyncErrorDialogFragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG_SYNC_ERROR);
    }

    // --- Dialog callback methods ---

    @Override
    public void onErrorDialogRetry(String tag) {
        switch (tag) {
            case DIALOG_FRAGMENT_TAG_SYNC_ERROR:
                mSyncErrorDialogFragment = null;
                mPresenter.executeLocationSync();
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
            default:
        }
    }

}
