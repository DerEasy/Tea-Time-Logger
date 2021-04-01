package com.easy.teatimelogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import static com.easy.teatimelogger.MainActivity.*;

public class ResetDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Reset Session Notice")
                .setMessage("Do you really want to reset the session? Session data will be lost.")
                .setNegativeButton("No", (dialog, which) -> {})
                .setPositiveButton("Yes", (dialog, which) -> pMainActivityRef.get().resetChronometer(null));

        return builder.create();
    }
}