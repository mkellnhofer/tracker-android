package com.kellnhofer.tracker.util;

import java.util.ArrayList;
import java.util.List;

public final class TypeUtils {

    private TypeUtils() {

    }

    public static long[] toLongArray(List<Long> list) {
        if (list == null) {
            return null;
        }
        long[] array = new long[list.size()];
        for (int i=0; i<list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static ArrayList<Long> toLongList(long[] array) {
        if (array == null) {
            return null;
        }
        ArrayList<Long> list = new ArrayList<>();
        for (long l : array) {
            list.add(l);
        }
        return list;
    }

}
