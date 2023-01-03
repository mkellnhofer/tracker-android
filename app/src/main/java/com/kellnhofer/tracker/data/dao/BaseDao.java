package com.kellnhofer.tracker.data.dao;

public abstract class BaseDao {

    protected static String escapeSearchString(String s) {
        String n = escapeString(s);
        if (n.indexOf('*') < 0) {
            n = "*" + n + "*";
        }
        return n.replace('*', '%');
    }

    private static String escapeString(String s) {
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
