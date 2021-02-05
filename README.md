# Tea-Time-Logger <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/ttl_launcher_icon.png" width="48">
Stopwatch app for Android that keeps all your stopwatch sessions in a database (date_time - minutes - seconds) with some neat little features and statistics.

# Features
- Keeps track of all your sessions in a database, sorted by date and selectable with a calendar
- Displays the total time and how many sessions there have been on the selected day, as well as total of all time
- Displays live-updated total time and saves your most recent session time on the main screen
- Lightweight: APK is 2.3 MB
- Intuitive, minimalistic and easy to use
- Optimised for dark and light mode

<img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Screenshot_23.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Screenshot_25.png" width="200">


# Instructions
- Start: Starts or resumes a session
- Pause: Pauses session
- Reset: Resets the stopwatch and halts further execution; Total Time Display will be reset to previous value

- End Session: Ends the session, executes Reset and writes data to the database
- Open Log: Opens the log and displays today's database entries
- Delete Latest Entry: Deletes the most recent entry of the current day
- Calendar: Enables you to select a date to show past database entries for


# Behaviour
- You can only delete entries of the current day, which is to prevent anomalies in display of the entries
- Sessions continuing into the next day will be saved on that day instead of the day they started in
- Total Time Display will not reset when continuing into the next day until the current session has been ended
- Tries to save a running session when the app is being killed by the user or a process; no guarantee it will work
- Should fit on most screens of today


# Warning
Uninstalling (and reinstalling) will delete the database and all your data with it!
Updating the app will not.


# Bugs
I have not found any major bugs, there may be small bugs in display, though they are a little nuisance at most.
