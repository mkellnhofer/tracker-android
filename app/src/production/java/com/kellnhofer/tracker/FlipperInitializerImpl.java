package com.kellnhofer.tracker;

import android.content.Context;

import okhttp3.Interceptor;

// This is a dummy implementation, it doesn't do anything for this app flavor
public class FlipperInitializerImpl implements FlipperInitializer {

    @Override
    public void init(Context context) {
        // No initialization needed here
    }

    @Override
    public Interceptor getOkHttpInterceptor() {
        return chain -> chain.proceed(chain.request());
    }

}
