package com.kellnhofer.tracker.rest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private String mPassword;

    public AuthInterceptor() {

    }

    public void setPassword(String password) {
        mPassword = password;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                    .header("Authorization", mPassword)
                    .build();

        return chain.proceed(request);
    }

}
