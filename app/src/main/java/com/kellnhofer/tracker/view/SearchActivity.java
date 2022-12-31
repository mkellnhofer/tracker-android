package com.kellnhofer.tracker.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.presenter.SearchContract;
import com.kellnhofer.tracker.presenter.SearchPresenter;

public class SearchActivity extends BaseActivity {

    private static final String FRAGMENT_TAG_VIEW = "search_fragment";

    private SearchFragment mFragment;

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchContract.Presenter presenter = new SearchPresenter(this, Injector.getLocationDao(this),
                Injector.getPersonDao(this));

        setContentView(R.layout.activity_search);

        AppBarLayout appBarLayout = findViewById(R.id.container_app_bar);
        appBarLayout.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));

        MaterialToolbar topAppBar = findViewById(R.id.top_app_bar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setOnMenuItemClickListener(this::onTopAppBarMenuItemClicked);

        mEditText = topAppBar.findViewById(R.id.view_search_string);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mFragment == null) {
                    return;
                }

                mFragment.onSearchUpdate(s.toString());
            }
        });

        mFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(
                FRAGMENT_TAG_VIEW);
        if (mFragment == null) {
            mFragment = new SearchFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_content, mFragment, FRAGMENT_TAG_VIEW)
                    .commit();
        }

        mFragment.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEditText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private boolean onTopAppBarMenuItemClicked(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_cancel:
                mEditText.setText("");
            default:
                return false;
        }
    }

}
