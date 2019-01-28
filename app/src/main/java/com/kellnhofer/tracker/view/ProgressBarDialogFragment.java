package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kellnhofer.tracker.R;

public class ProgressBarDialogFragment extends DialogFragment {

    private static final String BUNDLE_KEY_TITLE = "title";

    private static final String FORMAT_PERCENT_DEFAULT = "? %";
    private static final String FORMAT_PERCENT = "%.0f %%";
    private static final String FORMAT_ABSOLUTE_DEFAULT = "?/?";
    private static final String FORMAT_ABSOLUTE = "%d/%d";

    private static final String STATE_PROGRESS_FORMAT = "progress_format";
    private static final String STATE_PROGRESS_CURRENT = "progress_current";
    private static final String STATE_PROGRESS_TOTAL = "progress_total";

    private static final int PROGRESS_UNKNOWN = -1;

    public interface Listener {
        void onProgressBarDialogOk(String tag);
        void onProgressBarDialogCancel(String tag);
    }

    private Listener mListener;

    private String mProgressFormatAbsolute = FORMAT_ABSOLUTE;
    private int mProgressCurrent = PROGRESS_UNKNOWN;
    private int mProgressTotal = PROGRESS_UNKNOWN;

    private ProgressBar mProgressBar;
    private TextView mPercentTextView;
    private TextView mAbsoluteTextView;

    private Button mPositiveButton;
    private Button mNegativeButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            mListener = (Listener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement" +
                    Listener.class.getName() + "!");
        }

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Arguments missing!");
        }
        int titleResId = arguments.getInt(BUNDLE_KEY_TITLE, R.string.dialog_title_error);

        if (savedInstanceState != null) {
            mProgressFormatAbsolute = savedInstanceState.getString(STATE_PROGRESS_FORMAT);
            mProgressCurrent = savedInstanceState.getInt(STATE_PROGRESS_CURRENT, PROGRESS_UNKNOWN);
            mProgressTotal = savedInstanceState.getInt(STATE_PROGRESS_TOTAL, PROGRESS_UNKNOWN);
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.dialog_progress_bar, null);

        mProgressBar = view.findViewById(R.id.view_progress_bar);
        mPercentTextView = view.findViewById(R.id.view_progress_percent);
        mAbsoluteTextView = view.findViewById(R.id.view_progress_absolute);

        return new AlertDialog.Builder(getContext())
                .setTitle(titleResId)
                .setView(view)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onProgressBarDialogOk(getFragmentTag());
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onProgressBarDialogCancel(getFragmentTag());
                    }
                })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // The assignment has to be done here, because buttons are only available after the dialog
        // has been created
        AlertDialog dialog = (AlertDialog) getDialog();
        mPositiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        mNegativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
    }

    @Override
    public void onResume() {
        super.onResume();

        // If progress wasn't set yet: Init progress bar and buttons
        if (mProgressCurrent == PROGRESS_UNKNOWN && mProgressTotal == PROGRESS_UNKNOWN) {
            initProgressBar();
            initButtons();
        // Otherwise: Update progress bar and buttons
        } else {
            updateProgressBar(mProgressCurrent, mProgressTotal);
            updateButtonStates(mProgressCurrent, mProgressTotal);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_PROGRESS_FORMAT, mProgressFormatAbsolute);
        outState.putInt(STATE_PROGRESS_CURRENT, mProgressCurrent);
        outState.putInt(STATE_PROGRESS_TOTAL, mProgressTotal);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        // If progress wasn't set yet or progress wasn't completed: Handle as cancel
        if (mProgressCurrent == PROGRESS_UNKNOWN || mProgressTotal == PROGRESS_UNKNOWN ||
                mProgressCurrent != mProgressTotal) {
            mListener.onProgressBarDialogCancel(getFragmentTag());
        // Otherwise: Handle as OK
        } else {
            mListener.onProgressBarDialogOk(getFragmentTag());
        }
    }

    private String getFragmentTag() {
        String tag = getTag();
        return tag != null ? tag : "";
    }

    // --- Methods called from the activity ---

    public void updateProgress(int current, int total) {
        // Update progress state
        mProgressCurrent = current;
        mProgressTotal = total;

        // If the dialog fragment hasn't been added yet: Abort
        if (!isAdded()) {
            return;
        }

        // Update progress bar and buttons
        updateProgressBar(mProgressCurrent, mProgressTotal);
        updateButtonStates(mProgressCurrent, mProgressTotal);
    }

    // --- Factory methods ---

    public static ProgressBarDialogFragment newInstance(int titleResId) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_TITLE, titleResId);
        ProgressBarDialogFragment fragment = new ProgressBarDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // --- Helper methods ---

    private void initProgressBar() {
        mProgressBar.setIndeterminate(true);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(1);
        mPercentTextView.setText(FORMAT_PERCENT_DEFAULT);
        mAbsoluteTextView.setText(FORMAT_ABSOLUTE_DEFAULT);
    }

    private void updateProgressBar(int current, int total) {
        mProgressBar.setIndeterminate(false);
        if (current == 0 && total == 0) {
            mProgressBar.setProgress(1);
            mProgressBar.setMax(1);
        } else {
            mProgressBar.setProgress(current);
            mProgressBar.setMax(total);
        }
        mPercentTextView.setText(String.format(FORMAT_PERCENT, calculatePercentage(current, total)));
        mAbsoluteTextView.setText(String.format(mProgressFormatAbsolute, current, total));
    }

    private static float calculatePercentage(int current, int total) {
        if (current == 0 && total == 0) {
            return 100.0f;
        }
        return ((float) current / (float) total) * 100;
    }

    private void initButtons() {
        mPositiveButton.setVisibility(View.GONE);
        mNegativeButton.setVisibility(View.VISIBLE);
    }

    private void updateButtonStates(int current, int total) {
        if (current == total) {
            mPositiveButton.setVisibility(View.VISIBLE);
            mNegativeButton.setVisibility(View.GONE);
        }
    }

}
