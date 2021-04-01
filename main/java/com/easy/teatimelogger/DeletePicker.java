package com.easy.teatimelogger;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.easy.teatimelogger.LogActivity.getTodayItems;
import static com.easy.teatimelogger.LogActivity.pLogActivityRef;
import static com.easy.teatimelogger.MainActivity.pDatabase;
import static com.easy.teatimelogger.MainActivity.pMainActivityRef;

public class DeletePicker extends AppCompatDialogFragment {
    public static WeakReference<DeletePicker> pDeletePickerRef;
    private RecyclerView recyclerView;
    private LogAdapter pAdapter;
    public int[] listOfEntries = new int[pMainActivityRef.get().pAdapter.getItemCount()];
    public boolean[] selectedEntries = new boolean[listOfEntries.length];
    private final ArrayList<Boolean> finalisedEntries = new ArrayList<>();
    int counter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        updateDeletePicker(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View delView = inflater.inflate(R.layout.picker_recycler, null);

        recyclerView = delView.findViewById(R.id.pickerRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        pAdapter = new LogAdapter(getActivity(),1, getTodayItems(
                String.valueOf(pLogActivityRef.get().userYear),
                String.valueOf(pLogActivityRef.get().userMonth),
                String.valueOf(pLogActivityRef.get().userDay))
        );

        recyclerView.setAdapter(pAdapter);

        Cursor idCursor = getTodayID(
                String.valueOf(pLogActivityRef.get().userYear),
                String.valueOf(pLogActivityRef.get().userMonth),
                String.valueOf(pLogActivityRef.get().userDay)
        );

        idCursor.moveToFirst();
        for (int i = 0; i < pAdapter.getItemCount(); i++) {
            listOfEntries[i] = idCursor.getInt(idCursor.getColumnIndex("_id"));
            idCursor.moveToNext();
        }

        builder.setView(delView)
                .setTitle("Pick Entries to Delete")
                .setNegativeButton("Cancel", ((dialog, which) -> {}))
                .setPositiveButton("Delete", ((dialog, which) -> {
                    for (int selectedEntry = 0; selectedEntry < selectedEntries.length; selectedEntry++) {
                        finalisedEntries.add(selectedEntries[selectedEntry]);
                        if (finalisedEntries.get(selectedEntry)) counter++;
                    } if (counter == 0) {
                        Toast.makeText(getActivity(), "None selected.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pLogActivityRef.get().loadSwitchData();
                    if (!pLogActivityRef.get().savedDeleteIsChecked) {
                        delete();
                        if (counter != 1)
                            Toast.makeText(getActivity(), "Deleted " + counter + " entries.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), "Deleted " + counter + " entry.", Toast.LENGTH_SHORT).show();
                    } else openDialog();
                }));

        return builder.create();
    }

    public static Cursor getTodayID(String year, String month, String dayOfMonth) {
        if(month.length() == 1) month = "0" + month;
        if(dayOfMonth.length() == 1) dayOfMonth = "0" + dayOfMonth;
        return pDatabase.query(
                LogContract.LogEntry.TABLE_NAME,
                new String[]{"_id"},
                String.format("date(date_time) = '%s-%s-%s'", year, month, dayOfMonth),
                null,
                null,
                null,
                LogContract.LogEntry.COLUMN_DATE + " DESC"
        );
    }

    /**
     * Opens confirmation dialog
     */
    private void openDialog() {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.show(getFragmentManager(), "Delete Entry Dialog");
    }

    public void delete() {
        for (int position = 0; position < listOfEntries.length; position++) {
            if (finalisedEntries.get(position)) {
                pDatabase.delete("tealog", "_id = " + listOfEntries[position], null);
            }
        }
        pLogActivityRef.get().updateOnEdit();
    }

    private void updateDeletePicker(DeletePicker deleteActivity) {
        pDeletePickerRef = new WeakReference<>(deleteActivity);
    }
}