package com.kellnhofer.tracker.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.kellnhofer.tracker.R;

public class CreateDialogFragment extends DialogFragment {

    public interface CreateDialogListener {
        void onCreateDialogOk(String name);
        void onCreateDialogCancel();
    }

    private CreateDialogListener mListener;

    private EditText mNameView;

    public CreateDialogFragment() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            mListener = (CreateDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement " + CreateDialogListener.class.getName() + "!");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.dialog_create, null);

        mNameView = (EditText) view.findViewById(R.id.view_name);

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_dialog_create)
                .setView(view)
                .setPositiveButton(R.string.action_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCreateDialogOk(mNameView.getText().toString().trim());
                        CreateDialogFragment.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        CreateDialogFragment.this.getDialog().cancel();
                    }
                }).create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onCreateDialogCancel();
    }

    public static CreateDialogFragment newInstance() {
        return new CreateDialogFragment();
    }

}