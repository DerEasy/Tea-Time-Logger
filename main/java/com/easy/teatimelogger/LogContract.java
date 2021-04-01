package com.easy.teatimelogger;

import android.provider.BaseColumns;

public class LogContract {
    private LogContract() {
    }

    public static final class LogEntry implements BaseColumns {
        public static final String TABLE_NAME = "tealog";
        public static final String COLUMN_DATE = "date_time";
        public static final String COLUMN_MINUTES = "minutes";
        public static final String COLUMN_SECONDS = "seconds";
    }
}