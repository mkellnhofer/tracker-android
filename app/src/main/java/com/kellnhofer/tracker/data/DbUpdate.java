package com.kellnhofer.tracker.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DbUpdate {
    int order();
    int oldVersion();
    int newVersion();
}
