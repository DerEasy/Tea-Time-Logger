package com.easy.teatimelogger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private Context pContext;
    private Cursor pCursor;

    public LogAdapter(Context context, Cursor cursor) {
        pContext = context;
        pCursor = cursor;
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {
        public TextView dateText, sessionMinutesText, sessionSecondsText;;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);

            dateText = itemView.findViewById(R.id.textLogDate);
            sessionMinutesText = itemView.findViewById(R.id.textLogSessionMinutes);
            sessionSecondsText = itemView.findViewById(R.id.textLogSessionSeconds);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(pContext);
        View view = inflater.inflate(R.layout.log_item, parent, false);
        return new LogViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        if (!pCursor.moveToPosition(position)) return;

        String date = pCursor.getString(pCursor.getColumnIndex(LogContract.LogEntry.COLUMN_DATE));
        int sessionMinutes = pCursor.getInt(pCursor.getColumnIndex(LogContract.LogEntry.COLUMN_MINUTES));
        int sessionSeconds = pCursor.getInt(pCursor.getColumnIndex(LogContract.LogEntry.COLUMN_SECONDS));

        holder.dateText.setText(date);

        if (sessionMinutes / 10 == 0) {
            holder.sessionMinutesText.setText("Tea Time:     0" + sessionMinutes + ":");
        } else {
            holder.sessionMinutesText.setText("Tea Time:     " + sessionMinutes + ":");
        }

        if (sessionSeconds / 10 == 0) {
            holder.sessionSecondsText.setText("0" + sessionSeconds);
        } else {
            holder.sessionSecondsText.setText(String.valueOf(sessionSeconds));
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