package com.example.nikhil.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

public class AlertDialogFragement extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.error_title).setMessage(R.string.error_message).setPositiveButton(R.string.error_button_ok_text,null);
        return builder.create();
    }
}
