package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.util.ValidationUtils;

public class ServerUrlDialogFragment extends DialogFragment {

    public static final String BUNDLE_KEY_URL = "url";

    public interface Listener {
        void onServerUrlDialogOk(String url);
        void onServerUrlDialogCancel();
    }

    private TextWatcher mUrlTextWatcher = new ValidationWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            validateUrl();
            updatePositiveButtonState();
        }
    };

    private Listener mListener;

    private EditText mUrlView;

    private Button mPositiveButton;

    private boolean mIsValidUrl = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            mListener = (Listener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement " + Listener.class.getName() + "!");
        }

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Arguments missing!");
        }

        String url = arguments.getString(BUNDLE_KEY_URL, "");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.dialog_name, null);

        mUrlView = view.findViewById(R.id.view_name);

        if (savedInstanceState == null) {
            mUrlView.setText(url);
            mUrlView.setSelection(url.length());
            mUrlView.setHint(R.string.hint_server_url);
            mUrlView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        }

        mUrlView.addTextChangedListener(mUrlTextWatcher);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_title_server_url)
                .setView(view)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onServerUrlDialogOk(mUrlView.getText().toString());
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ServerUrlDialogFragment.this.getDialog().cancel();
                    }
                }).create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        // The assignment has to be done here, because buttons are only available after the dialog
        // has been created
        AlertDialog dialog = (AlertDialog) getDialog();
        mPositiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
    }

    @Override
    public void onResume() {
        super.onResume();

        validateUrl();

        updatePositiveButtonState();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onServerUrlDialogCancel();
    }

    private void updatePositiveButtonState() {
        if (mPositiveButton != null) {
            mPositiveButton.setEnabled(mIsValidUrl);
        }
    }

    // --- Factory methods ---

    public static ServerUrlDialogFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString(ServerUrlDialogFragment.BUNDLE_KEY_URL, url);
        ServerUrlDialogFragment fragment = new ServerUrlDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // --- Helper methods ---

    private void validateUrl() {
        String url = mUrlView.getText().toString();
        mIsValidUrl = ValidationUtils.checkIsValidServerUrl(url);

        if (url.length() > 0 && !mIsValidUrl) {
            mUrlView.setError(getResources().getString(R.string.error_server_url_invalid));
        } else {
            mUrlView.setError(null);
        }
    }

}
