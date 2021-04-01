package com.easy.teatimelogger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.easy.teatimelogger.DeletePicker.pDeletePickerRef;
import static com.easy.teatimelogger.EditPicker.pEditPickerRef;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private Context pContext;
    private Cursor pCursor;
    private final int pLayout;

    public LogAdapter(Context context, int layoutID, Cursor cursor) {
        /*
        layoutID:
        0 = log_item
        1 = delete_picker
        2 = edit_picker
        */
        if (layoutID < 0 || layoutID > 2) {
            throw new IllegalArgumentException("layoutID outside of accepted range. Refer to LogAdapter class.");
        }

        pContext = context;
        pCursor = cursor;
        pLayout = layoutID;
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {
        public TextView dateText, sessionMinutesText, sessionSecondsText;;
        public CheckBox deleteCheckbox;
        public RadioButton radioButton;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);

            dateText = itemView.findViewById(R.id.textLogDate);
            sessionMinutesText = itemView.findViewById(R.id.textLogSessionMinutes);
            sessionSecondsText = itemView.findViewById(R.id.textLogSessionSeconds);
            if (pLayout == 1) deleteCheckbox = itemView.findViewById(R.id.deleteCheckbox);
            if (pLayout == 2) radioButton = itemView.findViewById(R.id.editRadioButton);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(pContext);
        View view = null;

        if (pLayout == 0) {
            view = inflater.inflate(R.layout.log_item, parent, false);
        } else if (pLayout == 1) {
            view = inflater.inflate(R.layout.delete_picker, parent, false);
        } else if (pLayout == 2) {
            view = inflater.inflate(R.layout.edit_picker, parent, false);
        }

        return new LogViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        if (!pCursor.moveToPosition(position)) return;

        String date = pCursor.getString(pCursor.getColumnIndex(LogContract.LogEntry.COLUMN_DATE));
        int sessionMinutes = pCursor.getInt(pCursor.getColumnIndex(LogContract.LogEntry.COLUMN_MINUTES));
        int sessionSeconds = pCursor.getInt(pCursor.getColumnIndex(LogContract.LogEntry.COLUMN_SECONDS));

        StringBuilder dateBuilt = new StringBuilder(date);
        dateBuilt.delete(0, 11);
        date = String.valueOf(dateBuilt);

        if (pLayout == 0) {
            holder.dateText.setText("Time:   " + date);
            if (sessionMinutes / 10 == 0) {
                holder.sessionMinutesText.setText("Duration:   0" + sessionMinutes + ":");
            } else {
                holder.sessionMinutesText.setText("Duration:   " + sessionMinutes + ":");
            }
        } else {
            holder.dateText.setText(date);
            if (sessionMinutes / 10 == 0) {
                holder.sessionMinutesText.setText("0" + sessionMinutes + ":");
            } else {
                holder.sessionMinutesText.setText("" + sessionMinutes + ":");
            }
        }

        if (sessionSeconds / 10 == 0) {
            holder.sessionSecondsText.setText("0" + sessionSeconds);
        } else {
            holder.sessionSecondsText.setText(String.valueOf(sessionSeconds));
        }

        if (pLayout == 1) {
            holder.deleteCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isChecked()) pDeletePickerRef.get().selectedEntries[holder.getAdapterPosition()] = true;
                else pDeletePickerRef.get().selectedEntries[holder.getAdapterPosition()] = false;
            });
        }

        if (pLayout == 2) {
            holder.radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isChecked()) {
                    pEditPickerRef.get().selectedEntry = holder.getAdapterPosition();
                    pEditPickerRef.get().executeEdit();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return pCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (pCursor != null) pCursor.close();

        pCursor = newCursor;

        if (newCursor != null) notifyDataSetChanged();
    }
}