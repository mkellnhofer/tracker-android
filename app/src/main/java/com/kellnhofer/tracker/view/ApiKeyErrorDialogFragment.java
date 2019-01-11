package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.kellnhofer.tracker.R;

public class ApiKeyErrorDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_title_error)
                .setMessage(R.string.dialog_message_missing_api_key)
                .setCancelable(false)
                .create();
    }

    // --- Factory methods ---

    public static ApiKeyErrorDialogFragment newInstance() {
        ApiKeyErrorDialogFragment fragment = new ApiKeyErrorDialogFragment();
        fragment.setCancelable(false);
        return fragment;
    }

}