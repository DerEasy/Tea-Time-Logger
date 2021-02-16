package com.easy.teatimelogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import static com.easy.teatimelogger.MainActivity.pMainActivityRef;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat pSwitchResetButton, pSwitchDeleteButton;
    private Button pButtonConfirmGoal;
    private EditText pEditGoalMinutes;
    private TextView pTextCurrentGoal;
    private int currentGoal;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String RESET_DIALOG = "reset";
    public static final String DELETE_DIALOG = "delete";
    public static final String GOAL_MINUTES = "goal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pSwitchResetButton = (SwitchCompat) findViewById(R.id.switchResetDialog);
        pSwitchDeleteButton = (SwitchCompat) findViewById(R.id.switchDeleteDialog);
        pButtonConfirmGoal = (Button) findViewById(R.id.buttonConfirmGoal);
        pEditGoalMinutes = (EditText) findViewById(R.id.editGoalMinutes);
        pTextCurrentGoal = (TextView) findViewById(R.id.textCurrentGoal);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        pSwitchResetButton.setChecked(sharedPreferences.getBoolean(RESET_DIALOG, true));
        pSwitchDeleteButton.setChecked(sharedPreferences.getBoolean(DELETE_DIALOG, true));
        pTextCurrentGoal.setText("Current goal: " + sharedPreferences.getInt(GOAL_MINUTES, 60) + " minutes");
        if (sharedPreferences.getInt(GOAL_MINUTES, 60) == 1) pTextCurrentGoal.setText("Current goal: " + sharedPreferences.getInt(GOAL_MINUTES, 60) + " minute");
        if (sharedPreferences.getInt(GOAL_MINUTES, 60) == 0) pTextCurrentGoal.setText("Progress bar disabled.");

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

        pButtonConfirmGoal.setOnClickListener(pButtonConfirmGoal -> {
            int editValueInt = 0;
            String editValueString = String.valueOf(pEditGoalMinutes.getText());
            if (editValueString.length() > 0)  editValueInt = Integer.parseInt(editValueString);
            if (editValueInt > 0 && editValueInt <= 1440) {
                currentGoal = editValueInt;
                pTextCurrentGoal.setText(String.format(Locale.getDefault(),"Current goal: %d minutes", currentGoal));
                if (currentGoal == 1) pTextCurrentGoal.setText(String.format(Locale.getDefault(),"Current goal: %d minute", currentGoal));
                editor.putInt(GOAL_MINUTES, editValueInt);
                editor.apply();
                pMainActivityRef.get().loadGoalData();
                pMainActivityRef.get().setProgress();
            } else if (editValueInt > 1440) {
                pTextCurrentGoal.setText("A day only contains 1440 minutes.");
            } else if (editValueInt == 0) {
                currentGoal = editValueInt;
                pTextCurrentGoal.setText("Progress bar disabled.");
                editor.putInt(GOAL_MINUTES, editValueInt);
                editor.apply();
                pMainActivityRef.get().loadGoalData();
                pMainActivityRef.get().setProgress();
            } else {
                pTextCurrentGoal.setText("Invalid input.");
            }
            pMainActivityRef.get().setProgressBarVisibility();
        });
    }
}