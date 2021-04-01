package com.easy.teatimelogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import static com.easy.teatimelogger.SettingsActivity.pSettingsActivityRef;

public class RestoreDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Restore Database")
                .setMessage("Do you really want to overwrite the app database with your backup? This action cannot be undone.")
                .setNegativeButton("No", (dialog, which) -> {})
                .setPositiveButton("Yes", (dialog, which) -> pSettingsActivityRef.get().restore());

        return builder.create();
    }
}