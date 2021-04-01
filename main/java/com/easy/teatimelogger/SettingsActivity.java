package com.easy.teatimelogger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static com.easy.teatimelogger.MainActivity.pDatabase;
import static com.easy.teatimelogger.MainActivity.pMainActivityRef;
import static com.easy.teatimelogger.Notification.CHANNEL_REMINDER;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat pSwitchResetButton, pSwitchDeleteButton;
    private Button pButtonConfirmGoal;
    private ImageButton pButtonBackup, pButtonRestore;
    private EditText pEditGoalMinutes;
    private NotificationManagerCompat notifManager;
    public static WeakReference<SettingsActivity> pSettingsActivityRef;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String RESET_DIALOG = "reset";
    public static final String DELETE_DIALOG = "delete";
    public static final String GOAL_MINUTES = "goal";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        updateSettingsActivity(this);
        pSwitchResetButton = (SwitchCompat) findViewById(R.id.switchResetDialog);
        pSwitchDeleteButton = (SwitchCompat) findViewById(R.id.switchDeleteDialog);
        pButtonConfirmGoal = (Button) findViewById(R.id.buttonConfirmGoal);
        pEditGoalMinutes = (EditText) findViewById(R.id.editGoalMinutes);
        pButtonBackup = (ImageButton) findViewById(R.id.imageButtonBackup);
        pButtonRestore = (ImageButton) findViewById(R.id.imageButtonRestore);
        notifManager = NotificationManagerCompat.from(this);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Loads all data associated with SettingsActivity from SharedPreferences
        pSwitchResetButton.setChecked(sharedPreferences.getBoolean(RESET_DIALOG, true));
        pSwitchDeleteButton.setChecked(sharedPreferences.getBoolean(DELETE_DIALOG, true));
        if (sharedPreferences.getInt(GOAL_MINUTES, 60) == 1) pEditGoalMinutes.setHint("Currently: 1 minute");
        else pEditGoalMinutes.setHint("Currently: " + sharedPreferences.getInt(GOAL_MINUTES, 60) + " minutes");
        if (sharedPreferences.getInt(GOAL_MINUTES, 60) == 0) pEditGoalMinutes.setHint("Disabled");

        //Lambda function
        //Saves switch boolean value of Session Reset confirmation dialog
        pSwitchResetButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (pSwitchResetButton.isChecked()) {
                editor.putBoolean(RESET_DIALOG, true);
                editor.apply();
                pSwitchResetButton.setChecked(true);
            } else {
                editor.putBoolean(RESET_DIALOG, false);
                editor.apply();
                pSwitchResetButton.setChecked(false);
            }
        });

        //Lambda function
        //Saves switch boolean value of Entry Deletion confirmation dialog
        pSwitchDeleteButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (pSwitchDeleteButton.isChecked()) {
                editor.putBoolean(DELETE_DIALOG, true);
                editor.apply();
                pSwitchDeleteButton.setChecked(true);
            } else {
                editor.putBoolean(DELETE_DIALOG, false);
                editor.apply();
                pSwitchDeleteButton.setChecked(false);
            }
        });

        //Lambda function
        //Ensures proper handling of all possible inputs in the goal EditText,
        //then updates all variables and displays and saves to SharedPreferences
        pButtonConfirmGoal.setOnClickListener(pButtonConfirmGoal -> {
            int editValueInt = 0;
            String editValueString = String.valueOf(pEditGoalMinutes.getText());
            if (editValueString.length() > 0)  editValueInt = Integer.parseInt(editValueString);

            if (editValueInt != sharedPreferences.getInt(GOAL_MINUTES, 60)) {
                if (editValueInt > 0 && editValueInt <= 1440) {
                    editor.putInt(GOAL_MINUTES, editValueInt);
                    editor.apply();
                    pMainActivityRef.get().loadGoalData();
                    if (sharedPreferences.getInt(GOAL_MINUTES, 60) == 1) pEditGoalMinutes.setHint("Currently: 1 minute");
                    else pEditGoalMinutes.setHint("Currently: " + sharedPreferences.getInt(GOAL_MINUTES, 60) + " minutes");
                    Toast.makeText(this, "Goal duration updated.", Toast.LENGTH_SHORT).show();
                } else if (editValueInt > 1440) {
                    Toast.makeText(this, "A day only contains 1440 minutes.", Toast.LENGTH_SHORT).show();
                } else if (editValueInt == 0) {
                    editor.putInt(GOAL_MINUTES, editValueInt);
                    editor.apply();
                    pMainActivityRef.get().loadGoalData();
                    pEditGoalMinutes.setHint("Disabled");
                    Toast.makeText(this, "Progress bar disabled.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "An error occurred.", Toast.LENGTH_LONG).show();
                }
                pMainActivityRef.get().setProgress();
                pMainActivityRef.get().setProgressBarVisibility();
            }
        });

        pButtonBackup.setOnClickListener(pButtonBackup -> {
            verifyStoragePermissions(this);
            BackupDialog backupDialog = new BackupDialog();
            backupDialog.show(getSupportFragmentManager(), "Backup Dialog");
        });

        pButtonRestore.setOnClickListener(pButtonRestore -> {
            verifyStoragePermissions(this);
            RestoreDialog restoreDialog = new RestoreDialog();
            restoreDialog.show(getSupportFragmentManager(), "Restore Dialog");
        });
    }

    /*public void sendReminderNotif() {
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        android.app.Notification notification = new NotificationCompat.Builder(this, CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.ttl_icon)
                .setContentTitle("Tea Time")
                .setContentText("It's Tea Time!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();

        notifManager.notify(0, notification);
    }*/

    private void updateSettingsActivity(SettingsActivity settingsActivity) {
        pSettingsActivityRef = new WeakReference<>(settingsActivity);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        File f = new File(Environment.getExternalStorageDirectory(), "TeaTimeLogger Backup");
        if (!f.exists()) f.mkdirs();
    }

    public void backup() {
        File f = new File(Environment.getExternalStorageDirectory(), "TeaTimeLogger Backup");
        if (!f.exists()) f.mkdirs();
        try {
            backupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Backup failed. You must allow the app access to the device storage.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Backup failed. Error unknown.", Toast.LENGTH_LONG).show();
        }
    }

    public void restore() {
        try {
            restoreDatabase();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Restoration failed. Check if the file is in the correct directory and not renamed.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Restoration failed. Error unknown.", Toast.LENGTH_LONG).show();
        }
    }

    private void backupDatabase() throws IOException {
        final String inFileName = getApplicationContext().getDatabasePath("teatimelog.db").getPath();
        File dbFile = new File(inFileName);
        FileInputStream fis = new FileInputStream(dbFile);

        String outFileName = Environment.getExternalStorageDirectory() + "/TeaTimeLogger Backup/teatimelog.db";

        OutputStream output = new FileOutputStream(outFileName);

        //Transfer Bytes from the input to output
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        fis.close();
        System.out.println(pDatabase.getPath());
        Toast.makeText(this, "Backup successfully created in\n/TeaTimeLogger Backup/teatimelog.db\nKeep it there to restore it later.", Toast.LENGTH_LONG).show();
    }

    private void restoreDatabase() throws IOException {
        final String inFileName = Environment.getExternalStorageDirectory() + "/TeaTimeLogger Backup/teatimelog.db";
        File dbFile = new File(inFileName);
        FileInputStream fis = new FileInputStream(dbFile);

        String outFileName = getApplicationContext().getDatabasePath("teatimelog.db").getPath();

        OutputStream output = new FileOutputStream(outFileName);

        //Transfer Bytes from the input to output
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        fis.close();
        Toast.makeText(this, "Database successfully restored.", Toast.LENGTH_LONG).show();
    }

    private void verifyStoragePermissions(Activity activity) {
        //Check if write permission is enabled
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //Prompt the user for permission
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}