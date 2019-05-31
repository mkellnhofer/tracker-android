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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Date;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.util.DateUtils;

public class CreateEditDialogFragment extends DialogFragment {

    private static final String STATE_PERSON_VIEWS_IDS = "person_views_ids";

    private static final int LENGTH_MIN_NAME = 1;
    private static final int LENGTH_MAX_NAME = 100;
    private static final int LENGTH_MIN_DESCRIPTION = 0;
    private static final int LENGTH_MAX_DESCRIPTION = 2000;
    private static final int LENGTH_MIN_PERSON_NAME = 0;
    private static final int LENGTH_MAX_PERSON_NAME = 100;

    public static final int MODE_CREATE = 1;
    public static final int MODE_EDIT = 2;

    public static final String BUNDLE_KEY_MODE = "mode";
    public static final String BUNDLE_KEY_LOCATION_NAME = "location_name";
    public static final String BUNDLE_KEY_LOCATION_DATE = "location_date";
    public static final String BUNDLE_KEY_LOCATION_DESCRIPTION = "location_description";
    public static final String BUNDLE_KEY_LOCATION_PERSON_NAMES = "location_person_names";
    public static final String BUNDLE_KEY_PERSON_NAMES = "person_names";

    public interface Listener {
        void onCreateEditDialogOk(String locationName, Date locationDate, String locationDescription,
                ArrayList<String> locationPersonNames);

        void onCreateEditDialogCancel(String locationName, Date locationDate,
                String locationDescription, ArrayList<String> locationPersonNames);
    }

