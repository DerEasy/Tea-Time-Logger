package com.easy.teatimelogger;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.database.Cursor;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import static com.easy.teatimelogger.MainActivity.pMainActivityRef;
import static com.easy.teatimelogger.MainActivity.pDatabase;
import static com.easy.teatimelogger.MainActivity.c;
import static com.easy.teatimelogger.SettingsActivity.DELETE_DIALOG;

public class LogActivity extends AppCompatActivity {
    public CalendarView calendar;
    public Cursor valueCursor;
    public int totalTimeOnSelectedDay, sessionCounter, allTimeTotal;
    public TextView pDateText, pSessionMinutesText, pSessionSecondsText, pTotalTimeOnSelectedDay, pSessionCounterText, pAllTimeTotalText, pAllTimeSessionText;
    public Button pDeleteButton;
    public static final String SHARED_PREFS = "sharedPrefs";
    private boolean savedDeleteIsChecked;
    public static WeakReference<LogActivity> pLogActivityRef;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        //Assigns WeakReference object current class instance
        updateLogActivity();

        //Creates new writable database instance
        LogDBHelper dbHelper = new LogDBHelper(this);
        pDatabase = dbHelper.getWritableDatabase();

        //Assigns variables their corresponding items in the LogActivity
        pDateText = (TextView) findViewById(R.id.textLogDate);
        pSessionMinutesText = (TextView) findViewById(R.id.textLogSessionMinutes);
        pSessionSecondsText = (TextView) findViewById(R.id.textLogSessionSeconds);
        pTotalTimeOnSelectedDay = (TextView) findViewById(R.id.textTotalTimeOnSelectedDay);
        pSessionCounterText = (TextView) findViewById(R.id.textSessionCounter);
        pAllTimeTotalText = (TextView) findViewById(R.id.textAllTimeTotal);
        pAllTimeSessionText = (TextView) findViewById(R.id.textAllTimeSessionCounter);
        pDeleteButton = (Button) findViewById(R.id.buttonDelete);

        setDeleteButtonVisibility();

        //Assigns recyclerView and sets its LinearLayout
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Current local day calendar for comparison purposes
        c = Calendar.getInstance();

