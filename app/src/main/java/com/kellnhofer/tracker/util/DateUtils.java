package com.kellnhofer.tracker.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.kellnhofer.tracker.Constants;

public class DateUtils {

    private DateUtils() {

    }

    public static String toUiFormat(Date date) {
        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_UI, Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(date);
    }

    public static Date fromUiFormat(String date) {
        if (!date.matches(Constants.DATE_VALIDATOR_UI)) {
            throw new RuntimeException("Invalid UI date string!");
        }
        try {
            DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_UI, Locale.getDefault());
            df.setTimeZone(TimeZone.getDefault());
            return df.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid UI date string!", e);
        }
    }

    public static String toDbFormat(Date date) {
        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    public static Date fromDbFormat(String date) {
        try {
            DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid DB date string!", e);
        }
    }

    public static int getWeekDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }

}
