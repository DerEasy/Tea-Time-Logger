package com.easy.teatimelogger;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

import static com.easy.teatimelogger.DeletePicker.getTodayID;
import static com.easy.teatimelogger.LogActivity.getTodayItems;
import static com.easy.teatimelogger.LogActivity.pLogActivityRef;
import static com.easy.teatimelogger.MainActivity.pMainActivityRef;

public class EditPicker extends AppCompatDialogFragment {
    public static WeakReference<EditPicker> pEditPickerRef;
    private RecyclerView recyclerView;
    private LogAdapter pAdapter;
    public int[] listOfEntries = new int[pMainActivityRef.get().pAdapter.getItemCount()];
    public int selectedEntry;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        updateEditPicker(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View delView = inflater.inflate(R.layout.picker_recycler, null);

        recyclerView = delView.findViewById(R.id.pickerRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        pAdapter = new LogAdapter(getActivity(),2, getTodayItems(
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
                .setTitle("Pick Entry to Edit")
                .setNegativeButton("Cancel", ((dialog, which) -> {})
                );

        return builder.create();
    }

    private void updateEditPicker(EditPicker editPicker) {
        pEditPickerRef = new WeakReference<>(editPicker);
    }

    public void executeEdit() {
        EditDialog editDialog = new EditDialog(listOfEntries[selectedEntry]);
        editDialog.show(getFragmentManager(), "Edit Dialog");
        dismiss();
    }
}