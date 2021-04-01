package com.easy.teatimelogger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.util.Calendar;

import static com.easy.teatimelogger.LogActivity.getTodayItems;
import static com.easy.teatimelogger.LogActivity.getTodayValues;
import static com.easy.teatimelogger.SettingsActivity.GOAL_MINUTES;
import static com.easy.teatimelogger.SettingsActivity.RESET_DIALOG;

public class MainActivity extends AppCompatActivity {
    /**
     * SharedPreferences. Used for saving the total time and last session TextView Strings.
     */
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SAVED_SESSION = "session";
    public static final String SAVED_TOTAL = "total";
    public static final String SAVED_TOTAL_INTEGER = "totalseconds";
    private String savedSessionText, savedTotalText;
    private boolean savedResetIsChecked;

    /**
     * Database, Reference to MainActivity instance, Calendar and timer values for all classes
     */
    public static SQLiteDatabase pDatabase;
    public static WeakReference<MainActivity> pMainActivityRef;
    public static Calendar c;
    public int timerSeconds, timerMinutes;

    /**
     * Pause offset to calculate time when session has been paused
     * Session Time in milliseconds and boolean to check if session is running
     */
    private long pauseOffset, sessionTime;
    private boolean running;

    /**
     * Local Date compares calendar, helper view instance to pass button calls and local date to update TextViews if they are of a past date
     */
    public LogAdapter pAdapter;
    public LocalDate localDate;
    public TextView pTotalTimeOnSelectedDay, pTextSessionTime;

    /**
     * Chronometer class instance, Reset button alert dialog pre-requisite and time calculating variables
     */
    private Chronometer chronometer;
    private Button pButtonReset;
    private ProgressBar pGoalProgress;
    private TextView pProgressPercentage, pTextMainGoal;
    private int totalTimeOnSelectedDay, totalSeconds, confSecs, progress, goal;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assigns WeakReference object current class instance
        updateMainActivity(this);

