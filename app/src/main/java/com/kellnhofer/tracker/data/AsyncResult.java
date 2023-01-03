package com.kellnhofer.tracker.data;

import java.util.function.Supplier;

import android.content.Context;
import android.os.Handler;

public class AsyncResult<T> {

    public interface Observer<T> {
        void onResult(T result);
    }

    private Context mContext;
    private Observer<T> mObserver;

    private AsyncResult() {

    }

    public void observe(Context context, Observer<T> observer) {
        if (context == null) {
            throw new IllegalArgumentException("Context can't be null!");
        }
        mContext = context;

        if (observer == null) {
            throw new IllegalArgumentException("Observer can't be null!");
        }
        mObserver = observer;
    }

    private void setResult(T result) {
        if (mContext == null || mObserver == null) {
            return;
        }

        Handler handler = new Handler(mContext.getMainLooper());
        handler.post(() -> mObserver.onResult(result));
    }

    public static <T> AsyncResult<T> createAsyncResult(Supplier<T> supplier) {
        final AsyncResult<T> result = new AsyncResult<>();
        new Thread(() -> result.setResult(supplier.get())).start();
        return result;
    }

}
