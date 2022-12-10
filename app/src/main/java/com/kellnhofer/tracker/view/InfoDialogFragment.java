package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kellnhofer.tracker.R;

public class InfoDialogFragment extends DialogFragment {

    private static final String BUNDLE_KEY_TITLE = "title";
    private static final String BUNDLE_KEY_MESSAGE = "message";

    public interface Listener {
        void onInfoDialogOk(String tag);
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

        return new AlertDialog.Builder(getContext())
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.action_ok, (d, id) ->
                        mListener.onInfoDialogOk(getFragmentTag())).create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // The initialization has to be done here, because the message view is only available after
        // the dialog has been created
        AlertDialog dialog = (AlertDialog) getDialog();
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onInfoDialogOk(getFragmentTag());
    }

    private String getFragmentTag() {
        String tag = getTag();
        return tag != null ? tag : "";
    }

    // --- Factory methods ---

    public static InfoDialogFragment newInstance(int titleResId, int messageResId) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_TITLE, titleResId);
        args.putInt(BUNDLE_KEY_MESSAGE, messageResId);
        InfoDialogFragment fragment = new InfoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

}