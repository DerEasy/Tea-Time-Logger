package com.easy.teatimelogger;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import static com.easy.teatimelogger.LogActivity.pLogActivityRef;

public class DurationPickerDialog extends AppCompatDialogFragment {
    private NumberPicker hour, minute, second;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View pickedDurationView = inflater.inflate(R.layout.numberpicker_dialog, null);

        hour = pickedDurationView.findViewById(R.id.numberPickerHour);
        minute = pickedDurationView.findViewById(R.id.numberPickerMinute);
        second = pickedDurationView.findViewById(R.id.numberPickerSecond);

        //Sets min and max values of all NumberPickers
        hour.setMinValue(0); hour.setMaxValue(23);
        minute.setMinValue(0); minute.setMaxValue(59);
        second.setMinValue(0); second.setMaxValue(59);

        builder.setView(pickedDurationView)
                .setTitle("Duration")
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .setPositiveButton("Insert", (dialog, which) -> {
                    //Sets variables for insertion to values in DurationPicker Dialog
                    pLogActivityRef.get().durationHour = hour.getValue();
                    pLogActivityRef.get().durationMinute = minute.getValue();
                    pLogActivityRef.get().durationSecond = second.getValue();
                    pLogActivityRef.get().adjustedDurationMinute = pLogActivityRef.get().durationMinute + (pLogActivityRef.get().durationHour * 60);

                    //Checks if the entry is 00:00 minutes long and then inserts the entry if it is not
                    if (pLogActivityRef.get().adjustedDurationMinute != 0 && pLogActivityRef.get().durationSecond != 0
                        || pLogActivityRef.get().adjustedDurationMinute == 0 && pLogActivityRef.get().durationSecond != 0
                        || pLogActivityRef.get().adjustedDurationMinute != 0 && pLogActivityRef.get().durationSecond == 0) {

                        pLogActivityRef.get().insert(null);
                    }
                });

        return builder.create();
    }
}