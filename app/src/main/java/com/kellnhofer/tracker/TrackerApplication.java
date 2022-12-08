package com.kellnhofer.tracker;

import java.lang.reflect.Type;

import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.kellnhofer.tracker.data.DbHelper;
import com.kellnhofer.tracker.rest.AuthInterceptor;
import com.kellnhofer.tracker.rest.LocationApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackerApplication extends Application {

    private static final String LOG_TAG = TrackerApplication.class.getSimpleName();

    private TrackerSettings mSettings;
    private TrackerStates mStates;

    private DbHelper mDbHelper;

    private OkHttpClient mOkHttpClient;
    private Gson mGson;
    private Retrofit mRetrofit;

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = new TrackerSettings(this, Injector.getLocationService(this));
        mStates = new TrackerStates(this);

        initData();

        initStetho();

        initOkHttp();
        initGson();
        initRetrofit();
    }

    private void initData() {
        Log.d(LOG_TAG, "Init data.");

        mDbHelper = new DbHelper(this);
    }

    private void initStetho() {
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }

    public void initOkHttp() {
        Log.d(LOG_TAG, "Init OkHttp.");

        AuthInterceptor authInterceptor = new AuthInterceptor();
        authInterceptor.setPassword(mSettings.getServerPassword());

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(new StethoInterceptor());
        }
        builder.addNetworkInterceptor(authInterceptor);
        mOkHttpClient = builder.build();
    }

    private void initGson() {
        Log.d(LOG_TAG, "Init Gson.");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Void.class, new JsonDeserializer<Void>() {
            @Override
            public Void deserialize(JsonElement json, Type type,
                                    JsonDeserializationContext context)
                    throws JsonParseException {
                return null;
            }
        });
        gsonBuilder.setDateFormat(Constants.DATE_FORMAT_API);
        mGson = gsonBuilder.create();
    }

    public void initRetrofit() {
        Log.d(LOG_TAG, "Init Retrofit.");

        mRetrofit = new Retrofit.Builder()
                .baseUrl(mSettings.getServerUrl() + "/")
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();
    }

    public TrackerSettings getSettings() {
        return mSettings;
    }

    public TrackerStates getStates() {
        return mStates;
    }

    public LocationApi getLocationApi() {
        return mRetrofit.create(LocationApi.class);
    }

    // --- Helper methods ---

    public boolean hasGpsPermissions() {
        for (String permission : Constants.GPS_PERMISSIONS) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
