package com.kellnhofer.tracker.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.presenter.SearchContract;
import com.kellnhofer.tracker.presenter.SearchPresenter;

public class SearchActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG_VIEW = "search_fragment";

    private SearchFragment mFragment;

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchContract.Presenter presenter = new SearchPresenter(this, Injector.getLocationDao(this),
                Injector.getPersonDao(this));

        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (toolbar != null) {
            mEditText = toolbar.findViewById(R.id.view_search_string);
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
        }

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

}
