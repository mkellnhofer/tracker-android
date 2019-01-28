package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.util.DateUtils;

public class ViewDialogFragment extends DialogFragment {

    public static final String BUNDLE_KEY_NAME = "name";
    public static final String BUNDLE_KEY_DATE = "date";
    public static final String BUNDLE_KEY_DESCRIPTION = "description";
    public static final String BUNDLE_KEY_PERSONS = "persons";

    public ViewDialogFragment() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Arguments missing!");
        }
        String name = arguments.getString(BUNDLE_KEY_NAME);
        Date date = new Date(arguments.getLong(BUNDLE_KEY_DATE));
        String description = arguments.getString(BUNDLE_KEY_DESCRIPTION);
        List<String> persons = arguments.getStringArrayList(BUNDLE_KEY_PERSONS);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_view, null);

        TextView nameView = view.findViewById(R.id.view_name);
        TextView dateView = view.findViewById(R.id.view_date);
        TextView descriptionView = view.findViewById(R.id.view_description);
        TextView noPersonsView = view.findViewById(R.id.view_no_persons);
        LinearLayout personsContainer = view.findViewById(R.id.container_persons);

        nameView.setText(name);
        dateView.setText(DateUtils.toUiFormat(date));
        if (description != null && !description.isEmpty()) {
            descriptionView.setText(description);
        } else {
            descriptionView.setVisibility(View.GONE);
        }

        if (persons != null && !persons.isEmpty()) {
            for (String person : persons) {
                addPersonView(personsContainer, person);
            }
        } else {
            noPersonsView.setVisibility(View.VISIBLE);
            personsContainer.setVisibility(View.GONE);
        }

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_title_view)
                .setView(view)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                }).create();
    }

    private void addPersonView(LinearLayout personsContainer, String name) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        TextView nameView = (TextView) inflater.inflate(R.layout.view_view_person, null);

        nameView.setId(View.generateViewId());
        nameView.setText(name);

        personsContainer.addView(nameView);
    }

    // --- Factory methods ---

    public static ViewDialogFragment newInstance(String name, Date date, String description,
            ArrayList<String> persons) {
        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_NAME, name);
        args.putLong(BUNDLE_KEY_DATE, date.getTime());
        args.putString(BUNDLE_KEY_DESCRIPTION, description);
        args.putStringArrayList(BUNDLE_KEY_PERSONS, persons);

        ViewDialogFragment fragment = new ViewDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

}