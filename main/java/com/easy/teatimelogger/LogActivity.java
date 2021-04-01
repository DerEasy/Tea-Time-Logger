package com.easy.teatimelogger;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.database.Cursor;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;

import static com.easy.teatimelogger.MainActivity.pMainActivityRef;
import static com.easy.teatimelogger.MainActivity.pDatabase;
import static com.easy.teatimelogger.MainActivity.c;
import static com.easy.teatimelogger.SettingsActivity.DELETE_DIALOG;

public class LogActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    public CalendarView calendar;
    public Cursor valueCursor;
    public int totalTimeOnSelectedDay, sessionCounter, allTimeTotal;
    public TextView pDateText, pSessionMinutesText, pSessionSecondsText, pTotalTimeOnSelectedDay, pSessionCounterText, pAllTimeTotalText, pAllTimeSessionText;
    public ImageButton pDeleteButton, pInsertButton, pEditButton;
    public int userYear, userMonth, userDay, durationHour, durationMinute, durationSecond, adjustedDurationMinute;
    private String insertTime;
    public static WeakReference<LogActivity> pLogActivityRef;
    private RecyclerView recyclerView;
    public static final String SHARED_PREFS = "sharedPrefs";
    public boolean savedDeleteIsChecked;

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
        pDeleteButton = (ImageButton) findViewById(R.id.imageButtonDelete);
        pInsertButton = (ImageButton) findViewById(R.id.imageButtonInsert);
        pEditButton = (ImageButton) findViewById(R.id.imageButtonEdit);

        //Assigns recyclerView and sets its LinearLayout
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //Current local day calendar for comparison purposes
        c = Calendar.getInstance();
        userYear = c.get(Calendar.YEAR);
        userMonth = (c.get(Calendar.MONTH)) + 1;
        userDay = c.get(Calendar.DAY_OF_MONTH);

        setDelEditButtonVisibility();

        //Sets adapter of MainActivity instance to the recyclerView and gets current dataset
        pMainActivityRef.get().pAdapter = new LogAdapter(this, 0, getTodayItems(String.valueOf(c.get(Calendar.YEAR)),
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
            pMainActivityRef.get().pAdapter = new LogAdapter(this, 0, getTodayItems(
                    String.valueOf(year),
                    String.valueOf(month + 1),
                    String.valueOf(dayOfMonth))
            );
            recyclerView.setAdapter(pMainActivityRef.get().pAdapter);

            callTTOSD(String.valueOf(year), String.valueOf(month + 1), String.valueOf(dayOfMonth));

            //Sets the user-selected date variables automatically
            userYear = year;
            userMonth = month + 1;
            userDay = dayOfMonth;
            setDelEditButtonVisibility();
        });

        //Lambda function
        //Displays an alert dialog if it is set in the settings
        pDeleteButton.setOnClickListener(pDeleteButton -> {
            DeletePicker deletePicker = new DeletePicker();
            deletePicker.show(getSupportFragmentManager(), "Delete Picker Dialog");
        });

        //Lambda function
        //Brings up time picker dialog
        pInsertButton.setOnClickListener(pInsertButton -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        pEditButton.setOnClickListener(pEditButton -> {
            EditPicker editPicker = new EditPicker();
            editPicker.show(getSupportFragmentManager(), "Edit Picker Dialog");
        });
    }

    /**
     *  Assign class instance to WeakReference object.
     */
    private void updateLogActivity() {
        pLogActivityRef = new WeakReference<>(this);
    }

    /**
     * Automatically sets the visibility of the delete button. May only be used in specific cases.
     */
    private void setDelEditButtonVisibility() {
        valueCursor = getTodayValues(String.valueOf(userYear), String.valueOf(userMonth), String.valueOf(userDay));
        if (valueCursor.getCount() == 0) {
            pDeleteButton.setVisibility(View.INVISIBLE);
            pEditButton.setVisibility(View.INVISIBLE);
        } else {
            pDeleteButton.setVisibility(View.VISIBLE);
            pEditButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Gets the total minutes and session count of the entire database and updates displays
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

        pAllTimeTotalText.setText("Total:    " + allTimeTotal + " min");
        pAllTimeSessionText.setText("Sessions: " + valueCursor.getCount());
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
            pTotalTimeOnSelectedDay.setText("Selected Day:    " + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("Selected Day:    0" + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && secondsHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("Selected Day:    0" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            pTotalTimeOnSelectedDay.setText("Selected Day:    " + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else {
            pTotalTimeOnSelectedDay.setText("Selected Day:    " + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        }

        sessionCounter = valueCursor.getCount();
        pSessionCounterText.setText("Sessions: " + sessionCounter);
    }

    /**
     * Returns Total Time of Selected Day on MainActivity
     * @return String of Total Time
     */
    public String callTTOSDonMain() {
        totalTimeOnSelectedDay = 0;
        valueCursor = getTodayValues(String.valueOf(c.get(Calendar.YEAR)), String.valueOf(c.get(Calendar.MONTH) + 1), String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        valueCursor.moveToFirst();

        for (int i = 0; i < valueCursor.getCount(); i++) {
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("seconds"));
            totalTimeOnSelectedDay += valueCursor.getInt(valueCursor.getColumnIndex("minutes")) * 60;
            valueCursor.moveToNext();
        }

        if (minutesHaveDoubleDigits() && secondsHaveDoubleDigits()) {
            return ("" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            return ("0" + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else if (!minutesHaveDoubleDigits() && secondsHaveDoubleDigits()) {
            return ("0" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        } else if (minutesHaveDoubleDigits() && !secondsHaveDoubleDigits()) {
            return ("" + (totalTimeOnSelectedDay / 60) + ":0" + (totalTimeOnSelectedDay % 60));
        } else {
            return ("" + (totalTimeOnSelectedDay / 60) + ":" + (totalTimeOnSelectedDay % 60));
        }
    }

    /**
     * Calls setProgress() on MainActivity, but with an integer return value.
     * This method is used in combination with the parametrised setLogProgress() method in MainActivity.
     */
    public int callSetProgressOnMain() {
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
     * Loads delete switch setting value
     */
    public void loadSwitchData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        savedDeleteIsChecked = sharedPreferences.getBoolean(DELETE_DIALOG, true);
    }

    /**
     * Check if minutes have double digits
     * @return true if double digits
     */
    private boolean minutesHaveDoubleDigits() {
        return totalTimeOnSelectedDay / 60 >= 10;
    }

    /**
     * Check if seconds have double digits
     * @return true if double digits
     */
    private boolean secondsHaveDoubleDigits() {
        return totalTimeOnSelectedDay % 60 >= 10;
    }

    /**
     *  Lets the user manually insert an entry into the database, then updates all related displays/variables.
     */
    public void insert(View v) {
        String selectedDate;

        //This if-block provides the correct formatting for the date variable
        //Crucial, else entries will not show up in the database
        if (userMonth / 10 == 0 && userDay / 10 == 0) {
            selectedDate = userYear + "-0" + userMonth + "-0" + userDay + " ";
        } else if (userMonth / 10 == 0 && userDay / 10 != 0) {
            selectedDate = userYear + "-0" + userMonth + "-" + userDay + " ";
        } else if (userMonth / 10 != 0 && userDay / 10 == 0) {
            selectedDate = userYear + "-" + userMonth + "-0" + userDay + " ";
        } else {
            selectedDate = userYear + "-" + userMonth + "-" + userDay + " ";
        }

        //Executes the insertion statement in SQL with the proper variables set beforehand by TimePicker and DurationDialog
        String insert = " INSERT INTO tealog (date_time, minutes, seconds) VALUES ('" + selectedDate + insertTime + "', " + adjustedDurationMinute + ", " + durationSecond + ")";
        pDatabase.execSQL(insert);
        Toast.makeText(this, "Insertion successful.", Toast.LENGTH_SHORT).show();
        updateOnEdit();
    }

    /**
     * Updates all associated variables and displays when deleting, inserting or otherwise manipulating data in the database.
     */
    public void updateOnEdit() {
        pMainActivityRef.get().pAdapter.swapCursor(getTodayItems(String.valueOf(userYear), String.valueOf(userMonth), String.valueOf(userDay)));
        recyclerView.setAdapter(pMainActivityRef.get().pAdapter);
        callTTOSD(String.valueOf(userYear), String.valueOf(userMonth), String.valueOf(userDay));
        getTotal();
        setDelEditButtonVisibility();
        pMainActivityRef.get().callTTOSD();
        pMainActivityRef.get().changeTotalTimeText();
        pMainActivityRef.get().setLogProgress(callSetProgressOnMain());
        pMainActivityRef.get().pTotalTimeOnSelectedDay.setText(callTTOSDonMain());
        pMainActivityRef.get().saveTimeData();
    }

    /** Continuation of insert button lambda function.
     *  Assigns the time the user selected in the TimePicker dialog, then opens the
     *  DurationPicker, which then executes the insert function if the values allow for it.
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hour = String.valueOf(hourOfDay);
        String min = String.valueOf(minute);
        if (hour.length() == 1) hour = "0" + hour;
        if (min.length() == 1) min = "0" + min;
        insertTime = String.format("%s:%s:00", hour, min);

        DurationPickerDialog durationPicker = new DurationPickerDialog();
        durationPicker.show(getSupportFragmentManager(), "duration picker");
    }

    /**
     *  Adds an entry to the database.
     *  Automatically called by End Session button.
     */
    public static void addItem() {
        ContentValues cv = new ContentValues();
        cv.put(LogContract.LogEntry.COLUMN_MINUTES, pMainActivityRef.get().timerMinutes);
        cv.put(LogContract.LogEntry.COLUMN_SECONDS, pMainActivityRef.get().timerSeconds);

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