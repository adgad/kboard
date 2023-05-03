package com.adgad.kboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by arjun on 03/01/16.
 */
public class AddWordDialogFragment extends DialogFragment {

    private static final String TAG = AddWordDialogFragment.class.getName();


    public interface AddWordDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, int index);
        void onDialogNegativeClick(DialogFragment dialog, int index);
        void onDialogNeutralClick(int index);


    }


    // Use this instance of the interface to deliver action events
    private AddWordDialogListener mListener;

    //Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddWordDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddWordDialog");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        String existingWord = getArguments().getString("word");
        View view = inflater.inflate(R.layout.dialog_new_word, null);
        if(existingWord != null) {
            ((TextView)view.findViewById(R.id.word)).setText(existingWord);
        }
        final int index = getArguments().getInt("index");
        final boolean isEditing = (index > -1);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setTitle(isEditing ? "Edit Key" : "Add Key")
                .setPositiveButton(isEditing ? "OK" : "Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(AddWordDialogFragment.this, index);
                    }
                })
                .setNegativeButton(isEditing ? "Delete" : "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AddWordDialogFragment.this, index);
                    }
                });

        if (isEditing) {
            builder.setNeutralButton("Move up", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onDialogNeutralClick(index);
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            Log.e(TAG, "dialog is null, skipping button disabling");
            return;
        }

        final int index = getArguments().getInt("index");
        final int keyCount = getArguments().getInt("keyCount");
        final boolean isEditing = (index > -1);

        if (isEditing) {
            if (keyCount <= 1) {
                // Disable the "Delete" button, so that the last key will not be deleted
                Button deleteButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                if (deleteButton != null) {
                    deleteButton.setEnabled(false);
                } else {
                    Log.e(TAG, "deleteButton is null, cannot disable");
                }
            }
            if (index == 0) {
                // Disable the "Move up" button, as the topmost key cannot be moved up
                Button moveUpButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                if (moveUpButton != null) {
                    moveUpButton.setEnabled(false);
                } else {
                    Log.e(TAG, "moveUpButton is null, cannot disable");
                }
            }
        }
    }
}