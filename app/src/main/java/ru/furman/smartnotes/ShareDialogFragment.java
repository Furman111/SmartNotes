package ru.furman.smartnotes;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class ShareDialogFragment extends DialogFragment {

    public interface ShareDialogListener{
        void shareVK();
        void shareFB();
        void shareTwitter();
    }

    private ShareDialogListener listener;

    public void setListener(ShareDialogListener listener){
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getResources().getString(R.string.to_share));
        builder.setItems(R.array.share_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        listener.shareVK();
                        break;
                    case 1:
                        listener.shareFB();
                        break;
                    case 2:
                        listener.shareTwitter();
                        break;
                }
            }
        });

        return builder.create();
    }
}
