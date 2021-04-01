package com.easy.teatimelogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import static com.easy.teatimelogger.DeletePicker.pDeletePickerRef;
import static com.easy.teatimelogger.LogActivity.pLogActivityRef;

public class DeleteDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Entry Deletion Notice")
                .setMessage("Do you really want to irreversibly delete the selected entries?")
                .setNegativeButton("No", (dialog, which) -> {})
                .setPositiveButton("Yes", (dialog, which) -> {
                    pDeletePickerRef.get().delete();
                    if (pDeletePickerRef.get().counter != 1)
                        Toast.makeText(getActivity(), "Deleted " + pDeletePickerRef.get().counter + " entries.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Deleted " + pDeletePickerRef.get().counter + " entry.", Toast.LENGTH_SHORT).show();
                });

        return builder.create();
    }
}