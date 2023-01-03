package com.kellnhofer.tracker.presenter;

import android.content.Context;
import android.os.Handler;

public abstract class BasePresenter implements BaseContract.Presenter {

    protected final Context mContext;

    protected BasePresenter(Context context) {
        mContext = context;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    protected void executeOnMainThread(Runnable runnable) {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        mainHandler.post(runnable);
    }

}
