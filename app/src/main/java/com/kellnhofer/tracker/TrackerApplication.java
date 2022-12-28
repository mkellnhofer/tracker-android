package com.kellnhofer.tracker;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.kellnhofer.tracker.data.TrackerDatabase;
import com.kellnhofer.tracker.remote.AuthInterceptor;
import com.kellnhofer.tracker.remote.LocationApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackerApplication extends Application {

    private static final String LOG_TAG = TrackerApplication.class.getSimpleName();

    private final FlipperInitializer mFlipperInitializer = new FlipperInitializerImpl();

    private TrackerSettings mSettings;
    private TrackerStates mStates;

    private TrackerDatabase mDatabase;

    private OkHttpClient mOkHttpClient;
    private Gson mGson;
    private Retrofit mRetrofit;

    @Override
    public void onCreate() {
        super.onCreate();

        initFlipper();

        mSettings = new TrackerSettings(this, Injector.getLocationService(this));
        mStates = new TrackerStates(this);

        initDatabase();

        initOkHttp();
        initGson();
        initRetrofit();
    }

    private void initFlipper() {
        mFlipperInitializer.init(this);
    }

    private void initDatabase() {
        Log.d(LOG_TAG, "Init database.");

        mDatabase = TrackerDatabase.getDatabase(this);
    }

    public void initOkHttp() {
        Log.d(LOG_TAG, "Init OkHttp.");

        AuthInterceptor authInterceptor = new AuthInterceptor();
        authInterceptor.setPassword(mSettings.getServerPassword());

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addNetworkInterceptor(mFlipperInitializer.getOkHttpInterceptor());
        builder.addNetworkInterceptor(authInterceptor);
        mOkHttpClient = builder.build();
    }

    private void initGson() {
        Log.d(LOG_TAG, "Init Gson.");

        mGson = new GsonBuilder()
                .registerTypeAdapter(Void.class, (JsonDeserializer<Void>) (json, type, c) -> null)
                .setDateFormat(Constants.DATE_FORMAT_API)
                .create();
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

}
