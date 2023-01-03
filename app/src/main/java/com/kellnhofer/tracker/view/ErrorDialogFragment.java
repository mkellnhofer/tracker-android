package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kellnhofer.tracker.R;

public class ErrorDialogFragment extends DialogFragment {

    private static final String BUNDLE_KEY_TITLE = "title";
    private static final String BUNDLE_KEY_MESSAGE = "message";
    private static final String BUNDLE_KEY_IS_RETRY_ENABLED = "retry";

    public interface Listener {
        void onErrorDialogRetry(String tag);
        void onErrorDialogCancel(String tag);
    }

    private Listener mListener;

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
        int titleResId = arguments.getInt(BUNDLE_KEY_TITLE, R.string.dialog_title_error);
        int messageResId = arguments.getInt(BUNDLE_KEY_MESSAGE, R.string.error_unknown);
        boolean isRetryEnabled = arguments.getBoolean(BUNDLE_KEY_IS_RETRY_ENABLED, false);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(titleResId)
                .setMessage(messageResId);
        if (isRetryEnabled) {
            builder.setPositiveButton(R.string.action_retry, (d, id) ->
                    mListener.onErrorDialogRetry(getFragmentTag()));
            builder.setNegativeButton(R.string.action_cancel, (d, id) ->
                    mListener.onErrorDialogCancel(getFragmentTag()));
        } else {
            builder.setNegativeButton(R.string.action_ok, (d, id) ->
                    mListener.onErrorDialogCancel(getFragmentTag()));
        }

        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onErrorDialogCancel(getFragmentTag());
    }

    private String getFragmentTag() {
        String tag = getTag();
        return tag != null ? tag : "";
    }

    // --- Factory methods ---

    public static ErrorDialogFragment newInstance(int titleResId, int messageResId,
            boolean retryEnabled) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_TITLE, titleResId);
        args.putInt(BUNDLE_KEY_MESSAGE, messageResId);
        args.putBoolean(BUNDLE_KEY_IS_RETRY_ENABLED, retryEnabled);
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

}