        //Sets adapter of MainActivity instance to the recyclerView and gets current dataset
        pMainActivityRef.get().pAdapter = new LogAdapter(this, getTodayItems(String.valueOf(c.get(Calendar.YEAR)),
                String.valueOf(c.get(Calendar.MONTH) + 1),
                String.valueOf(c.get(Calendar.DAY_OF_MONTH))));
        recyclerView.setAdapter(pMainActivityRef.get().pAdapter);
        callTTOSD(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        getTotal();

        //Assigns CalendarView
        calendar = (CalendarView) findViewById(R.id.calendarView);

        //Lambda function
        //Updates dataset when selected calendar date has changed
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            pMainActivityRef.get().pAdapter = new LogAdapter(this, getTodayItems(String.valueOf(year), String.valueOf(month + 1), String.valueOf(dayOfMonth)));
            recyclerView.setAdapter(pMainActivityRef.get().pAdapter);

            callTTOSD(String.valueOf(year), String.valueOf(month + 1), String.valueOf(dayOfMonth));

            //Sets visibility of Delete button; only visible when selected date equals current local date
            setDeleteButtonVisibility();
            if (String.valueOf(c.get(Calendar.YEAR)).equals(String.valueOf(year)) &&
                    String.valueOf(c.get(Calendar.MONTH)).equals(String.valueOf(month)) &&
                    String.valueOf(c.get(Calendar.DAY_OF_MONTH)).equals(String.valueOf(dayOfMonth))) {
                setDeleteButtonVisibility();
            } else {
                pDeleteButton.setVisibility(View.INVISIBLE);
            }
        });

        //Lambda function
        //Displays an alert dialog if it is set in the settings
        pDeleteButton.setOnClickListener(pDeleteButton -> {
            loadSwitchData();
            valueCursor = getTodayValues(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
            if (valueCursor.getCount() != 0) {
                if (savedDeleteIsChecked) openDialog();
                else delete(null);
                pMainActivityRef.get().setLogProgress(callSetProgressOnMain());
            }
        });
    }

    /**
     *  Assign class instance to WeakReference object.
     */
    private void updateLogActivity() {
        pLogActivityRef = new WeakReference<>(this);
    }

    /**
     * Opens confirmation dialog
     */
    private void openDialog() {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.show(getSupportFragmentManager(), "Delete Entry Dialog");
    }

    /**
     * Loads delete switch setting value
     */
    private void loadSwitchData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        savedDeleteIsChecked = sharedPreferences.getBoolean(DELETE_DIALOG, true);
    }

    /**
     * Automatically sets the visibility of the delete button. May only be used in specific cases.
     */
    private void setDeleteButtonVisibility() {
        valueCursor = getTodayValues(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        if (valueCursor.getCount() == 0) {
            pDeleteButton.setVisibility(View.INVISIBLE);
        } else {
            pDeleteButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Gets the total minutes and session count of the entire database
     */
    public void getTotal() {
        allTimeTotal = 0;
        valueCursor = getAllValues();
        valueCursor.moveToFirst();

        for (int i = 0; i < valueCursor.getCount(); i++) {
            allTimeTotal += valueCursor.getInt(valueCursor.getColumnIndex("minutes")) * 60;
            allTimeTotal += valueCursor.getInt(valueCursor.getColumnIndex("seconds"));
            valueCursor.moveToNext();
        } allTimeTotal /= 60;

        pAllTimeTotalText.setText("T. Time: " + allTimeTotal + " min");
        pAllTimeSessionText.setText("T. Sessions: " + valueCursor.getCount());
    }

    /**
     * Calls Total Time of Selected Day and that day's session counter.
     * Automatically sets text of TextView variables with correct formatting (e.g. 05:02).
     * @param year year
     * @param month month - double digits
     * @param day_of_month day - double digits
     */
    private void callTTOSD(String year, String month, String day_of_month) {
        totalTimeOnSelectedDay = 0;
        valueCursor = getTodayValues(year, month, day_of_month);
        valueCursor.moveToFirst();

        for (int i = 0; i < valueCursor.getCount(); i++) {
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("seconds"));
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("minutes")) * 60;
            valueCursor.moveToNext();
        }

        if (minutesHaveDoubleDigits() && secondsHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("Total Time on this Day:    " + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("Total Time on this Day:    0" + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && secondsHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("Total Time on this Day:    0" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("Total Time on this Day:    " + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else {
            pTotalTimeOnSelectedDay.setText("Total Time on this Day:    " + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        }

        sessionCounter = valueCursor.getCount();
        pSessionCounterText.setText("Sessions: " + sessionCounter);
    }

    /**
     * Returns Total Time of Selected Day on MainActivity
     * @return String of Total Time
     */
    private String callTTOSDonMain() {
        totalTimeOnSelectedDay = 0;
        valueCursor = getTodayValues(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        valueCursor.moveToFirst();

        for (int i = 0; i < valueCursor.getCount(); i++) {
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("seconds"));
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("minutes")) * 60;
            valueCursor.moveToNext();
        }

        if (minutesHaveDoubleDigits() && secondsHaveDoubleDigits()) {
            return ("Total Time Today:    " + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            return ("Total Time Today:    0" + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && secondsHaveDoubleDigits()) {
            return ("Total Time Today:    0" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            return ("Total Time Today:    " + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else {
            return ("Total Time Today:    " + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        }
    }

    private int callSetProgressOnMain() {
        totalTimeOnSelectedDay = 0;
        valueCursor = getTodayValues(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        valueCursor.moveToFirst();

        for (int i = 0; i < valueCursor.getCount(); i++) {
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("seconds"));
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("minutes")) * 60;
            valueCursor.moveToNext();
        }

        return totalTimeOnSelectedDay;
    }

    /**
     * Check if minutes have double digits
     * @return true if double digits
     */
    private boolean minutesHaveDoubleDigits() {
        return totalTimeOnSelectedDay / 60 >= 10 && totalTimeOnSelectedDay / 60 < 100;
    }

    /**
     * Check if seconds have double digits
     * @return true if double digits
     */
    private boolean secondsHaveDoubleDigits() {
        return totalTimeOnSelectedDay % 60 >= 10;
    }

    /**
     *  Removes the last entry added to the database.
     */
    public void delete(View v) {
        //pDatabase.delete(LogContract.LogEntry.TABLE_NAME, null, null); Clears entire database for developmental purposes
        valueCursor = getTodayValues(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));

        //Ensures that only values of the current local day can be deleted.
        if (valueCursor.getCount() != 0) {
            String del = " DELETE FROM tealog WHERE _ID in(SELECT MAX(_ID) FROM tealog)";
            pDatabase.execSQL(del);
            pMainActivityRef.get().pAdapter.swapCursor(getTodayItems(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH))));
            pMainActivityRef.get().pTextSessionTime.setText("Last Session has been deleted.");
            pMainActivityRef.get().pTotalTimeOnSelectedDay.setText(callTTOSDonMain());
            callTTOSD(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
            pMainActivityRef.get().saveTimeData();
            pMainActivityRef.get().setLogProgress(callSetProgressOnMain());
            getTotal();
        }
        setDeleteButtonVisibility();
    }

    /**
     *  Adds an entry to the database.
     *  Automatically called by End Session button.
     */
    public static void addItem() {
        ContentValues cv = new ContentValues();
        cv.put(LogContract.LogEntry.COLUMN_MINUTES, MainActivity.timerMinutes);
        cv.put(LogContract.LogEntry.COLUMN_SECONDS, MainActivity.timerSeconds);

        pDatabase.insert(LogContract.LogEntry.TABLE_NAME, null, cv);
        pMainActivityRef.get().pAdapter.swapCursor(getTodayItems(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH))));
    }

    /**
     *  Returns cursor with entries of the selected day from the database
     */
    public static Cursor getTodayItems(String year, String month, String dayOfMonth) {
        if(month.length() == 1) month = "0" + month;
        if(dayOfMonth.length() == 1) dayOfMonth = "0" + dayOfMonth;
        return pDatabase.query(
                LogContract.LogEntry.TABLE_NAME,
                null,
                String.format("date(date_time) = '%s-%s-%s'", year, month, dayOfMonth),
                null,
                null,
                null,
                LogContract.LogEntry.COLUMN_DATE + " DESC"
        );
    }

    /**
     *  Returns Cursor with selected day's minutes and seconds
     */
    public static Cursor getTodayValues(String year, String month, String dayOfMonth) {
        if(month.length() == 1) month = "0" + month;
        if(dayOfMonth.length() == 1) dayOfMonth = "0" + dayOfMonth;
        return pDatabase.query(
                LogContract.LogEntry.TABLE_NAME,
                new String[]{"minutes", "seconds"},
                String.format("date(date_time) = '%s-%s-%s'", year, month, dayOfMonth),
                null,
                null,
                null,
                LogContract.LogEntry.COLUMN_DATE + " DESC"
        );
    }

    /**
     *  Returns Cursor with all day's minutes and seconds
     */
    public static Cursor getAllValues() {
        return pDatabase.query(LogContract.LogEntry.TABLE_NAME,
                new String[]{"minutes", "seconds"},
                null,
                null,
                null,
                null,
                LogContract.LogEntry.COLUMN_DATE + " DESC"
        );
    }
}