package ru.furman.smartnotes.editnote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import ru.furman.smartnotes.R;

public class PhotoPickerDialogFragment extends DialogFragment {

    private PhotoPickerDialogListener listener;

    public interface PhotoPickerDialogListener {
        void pickPhotoWithCamera();

        void pickPhotoFromGallery();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof PhotoPickerDialogFragment.PhotoPickerDialogListener) {
            listener = (PhotoPickerDialogListener) context;
        } else
            throw new ClassCastException(context.toString()
                    + " must implement PhotoPickerDialogListener");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.add_photo))
                .setItems(R.array.photo_picker_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1:
                                listener.pickPhotoFromGallery();
                                return;
                            case 0:
                                listener.pickPhotoWithCamera();
                                return;
                        }
                    }
                });
        return builder.create();
    }
}
