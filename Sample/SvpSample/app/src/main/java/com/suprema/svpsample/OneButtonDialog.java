package com.suprema.svpsample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class OneButtonDialog extends DialogFragment {

    static  final String TAG = "OneButtonDialog";

    static final int FINGER_DIALOG = 1;
    static final int CARD_DIALOG = 2;

    public static OneButtonDialog newButtonDialog(int title, int mode) {
        OneButtonDialog frag = new OneButtonDialog( );

        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("mode", mode);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG,"destroyView");
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.i(TAG,"Dismiss..");
        super.onDismiss(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        int mode = getArguments().getInt("mode");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        switch (mode)
        {
            case FINGER_DIALOG:
                builder.setMessage(title).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SVP.manager.cancelScanFingerprint();
                    }
                });
                break;
            case CARD_DIALOG:
                builder.setMessage(title).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SVP.manager.cancelScanCard();
                    }
                });
                break;
        }
        return builder.create();
    }
}
