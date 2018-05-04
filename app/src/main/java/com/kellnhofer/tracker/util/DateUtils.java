package com.kellnhofer.tracker.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        try {
            DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_UI, Locale.getDefault());
            df.setTimeZone(TimeZone.getDefault());
            return df.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid UI date string!", e);
        }
    }

    public static String toServiceFormat(Date date) {
        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_SERVICE, Locale.getDefault());
        return df.format(date);
    }

    public static Date fromServiceFormat(String date) {
        try {
            DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_SERVICE, Locale.getDefault());
            return df.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid service date string!", e);
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

}
