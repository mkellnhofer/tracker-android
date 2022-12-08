package com.kellnhofer.tracker;

import java.util.Arrays;

import android.content.Context;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin.SharedPreferencesDescriptor;
import com.facebook.soloader.SoLoader;
import okhttp3.Interceptor;

public class FlipperInitializerImpl implements FlipperInitializer {

    private final NetworkFlipperPlugin mNetworkPlugin = new NetworkFlipperPlugin();

    @Override
    public void init(Context context) {
        SoLoader.init(context, false);

        FlipperClient client = AndroidFlipperClient.getInstance(context);
        client.addPlugin(new DatabasesFlipperPlugin(context));
        client.addPlugin(new InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()));
        client.addPlugin(NavigationFlipperPlugin.getInstance());
        client.addPlugin(mNetworkPlugin);
        client.addPlugin(new SharedPreferencesFlipperPlugin(context, Arrays.asList(
                getSharedPreferencesDescriptor(TrackerSettings.PREF_FILE_NAME),
                getSharedPreferencesDescriptor(TrackerStates.PREF_FILE_NAME))));
        client.start();
    }

    private static SharedPreferencesDescriptor getSharedPreferencesDescriptor(String fileName) {
        return new SharedPreferencesDescriptor(fileName, Context.MODE_PRIVATE);
    }

    @Override
    public Interceptor getOkHttpInterceptor() {
        return new FlipperOkhttpInterceptor(mNetworkPlugin);
    }

}
