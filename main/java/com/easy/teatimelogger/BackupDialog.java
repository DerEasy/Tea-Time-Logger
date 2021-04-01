package com.easy.teatimelogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import static com.easy.teatimelogger.SettingsActivity.pSettingsActivityRef;

public class BackupDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Backup Database")
                .setMessage("Do you really want to backup the app database? This action will overwrite any previous backup.")
                .setNegativeButton("No", (dialog, which) -> {})
                .setPositiveButton("Yes", (dialog, which) -> pSettingsActivityRef.get().backup());

        return builder.create();
    }
}