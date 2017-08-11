package ru.furman.smartnotes.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import ru.furman.smartnotes.R;

public class DeleteNoteDialogFragment extends DialogFragment {

    public interface DeleteNoteDialogFragmentListener {
        void deleteNote();
    }

    private DeleteNoteDialogFragmentListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof DeleteNoteDialogFragmentListener){
            listener = (DeleteNoteDialogFragmentListener) context;
        }
        else
            throw new ClassCastException(context.toString()
                    + " must implement DeleteNoteDialogFragmentListener");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_note_confirmation)
                .setPositiveButton(R.string.to_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.deleteNote();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

}

