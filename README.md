# Tea Time Logger <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Icons/App%20Icon%20Vector%20Graphic.svg" width="48">
Stopwatch app that keeps all your stopwatch sessions in a database with a log, statistics, goal system, backup and restore functions, database insertion/editing/deletion functions and some other features.

# Features
- Keeps track of all your sessions in a database, sorted by date and selectable with a calendar
- Displays the total time and how many sessions there have been on the selected day, as well as total of all time
- Displays live-updated total time and saves your most recent session time on the main screen
- Goal System with Progress Bar indicates your progress on achieving your goals
- Lightweight: APK is 2.4 MB
- Intuitive, minimalistic and easy to use
- Optimised for dark and light mode

<img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/Main.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/Log.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/Settings.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/DeletePicker.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/EditPicker.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/EditDialog.png" width="200">



# Instructions
- Start: Starts or resumes a session
- Pause: Pauses session
- Reset: Resets the stopwatch and halts further execution

- End Session: Ends the session, executes Reset and writes data to the database
- Open Log: Opens the log and displays today's database entries
- Delete Latest Entry: Deletes the most recent entry of the current day
- Calendar: Enables you to select a date to show past database entries for


# Behaviour
- You can only delete entries of the current day, which is to prevent anomalies in display of the entries
- Sessions continuing into the next day will be saved on that day instead of the day they started in
- Total Time Display will not reset when continuing into the next day until the current session has been ended
- Progress Bar will not reset when continuing into the next day until the current session has been ended
- Tries to save a running session when the app is being killed by the user or a process; no guarantee it will work
- Should fit on most screens of today


# Warning
Uninstalling (and reinstalling) will delete the database and all your data with it!
Updating the app will not.


# Bugs
Yet to be found.
