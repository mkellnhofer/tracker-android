package com.kellnhofer.tracker;

import android.content.Context;

import okhttp3.Interceptor;

public interface FlipperInitializer {

    void init(Context context);

    Interceptor getOkHttpInterceptor();

}
