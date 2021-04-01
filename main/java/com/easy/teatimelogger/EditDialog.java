package com.easy.teatimelogger;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import static com.easy.teatimelogger.LogActivity.pLogActivityRef;
import static com.easy.teatimelogger.MainActivity.pDatabase;
import static com.easy.teatimelogger.MainActivity.pMainActivityRef;

public class EditDialog extends AppCompatDialogFragment {
    private final int id;
    private int mins, secs;
    private TextView pEditMinutes, pEditSeconds;
    private NumberPicker pickerMinutes, pickerSeconds;

    public EditDialog(int id) {
        this.id = id;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View delView = inflater.inflate(R.layout.edit_dialog, null);

        pEditMinutes = delView.findViewById(R.id.textEditMinutes);
        pEditSeconds = delView.findViewById(R.id.textEditSeconds);
        pickerMinutes = delView.findViewById(R.id.editPickerMinute);
        pickerSeconds = delView.findViewById(R.id.editPickerSecond);

        pickerMinutes.setMinValue(0); pickerMinutes.setMaxValue(1439); pickerMinutes.setValue(1);
        pickerSeconds.setMinValue(0); pickerSeconds.setMaxValue(59);

        setDurationByID();

        builder.setView(delView)
                .setTitle("Edit the Entry")
                .setNegativeButton("Cancel", ((dialog, which) -> {}))
                .setPositiveButton("Apply", (((dialog, which) -> {
                    applyChanges();
                    pLogActivityRef.get().updateOnEdit();
                    Toast.makeText(getContext(), "Changes applied.", Toast.LENGTH_SHORT).show();
                }))
                );

        return builder.create();
    }

    private void applyChanges() {
        int updatedMins = pickerMinutes.getValue();
        int updatedSecs = pickerSeconds.getValue();
        ContentValues cv = new ContentValues();
        cv.put(LogContract.LogEntry.COLUMN_MINUTES, updatedMins);
        cv.put(LogContract.LogEntry.COLUMN_SECONDS, updatedSecs);

        pDatabase.update(LogContract.LogEntry.TABLE_NAME,
                cv,
                "_id = " + id,
                null);
    }

    @SuppressLint("SetTextI18n")
    private void setDurationByID() {
        Cursor localCursor = pDatabase.query(LogContract.LogEntry.TABLE_NAME,
                new String[]{"minutes", "seconds"},
                "_id = " + id,
                null,
                null,
                null,
                null);

        localCursor.moveToFirst();
        mins = localCursor.getInt(localCursor.getColumnIndex(LogContract.LogEntry.COLUMN_MINUTES));
        secs = localCursor.getInt(localCursor.getColumnIndex(LogContract.LogEntry.COLUMN_SECONDS));
        localCursor.close();

        if (minDD() & secDD()) {
            pEditMinutes.setText(String.valueOf(mins));
            pEditSeconds.setText(String.valueOf(secs));
        } else if (!minDD() & !secDD()) {
            pEditMinutes.setText("0" + mins);
            pEditSeconds.setText("0" + secs);
        } else if (!minDD() & secDD()) {
            pEditMinutes.setText("0" + mins);
            pEditSeconds.setText(String.valueOf(secs));
        } else if (minDD() & !secDD()) {
            pEditMinutes.setText(String.valueOf(mins));
            pEditSeconds.setText("0" + secs);
        } else {
            pEditMinutes.setText(String.valueOf(mins));
            pEditSeconds.setText(String.valueOf(secs));
        }
    }

    /**
     * Check if minutes have double or more digits
     * @return true if double or more digits
     */
    private boolean minDD() {
        return mins >= 10;
    }

    /**
     * Check if seconds have double digits
     * @return true if double digits
     */
    private boolean secDD() {
        return secs >= 10;
    }
}