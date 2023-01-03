package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kellnhofer.tracker.R;

public class ServerPasswordDialogFragment extends DialogFragment {

    private static final String BUNDLE_KEY_PASSWORD = "password";

    public interface Listener {
        void onServerPasswordDialogOk(String password);
        void onServerPasswordDialogCancel();
    }

    private final TextWatcher mPasswordTextWatcher = new ValidationWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            validatePassword();
            updatePositiveButtonState();
        }
    };

    private Listener mListener;

    private EditText mPasswordView;

    private Button mPositiveButton;

    private boolean mIsValidPassword = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            mListener = (Listener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity() + " must implement " +
                    Listener.class.getName() + "!");
        }

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Arguments missing!");
        }

        String password = arguments.getString(BUNDLE_KEY_PASSWORD, "");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.dialog_name, null);

        mPasswordView = view.findViewById(R.id.view_name);

        if (savedInstanceState == null) {
            mPasswordView.setText(password);
            mPasswordView.setSelection(password.length());
            mPasswordView.setHint(R.string.hint_server_password);
            mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        }

        mPasswordView.addTextChangedListener(mPasswordTextWatcher);

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dialog_title_server_password)
                .setView(view)
                .setPositiveButton(R.string.action_ok, (d, id) ->
                        mListener.onServerPasswordDialogOk(mPasswordView.getText().toString()))
                .setNegativeButton(R.string.action_cancel, (d, id) -> {})
                .create();

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

        validatePassword();

        updatePositiveButtonState();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onServerPasswordDialogCancel();
    }

    private void updatePositiveButtonState() {
        if (mPositiveButton != null) {
            mPositiveButton.setEnabled(mIsValidPassword);
        }
    }

    // --- Factory methods ---

    public static ServerPasswordDialogFragment newInstance(String password) {
        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_PASSWORD, password);
        ServerPasswordDialogFragment fragment = new ServerPasswordDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // --- Helper methods ---

    private void validatePassword() {
        String password = mPasswordView.getText().toString();
        mIsValidPassword = !password.isEmpty();
    }

}
