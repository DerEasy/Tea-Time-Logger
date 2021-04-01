# Tea Time Logger <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Icons/App%20Icon%20Vector%20Graphic.svg" width="48">
Stopwatch app that keeps all your stopwatch sessions in a database with a log, statistics, goal system, backup and restore functions, database insertion/editing/deletion functions and some other features.


# Download
Download above in the main branch or in the release tab (it's the TTLv2.0-stable.apk file). Links to both here:

https://github.com/DerEasy/Tea-Time-Logger/releases/tag/v2.0

https://github.com/DerEasy/Tea-Time-Logger/blob/main/TTLv2.0-stable.apk



# Features
- Keeps track of all your sessions in a database, sorted by date and selectable with a calendar
- Displays the total time and how many sessions there have been on the selected day, as well as total of all time
- Displays live-updated total time and saves your most recent session time on the main screen
- Goal System with Progress Bar indicates your progress on achieving your goals
- User can edit the database freely and easily
- Database can be backed up, so you will never lose your progress
- Lightweight: APK is 2.8 MB
- Intuitive, minimalistic and easy to use
- Optimised for dark and light mode

<img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/Main.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/Log.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/Settings.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/DeletePicker.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/EditPicker.png" width="200"> <img src="https://github.com/DerEasy/Tea-Time-Logger/blob/main/Images/Screenshots/EditDialog.png" width="200">



# Instructions
Main Screen
- Start: Starts or resumes a session
- Pause: Pauses session
- Reset: Resets the stopwatch and halts further execution
- End Session: Ends the session, executes Reset and writes data to the database
- Log Icon: Opens the log and displays today's database entries
- Settings Icon: Opens settings page

Log
- Calendar: Enables you to select a date to show database entries for; is also used for all actions below
- Delete Icon: Opens a pop-up where you can choose to delete (multiple) entries
- Edit Icon: Opens a pop-up where you can edit an entry's duration
- Insertion Icon: Opens two pop-ups; one to choose the time and another to choose the duration of your manual entry

Settings
- Resetting Session Switch: Enables a dialog to warn user before resetting a session
- Deleting Entries Switch: Enables a dialog to warn user before deleting an entry in the database
- Daily Goal: Enables/Disables the progress bar on the Main Screen and sets its goal duration. Possible values are 0 to 1440; 0 (or empty) will disable the progress bar
- Backup Icon: Creates a backup of the database in the device's internal storage. Path: /TeaTimeLogger Backup/teatimelog.db
- Restore Icon: Restores the backup in the path /TeaTimeLogger Backup/teatimelog.db if there is a backup


# Notice: Backup and Restore
This is how it works:

The backup copys the database file (teatimelog.db) from your device storage to the internal storage, which you as a normal Android user can access. To stop the app from hogging too much of your storage space, any previous backup file will be overwritten, unless you renamed it beforehand. You will find the file in the path /TeaTimeLogger Backup/teatimelog.db

Be sure NOT to rename the file NOR the folder it is in. They MUST be left with their default names in their default locations or restoration WILL NOT work. You can copy, move and upload the file wherever you want, just beware where exactly to put it back in the case of restoration.

If above criteria is met (which it always will be if you don't alter it yourself), the app's restore button will then take that backup file and overwrite the app's internal database with it. Beware that this will IRREVERSIBLY delete the app's internal database and replace it with the backup.

# Behaviour
- Sessions continuing into the next day will be saved on that day instead of the day they started in
- Total Time Display will not reset when continuing into the next day until the current session has been ended
- Progress Bar will not reset when continuing into the next day until the current session has been ended
- When entering the next day, displays will only update when the user starts a new session
- Tries to save a running session when the app is being killed by the user or a process; no guarantee it will work


# Warning
Uninstalling (and reinstalling) will delete the database and all your data with it!

Updating the app will not.

For this reason, always create a backup.
