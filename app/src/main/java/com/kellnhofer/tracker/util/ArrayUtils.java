package com.kellnhofer.tracker.util;

import java.util.ArrayList;

public final class ArrayUtils {

    private ArrayUtils() {

    }

    public static String join(ArrayList<?> objects) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<objects.size(); i++) {
            sb.append(objects.get(i));
            if (i < objects.size()-1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