    private TextWatcher mNameTextWatcher = new ValidationWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            validateName();
            updatePositiveButtonState();
        }
    };

    private TextWatcher mDateTextWatcher = new ValidationWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            validateDate();
            updatePositiveButtonState();
        }
    };

    private TextWatcher mDescriptionTextWatcher = new ValidationWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            validateDescription();
            updatePositiveButtonState();
        }

    };

    private TextWatcher mPersonNameTextWatcher = new ValidationWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            validatePersonNames();
            updatePositiveButtonState();
        }
    };

    private Listener mListener;

    private ArrayList<String> mPersonNames;

    private EditText mNameView;
    private EditText mDateView;
    private EditText mDescriptionView;
    private LinearLayout mPersonsContainer;
    private ArrayList<EditText> mPersonNameViews;

    private Button mPositiveButton;

    private boolean mIsValidName = false;
    private boolean mIsValidDate = false;
    private boolean mIsValidDescription = false;
    private boolean mAreValidPersonNames = false;

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
        String locationName = arguments.getString(BUNDLE_KEY_LOCATION_NAME, "");
        Date locationDate = new Date(arguments.getLong(BUNDLE_KEY_LOCATION_DATE));
        String locationDescription = arguments.getString(BUNDLE_KEY_LOCATION_DESCRIPTION, "");
        ArrayList<String> locationPersonNames = arguments.getStringArrayList(
                BUNDLE_KEY_LOCATION_PERSON_NAMES);
        if (locationPersonNames == null) {
            locationPersonNames = new ArrayList<>();
        }

        mPersonNames = arguments.getStringArrayList(BUNDLE_KEY_PERSON_NAMES);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_create_edit, null);

        mNameView = view.findViewById(R.id.view_name);
        mDateView = view.findViewById(R.id.view_date);
        mDescriptionView = view.findViewById(R.id.view_description);
        mPersonsContainer = view.findViewById(R.id.container_persons);
        mPersonNameViews = new ArrayList<>();

        if (savedInstanceState == null) {
            mNameView.setText(locationName);
            mNameView.setSelection(locationName.length());
            mDateView.setText(DateUtils.toUiFormat(locationDate));
            mDescriptionView.setText(locationDescription);
        }

        mNameView.addTextChangedListener(mNameTextWatcher);
        mDateView.addTextChangedListener(mDateTextWatcher);
        mDescriptionView.addTextChangedListener(mDescriptionTextWatcher);

        if (savedInstanceState == null) {
            for (String locationPersonName : locationPersonNames) {
                addPersonView(null, locationPersonName, false);
            }
            addPersonView(null, "", true);
        } else {
            int[] personViewsIds = savedInstanceState.getIntArray(STATE_PERSON_VIEWS_IDS);
            int i;
            for (i=0; i<personViewsIds.length/4-1; i++) {
                addPersonView(getFromPersonViewsIds(personViewsIds, i), "", false);
            }
            addPersonView(getFromPersonViewsIds(personViewsIds, i), "", true);
        }

        int titleText = mode == MODE_CREATE ? R.string.dialog_title_create : R.string.dialog_title_edit;
        int actionText = mode == MODE_CREATE ? R.string.action_create : R.string.action_save;

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(titleText)
                .setView(view)
                .setPositiveButton(actionText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        notifyOk();
                        CreateEditDialogFragment.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        notifyCancel();
                        CreateEditDialogFragment.this.getDialog().dismiss();
                    }
                }).create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

    private void addPersonView(int[] viewIds, String name, boolean isLast) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final RelativeLayout container = (RelativeLayout) inflater.inflate(
                R.layout.view_create_edit_person, null);
        final AutoCompleteTextView nameView = container.findViewById(R.id.view_person_name);
        final ImageButton addButton = container.findViewById(R.id.button_person_add);
        final ImageButton removeButton = container.findViewById(R.id.button_person_remove);

        container.setId(viewIds != null ? viewIds[0] : View.generateViewId());
        nameView.setId(viewIds != null ? viewIds[1] : View.generateViewId());
        nameView.setText(name);
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, mPersonNames);
        nameView.setAdapter(nameAdapter);
        nameView.addTextChangedListener(mPersonNameTextWatcher);
        addButton.setId(viewIds != null ? viewIds[2] : View.generateViewId());
        addButton.setVisibility(isLast ? View.VISIBLE : View.GONE);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.VISIBLE);
                addPersonView(null, "", true);
            }
        });
        removeButton.setId(viewIds != null ? viewIds[3] : View.generateViewId());
        removeButton.setVisibility(!isLast ? View.VISIBLE : View.GONE);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPersonsContainer.removeView(container);
                mPersonNameViews.remove(nameView);
                validatePersonNames();
                updatePositiveButtonState();
            }
        });

        mPersonsContainer.addView(container);
        mPersonNameViews.add(nameView);
    }

    private ArrayList<String> getPersonNames() {
        ArrayList<String> names = new ArrayList<>();
        for (int i=0; i<mPersonsContainer.getChildCount(); i++) {
            ViewGroup vg = (ViewGroup) mPersonsContainer.getChildAt(i);
            EditText v = (EditText) vg.getChildAt(0);
            String name = v.getText().toString().trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
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

        validateName();
        validateDate();
        validateDescription();
        validatePersonNames();

        updatePositiveButtonState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int[] personViewsIds = new int[mPersonsContainer.getChildCount()*4];
        for (int i=0; i<mPersonsContainer.getChildCount(); i++) {
            ViewGroup vg = (ViewGroup) mPersonsContainer.getChildAt(i);
            addToPersonViewsIds(personViewsIds, i, vg.getId(), vg.getChildAt(0).getId(),
                    vg.getChildAt(1).getId(), vg.getChildAt(2).getId());
        }
        outState.putIntArray(STATE_PERSON_VIEWS_IDS, personViewsIds);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        notifyCancel();
    }

    private void updatePositiveButtonState() {
        if (mPositiveButton != null) {
            mPositiveButton.setEnabled(mIsValidName && mIsValidDate && mIsValidDescription &&
                    mAreValidPersonNames);
        }
    }

    private void notifyOk() {
        String locationName = mNameView.getText().toString().trim();
        Date locationDate = DateUtils.fromUiFormat(mDateView.getText().toString());
        String locationDescription = mDescriptionView.getText().toString().trim();
        ArrayList<String> locationPersonNames = getPersonNames();

        mListener.onCreateEditDialogOk(locationName, locationDate, locationDescription,
                locationPersonNames);
    }

    private void notifyCancel() {
        String locationName = mNameView.getText().toString().trim();
        Date locationDate = DateUtils.fromUiFormat(mDateView.getText().toString());
        String locationDescription = mDescriptionView.getText().toString().trim();
        ArrayList<String> locationPersonNames = getPersonNames();

        mListener.onCreateEditDialogCancel(locationName, locationDate, locationDescription,
                locationPersonNames);
    }

    // --- Factory methods ---

    public static CreateEditDialogFragment newCreateInstance(String locationName, Date locationDate,
            String locationDescription, ArrayList<String> locationPersonNames,
            ArrayList<String> personNames) {
        return newInstance(MODE_CREATE, locationName, locationDate, locationDescription,
                locationPersonNames, personNames);
    }

    public static CreateEditDialogFragment newEditInstance(String locationName, Date locationDate,
            String locationDescription, ArrayList<String> locationPersonNames,
            ArrayList<String> personNames) {
        return newInstance(MODE_EDIT, locationName, locationDate, locationDescription,
                locationPersonNames, personNames);
    }

    private static CreateEditDialogFragment newInstance(int mode, String locationName,
            Date locationDate, String locationDescription, ArrayList<String> locationPersonNames,
            ArrayList<String> personNames) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_MODE, mode);
        args.putString(BUNDLE_KEY_LOCATION_NAME, locationName);
        args.putLong(BUNDLE_KEY_LOCATION_DATE, locationDate.getTime());
        args.putString(BUNDLE_KEY_LOCATION_DESCRIPTION, locationDescription);
        args.putStringArrayList(BUNDLE_KEY_LOCATION_PERSON_NAMES, locationPersonNames);
        args.putStringArrayList(BUNDLE_KEY_PERSON_NAMES, personNames);

        CreateEditDialogFragment fragment = new CreateEditDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    // --- Helper methods ---

    private void validateName() {
        String name = mNameView.getText().toString();
        mIsValidName = isValidName(name);

        if (!mIsValidName) {
            mNameView.setError(getResources().getString(R.string.error_location_name_invalid));
        } else {
            mNameView.setError(null);
        }
    }

    private static boolean isValidName(String name) {
        return name.length() >= LENGTH_MIN_NAME && name.length() < LENGTH_MAX_NAME;
    }

    private void validateDate() {
        String date = mDateView.getText().toString();
        mIsValidDate = isValidDate(date);

        if (!mIsValidDate) {
            mDateView.setError(getResources().getString(R.string.error_location_date_invalid));
        } else {
            mDateView.setError(null);
        }
    }

    private static boolean isValidDate(String date) {
        try {
            DateUtils.fromUiFormat(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateDescription() {
        String description = mDescriptionView.getText().toString();
        mIsValidDescription = isValidDescription(description);

        if (!mIsValidDescription) {
            mDescriptionView.setError(getResources().getString(
                    R.string.error_location_description_invalid));
        } else {
            mDescriptionView.setError(null);
        }
    }

    private static boolean isValidDescription(String description) {
        return description.length() >= LENGTH_MIN_DESCRIPTION &&
                description.length() < LENGTH_MAX_DESCRIPTION;
    }

    private void validatePersonNames() {
        boolean areValidPersonNames = true;
        ArrayList<String> personNames = new ArrayList<>();

        for (EditText personNameView : mPersonNameViews) {
            String personName = personNameView.getText().toString();

            boolean isValidPersonName = isValidPersonName(personName);
            if (personNames.contains(personName)) {
                isValidPersonName = false;
            }

            if (!personName.isEmpty()) {
                personNames.add(personName);
            }

            if (!isValidPersonName) {
                personNameView.setError(getResources().getString(
                        R.string.error_location_person_name_invalid));
                areValidPersonNames = false;
            } else {
                personNameView.setError(null);
            }
        }

        mAreValidPersonNames = areValidPersonNames;
    }

    private static boolean isValidPersonName(String name) {
        return name.length() >= LENGTH_MIN_PERSON_NAME && name.length() < LENGTH_MAX_PERSON_NAME;
    }

    private static void addToPersonViewsIds(int[] ids, int pos, int id1, int id2, int id3, int id4) {
        ids[4*pos] = id1;
        ids[4*pos+1] = id2;
        ids[4*pos+2] = id3;
        ids[4*pos+3] = id4;
    }

    private static int[] getFromPersonViewsIds(int[] ids, int pos) {
        return new int[]{ids[4*pos], ids[4*pos+1], ids[4*pos+2], ids[4*pos+3]};
    }

}