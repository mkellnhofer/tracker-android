package com.kellnhofer.tracker.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.room.TypeConverter;

public final class DateConverter {

    private static final String DATE_FORMAT_DB = "yyyy-MM-dd HH:mm:ss";

    private DateConverter() {

    }

    @TypeConverter
    public static Date toDate(String date) {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT_DB, Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid DB date string!", e);
        }
    }

    @TypeConverter
    public static String fromDate(Date date) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT_DB, Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

}