        //Local Date comparison pre-requisite
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDate = LocalDate.now();
        }

        //Assigns variables their corresponding Items in MainActivity and loads SharedPreferences data
        pTotalTimeOnSelectedDay = (TextView) findViewById(R.id.textTotalMinutes);
        pTextSessionTime = (TextView) findViewById(R.id.textSessionTime);
        pButtonReset = (Button) findViewById(R.id.buttonReset);
        pGoalProgress = (ProgressBar) findViewById(R.id.goalChart);
        pProgressPercentage = (TextView) findViewById(R.id.textProgress);
        pTextMainGoal = (TextView) findViewById(R.id.textMainGoal);
        loadTimeData();
        loadSwitchData();
        loadGoalData();
        setProgress();
        setProgressBarVisibility();

        //Sets up database and calls update on Total Time display
        LogDBHelper dbHelper = new LogDBHelper(this);
        pDatabase = dbHelper.getWritableDatabase();
        c = Calendar.getInstance();
        pAdapter = new LogAdapter(this, 0, getTodayItems(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH))));
        callTTOSD();

        chronometer = findViewById(R.id.chronometer);

        //Lambda function
        //Refreshes sessionTime every tick (second)
        chronometer.setOnChronometerTickListener(chronometer -> {
            sessionTime = SystemClock.elapsedRealtime() - chronometer.getBase();
            timerMinutes = (int) sessionTime / 60 / 1000;
            timerSeconds = (int) ((sessionTime / 1000) % 60);
            confSecs = (int) sessionTime / 1000;

            //Refreshes Total Time variable
            if (confSecs > totalSeconds) {
                totalSeconds += (confSecs - totalSeconds);
                callTTOSD();
                totalTimeOnSelectedDay += totalSeconds;
            }

            //Updates Total Time display and Progress bar
            changeTotalTimeText();
            setProgress();
        });

        //Lambda function
        //Displays an alert dialog if it is set in the settings
        pButtonReset.setOnClickListener(pButtonReset -> {
            loadSwitchData();
            if (sessionTime >= 1000) {
                if (savedResetIsChecked) openDialog();
                else resetChronometer(null);
                setProgress();
            }
        });
    }

    /**
     * Ensures that the app will not be killed when user presses back button
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Attempt to save current data and end session if app is killed
     */
    protected void onDestroy() {
        super.onDestroy();
        sessionChronometer(null);
        saveTimeData();
    }

    /**
     * Opens the reset dialog
     */
    public void openDialog() {
        ResetDialog resetDialog = new ResetDialog();
        resetDialog.show(getSupportFragmentManager(), "Reset Session Dialog");
    }

    /**
     * Checks if Total Time minutes have double digits
     * @return true if double digits
     */
    private boolean minutesTotalHaveDoubleDigits() {
        return totalTimeOnSelectedDay / 60 >= 10;
    }

    /**
     * Checks if Total Time seconds have double digits
     * @return true if double digits
     */
    private boolean secondsTotalHaveDoubleDigits() {
        return totalTimeOnSelectedDay % 60 >= 10;
    }

    /**
     * Checks if Session Time minutes have double digits
     * @return true if double digits
     */
    private boolean minutesSessionHaveDoubleDigits() {
        return timerMinutes / 10 > 0;
    }

    /**
     * Checks if Session Time seconds have double digits
     * @return true if double digits
     */
    private boolean secondsSessionHaveDoubleDigits() {
        return timerSeconds % 60 >= 10;
    }

    /**
     * Updates Total Time display with correct formatting
     */
    @SuppressLint("SetTextI18n")
    public void changeTotalTimeText() {
        if (minutesTotalHaveDoubleDigits() && secondsTotalHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesTotalHaveDoubleDigits() && !secondsTotalHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("0" + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesTotalHaveDoubleDigits() && secondsTotalHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("0" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (minutesTotalHaveDoubleDigits() && !secondsTotalHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("" + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else {
            pTotalTimeOnSelectedDay.setText("" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        }
    }

    /**
     * Updates Session Time display with correct formatting
     */
    @SuppressLint("SetTextI18n")
    private void changeSessionTimeText() {
        if (minutesSessionHaveDoubleDigits() && secondsSessionHaveDoubleDigits()) {
            pTextSessionTime.setText("" + timerMinutes + ":" + timerSeconds);
        } if (!minutesSessionHaveDoubleDigits() && !secondsSessionHaveDoubleDigits()) {
            pTextSessionTime.setText("0" + timerMinutes + ":0" + timerSeconds);
        } if (!minutesSessionHaveDoubleDigits() && secondsSessionHaveDoubleDigits()) {
            pTextSessionTime.setText("0" + timerMinutes + ":" + timerSeconds);
        } if (minutesSessionHaveDoubleDigits() && !secondsSessionHaveDoubleDigits()) {
            pTextSessionTime.setText("" + timerMinutes + ":0" + timerSeconds);
        }
    }

    /**
     * Assign class instance to WeakReference object.
     * CRITICAL for app functionality (LogActivity).
     * @param activity must be key word 'this'
     */
    private void updateMainActivity(MainActivity activity) {
        pMainActivityRef = new WeakReference<>(activity);
    }

    /**
     *  Creates new object of class LogActivity and opens the log window
     */
    public void openLog(View v) {
        Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }

    /**
     *  Creates new object of class SettingsActivity and opens the settings window
     */
    public void openSettings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     *  Starts chronometer
     */
    public void startChronometer(View v) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
            changeTotalTimeText();
            setProgress();
            callTTOSD();
            if (sessionTime < 1000) Toast.makeText(this, "Starting...", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Resuming...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  Pauses chronometer
     */
    public void pauseChronometer(View v) {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
            Toast.makeText(this, "Paused.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  Resets chronometer
     */
    public void resetChronometer(View v) {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        chronometer.stop();
        running = false;
        callTTOSD();
        changeTotalTimeText();
        setProgress();
        totalSeconds = 0;
    }

    /**
     *  Ends the current session, writes to database and updates TextViews and saved data
     */
    public void sessionChronometer(View v) {
        //Only execute if session actually has data to save, ergo at least 1 second
        if (sessionTime >= 1000) {
            //Reset Total Time display if session ended in another day than it started in
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!localDate.isEqual(LocalDate.now())) {
                    localDate = LocalDate.now();
                    c = Calendar.getInstance();
                    callTTOSD();
                    changeTotalTimeText();
                }
            }
            totalSeconds = 0;
            LogActivity.addItem();
            changeSessionTimeText();
            saveTimeData();
            changeTotalTimeText();
            resetChronometer(v);
            Toast.makeText(this, "Inserting entry into database...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves data to SharedPreferences
     */
    public void saveTimeData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVED_SESSION, String.valueOf(pTextSessionTime.getText()));
        editor.putString(SAVED_TOTAL, String.valueOf(pTotalTimeOnSelectedDay.getText()));
        editor.putInt(SAVED_TOTAL_INTEGER, totalTimeOnSelectedDay);
        editor.apply();
    }

    /**
     * Loads all data regarding daily goal
     */
    public void loadGoalData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        goal = sharedPreferences.getInt(GOAL_MINUTES, 60);
        goal *= 60;
        pGoalProgress.setMax(goal);
    }

    /**
     * Loads reset switch setting value
     */
    public void loadSwitchData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        savedResetIsChecked = sharedPreferences.getBoolean(RESET_DIALOG, true);
    }

    /**
     * Loads data from SharedPreferences
     */
    private void loadTimeData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        savedSessionText = sharedPreferences.getString(SAVED_SESSION, "NOT AVAILABLE");
        savedTotalText = sharedPreferences.getString(SAVED_TOTAL, "00:00");
        totalTimeOnSelectedDay = sharedPreferences.getInt(SAVED_TOTAL_INTEGER, 0);
        pTextSessionTime.setText(savedSessionText);
        pTotalTimeOnSelectedDay.setText(savedTotalText);
    }

    /**
     * Updates progress bar
     */
    @SuppressLint("SetTextI18n")
    public void setProgress() {
        progress = totalTimeOnSelectedDay;
        pGoalProgress.setProgress(progress);
        if (goal > 0) pProgressPercentage.setText(progress * 100 / goal + "%");
    }

    /**
     * Same as setProgress(), but with a parameter, so LogActivity can call the method properly.
     * This method is used in combination with the callSetProgressOnMain() method in LogActivity.
     */
    @SuppressLint("SetTextI18n")
    public void setLogProgress(int pTotalTimeOnSelectedDay) {
        progress = pTotalTimeOnSelectedDay;
        pGoalProgress.setProgress(progress);
        if (goal > 0) pProgressPercentage.setText(progress * 100 / goal + "%");
    }

    /**
     *  Automatically enables or disables visibility of the progress bar based on
     *  the given criteria, that is, the daily goal value that has been set.
     */
    public void setProgressBarVisibility() {
        if (goal == 0) {
            pGoalProgress.setVisibility(View.INVISIBLE);
            pProgressPercentage.setVisibility(View.INVISIBLE);
            pTextMainGoal.setVisibility(View.INVISIBLE);
        } else {
            pGoalProgress.setVisibility(View.VISIBLE);
            pProgressPercentage.setVisibility(View.VISIBLE);
            pTextMainGoal.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates Total Time of Selected Day (Today) variable
     */
    public void callTTOSD() {
        totalTimeOnSelectedDay = 0;
        Cursor valueCursor = getTodayValues(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        valueCursor.moveToFirst();

        for (int i = 0; i < valueCursor.getCount(); i++) {
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("seconds"));
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("minutes")) * 60;
            valueCursor.moveToNext();
        }
    }
}