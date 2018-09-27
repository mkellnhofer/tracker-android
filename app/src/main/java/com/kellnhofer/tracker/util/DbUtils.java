package com.kellnhofer.tracker.util;

public class DbUtils {

    private DbUtils() {

    }

    public static String toColumnsString(String... columns) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<columns.length; i++) {
            sb.append(columns[i]);
            if (i < columns.length-1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
