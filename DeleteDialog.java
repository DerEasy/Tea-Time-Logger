package com.easy.teatimelogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import static com.easy.teatimelogger.LogActivity.pLogActivityRef;

public class DeleteDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Entry Deletion Notice")
                .setMessage("Do you really want to irreversibly delete the latest entry?")
                .setNegativeButton("No", (dialog, which) -> {})
                .setPositiveButton("Yes", (dialog, which) -> pLogActivityRef.get().delete(null));

        return builder.create();
    }
}