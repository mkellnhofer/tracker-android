package com.kellnhofer.tracker.view;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kellnhofer.tracker.BuildConfig;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.TrackerSettings;
import com.kellnhofer.tracker.presenter.SettingsContract;
import com.kellnhofer.tracker.presenter.SettingsPresenter;

public class SettingsActivity extends AppCompatActivity implements SettingsContract.Observer,
        ServerUrlDialogFragment.Listener {

    private static final String FRAGMENT_TAG_SETTINGS = "settings_fragment";
    private static final String DIALOG_FRAGMENT_TAG_SERVER_URL = "server_url_dialog_fragment";

    private TrackerApplication mApplication;

    private TrackerSettings mSettings;

    private SettingsContract.Presenter mPresenter;

    private SettingsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (TrackerApplication) getApplication();

        mSettings = mApplication.getSettings();

        mPresenter = new SettingsPresenter(this, Injector.getLocationService(this));

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            mFragment = new SettingsFragment();

            getFragmentManager().beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_SETTINGS)
                    .commit();
        } else {
            mFragment = (SettingsFragment) getFragmentManager().findFragmentByTag(
                    FRAGMENT_TAG_SETTINGS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        initServerUrlPreference();
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

    private void initVersionPreference() {
        String version = BuildConfig.VERSION_NAME;
        mFragment.updatePreferenceSummary(TrackerSettings.PREF_KEY_VERSION, version);
    }

    // --- Fragment callback methods ---

    public void onServerUrlClicked() {
        showServerUrlDialog();
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

}