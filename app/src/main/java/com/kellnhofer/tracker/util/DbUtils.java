package com.kellnhofer.tracker.util;

public final class DbUtils {

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

    public static String escapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (Character c : s.toCharArray()) {
            if (c == '%' || c == '_') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

}
