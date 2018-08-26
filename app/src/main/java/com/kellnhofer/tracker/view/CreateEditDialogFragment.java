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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.Date;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.util.DateUtils;

public class CreateEditDialogFragment extends DialogFragment {

    public static final int MODE_CREATE = 1;
    public static final int MODE_EDIT = 2;

    public static final String BUNDLE_KEY_MODE = "mode";
    public static final String BUNDLE_KEY_NAME = "name";
    public static final String BUNDLE_KEY_DATE = "date";

    public interface Listener {
        void onCreateEditDialogOk(String name, Date date);
        void onCreateEditDialogCancel();
    }

    private TextWatcher mNameTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mIsValidName = isValidName(s.toString());
            updateNameViewError();
            updatePositiveButtonState();
        }

    };

    private TextWatcher mDateTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mIsValidDate = isValidDate(s.toString());
            updateDateViewError();
            updatePositiveButtonState();
        }

    };

    private Listener mListener;

    private EditText mNameView;
    private EditText mDateView;

    private Button mPositiveButton;

    private boolean mIsValidName = false;
    private boolean mIsValidDate = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            mListener = (Listener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement " +
                    Listener.class.getName() + "!");
        }

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Arguments missing!");
        }
        int mode = arguments.getInt(BUNDLE_KEY_MODE);
        String name = arguments.getString(BUNDLE_KEY_NAME, "");
        Date date = new Date(arguments.getLong(BUNDLE_KEY_DATE));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.dialog_create_edit, null);

        mNameView = (EditText) view.findViewById(R.id.view_name);
        mDateView = (EditText) view.findViewById(R.id.view_date);

        if (savedInstanceState == null) {
            if (mode == MODE_CREATE) {
                mDateView.setText(DateUtils.toUiFormat(new Date()));
            } else if (mode == MODE_EDIT) {
                mNameView.setText(name);
                mNameView.setSelection(name.length());
                mDateView.setText(DateUtils.toUiFormat(date));
            }
        }

        mNameView.addTextChangedListener(mNameTextWatcher);
        mDateView.addTextChangedListener(mDateTextWatcher);

        int titleText = mode == MODE_CREATE ? R.string.dialog_title_create : R.string.dialog_title_edit;
        int actionText = mode == MODE_CREATE ? R.string.action_create : R.string.action_save;

        return new AlertDialog.Builder(getContext())
                .setTitle(titleText)
                .setView(view)
                .setPositiveButton(actionText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = mNameView.getText().toString().trim();
                        Date date = DateUtils.fromUiFormat(mDateView.getText().toString());
                        mListener.onCreateEditDialogOk(name, date);
                        CreateEditDialogFragment.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        CreateEditDialogFragment.this.getDialog().cancel();
                    }
                }).create();
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

        mIsValidName = isValidName(mNameView.getText().toString());
        mIsValidDate = isValidDate(mDateView.getText().toString());

        updateNameViewError();
        updateDateViewError();
        updatePositiveButtonState();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onCreateEditDialogCancel();
    }

    private void updateNameViewError() {
        if (!mIsValidName) {
            mNameView.setError(getResources().getString(R.string.error_location_name_invalid));
        } else {
            mNameView.setError(null);
        }
    }

    private void updateDateViewError() {
        if (!mIsValidDate) {
            mDateView.setError(getResources().getString(R.string.error_location_date_invalid));
        } else {
            mDateView.setError(null);
        }
    }

    private void updatePositiveButtonState() {
        if (mPositiveButton != null) {
            mPositiveButton.setEnabled(mIsValidName && mIsValidDate);
        }
    }

    // --- Factory methods ---

    public static CreateEditDialogFragment newCreateInstance() {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_MODE, MODE_CREATE);

        CreateEditDialogFragment fragment = new CreateEditDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static CreateEditDialogFragment newEditInstance(String name, Date date) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_MODE, MODE_EDIT);
        args.putString(BUNDLE_KEY_NAME, name);
        args.putLong(BUNDLE_KEY_DATE, date.getTime());

        CreateEditDialogFragment fragment = new CreateEditDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    // --- Helper methods ---

    private static boolean isValidName(String name) {
        return name.length() > 0 && name.length() < 100;
    }

    private static boolean isValidDate(String date) {
        try {
            DateUtils.fromUiFormat(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}