package com.adgad.kboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by arjun on 03/01/16.
 */
public class AddWordDialogFragment extends DialogFragment {


    public interface AddWordDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int index);
        public void onDialogNegativeClick(DialogFragment dialog, int index);
    }


    // Use this instance of the interface to deliver action events
    AddWordDialogListener mListener;

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
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setTitle("Edit Key")
                .setPositiveButton(index > -1 ? "OK" : "Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(AddWordDialogFragment.this, index);
                    }
                })
                .setNegativeButton(index > -1 ? "Delete" : "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AddWordDialogFragment.this, index);
                    }
                });
        return builder.create();
    }
}