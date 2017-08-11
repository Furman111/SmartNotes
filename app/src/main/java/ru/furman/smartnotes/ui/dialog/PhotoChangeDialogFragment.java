package ru.furman.smartnotes.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import ru.furman.smartnotes.R;

public class PhotoChangeDialogFragment extends DialogFragment {

    private PhotoChangeDialogFragmentListener listener;

    public interface PhotoChangeDialogFragmentListener{
        void deletePhoto();
        void showPhoto();
        void changePhoto();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof PhotoChangeDialogFragmentListener){
            listener = (PhotoChangeDialogFragmentListener) context;
        }
        else
            throw new ClassCastException(context.toString()
                    + " must implement PhotoChangeDialogFragmentListener");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.photo))
                .setItems(R.array.photo_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                listener.showPhoto();
                                return;
                            case 1:
                                listener.changePhoto();
                                return;
                            case 2:
                                listener.deletePhoto();
                                return;
                        }
                    }
                });

        return builder.create();
    }

}
