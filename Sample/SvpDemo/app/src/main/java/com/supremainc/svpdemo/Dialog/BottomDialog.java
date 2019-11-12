package com.supremainc.svpdemo.Dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.supremainc.svpdemo.Activity.AddUserActivity;
import com.supremainc.svpdemo.R;

public class BottomDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    public static BottomDialog getInstance() { return new BottomDialog();}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.botton_sheet, container, false);

        Button updateUser = view.findViewById(R.id.btnUpdate);
        updateUser.setOnClickListener(this);

        Button deleteUser = view.findViewById(R.id.btnDelete);
        deleteUser.setOnClickListener(this);

        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                View bottomSheetInternal = d.findViewById(android.support.design.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnUpdate:
                ((AddUserActivity)getActivity()).changeUiForUpdate();
                dismiss();
                break;

            case R.id.btnDelete:
                showDeleteMsg();
                break;
            }
    }

    public void showDeleteMsg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Notification");
        builder.setMessage("Delete User?");
        builder.setIcon(R.drawable.ic_alert);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((AddUserActivity)getActivity()).deleteUser();
                getActivity().finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
