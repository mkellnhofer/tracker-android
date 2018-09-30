package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.kellnhofer.tracker.R;

public class DecisionDialogFragment extends DialogFragment {

    public static final String BUNDLE_KEY_TITLE_TEXT_ID = "title_text_id";
    public static final String BUNDLE_KEY_MESSAGE_TEXT_ID = "message_text_id";
    public static final String BUNDLE_KEY_ACTION_TEXT_ID = "action_text_id";

    public interface Listener {
        void onDecisionDialogOk(String tag);
        void onDecisionDialogCancel(String tag);
    }

    private Listener mListener;

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
        int titleTextId = arguments.getInt(BUNDLE_KEY_TITLE_TEXT_ID, 0);
        int messageTextId = arguments.getInt(BUNDLE_KEY_MESSAGE_TEXT_ID, 0);
        int actionTextId = arguments.getInt(BUNDLE_KEY_ACTION_TEXT_ID, 0);

        return new AlertDialog.Builder(getContext())
                .setTitle(titleTextId)
                .setMessage(messageTextId)
                .setPositiveButton(actionTextId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDecisionDialogOk(getFragmentTag());
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDecisionDialogCancel(getFragmentTag());
                    }
                }).create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onDecisionDialogCancel(getFragmentTag());
    }

    private String getFragmentTag() {
        String tag = getTag();
        return tag != null ? tag : "";
    }

    // --- Factory methods ---

    public static DecisionDialogFragment newInstance(int titleTextId, int messageTextId,
            int actionTextId) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_TITLE_TEXT_ID, titleTextId);
        args.putInt(BUNDLE_KEY_MESSAGE_TEXT_ID, messageTextId);
        args.putInt(BUNDLE_KEY_ACTION_TEXT_ID, actionTextId);
        DecisionDialogFragment fragment = new DecisionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
