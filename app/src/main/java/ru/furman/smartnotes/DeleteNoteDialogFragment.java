package ru.furman.smartnotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Furman on 05.08.2017.
 */

public class DeleteNoteDialogFragment extends DialogFragment {

    public interface NoticeDialogListener{
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private NoticeDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof NoticeDialogListener){
            listener = (NoticeDialogListener) context;
        }
        else
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_note_confirmation)
                .setPositiveButton(R.string.to_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(DeleteNoteDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(DeleteNoteDialogFragment.this);
                    }
                });

        return builder.create();
    }

}

