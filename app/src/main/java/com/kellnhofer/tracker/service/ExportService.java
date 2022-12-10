package com.kellnhofer.tracker.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;

import com.kellnhofer.tracker.BuildConfig;
import com.kellnhofer.tracker.TrackerApplication;

public class ExportService extends Service implements KmlExportThread.Callback {

    private static final String LOG_TAG = ExportService.class.getSimpleName();

    public static final String ACTION_START_KML_EXPORT =
            BuildConfig.APPLICATION_ID + ".action.START_KML_EXPORT";
    public static final String ACTION_STOP_KML_EXPORT =
            BuildConfig.APPLICATION_ID + ".action.STOP_KML_EXPORT";

    public class Binder extends android.os.Binder {
        public ExportService getService() {
            return ExportService.this;
        }
    }

    public interface Callback {
        void onKmlExportStarted();
        void onKmlExportProgress(int current, int total);
        void onKmlExportFinished(int total);
        void onKmlExportFailed(KmlExportError error);
    }

    private final IBinder mBinder = new Binder();

    private TrackerApplication mApplication;

    private Callback mCallback;

    private KmlExportThread mKmlExportThread = null;
    private final Queue<KmlExportState> mLastKmlExportStates = new ConcurrentLinkedQueue<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mApplication = (TrackerApplication) this.getApplication();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallback(Callback callback) {
        if (callback != null) {
            notifyLastKmlExportStates(callback);
        }

        mCallback = callback;
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY;
        }

        switch(action) {
            case ACTION_START_KML_EXPORT:
                Uri fileUri = intent.getData();
                startSync(fileUri);
                break;
            case ACTION_STOP_KML_EXPORT:
                stopSync();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported action '" + action + "'!");
        }

        return START_NOT_STICKY;
    }

    private void startSync(Uri fileUri) {
        if (mKmlExportThread != null) {
            Log.d(LOG_TAG, "KML export is already running.");
            return;
        }

        Log.i(LOG_TAG, "Starting KML export ...");

        mKmlExportThread = new KmlExportThread(mApplication, fileUri);
        mKmlExportThread.setCallback(this);
        mKmlExportThread.start();
    }

    private void stopSync() {
        if (mKmlExportThread == null) {
            return;
        }

        Log.i(LOG_TAG, "Stopping KML export ...");

        if (mKmlExportThread.isAlive()) {
            mKmlExportThread.interrupt();
        }

        mKmlExportThread = null;
    }

    // --- Sync callbacks ---


    @Override
    public void onKmlExportStarted() {
        notifyKmlExportStarted();
    }

    @Override
    public void onKmlExportProgress(int current, int total) {
        Log.i(LOG_TAG, "Kml export started.");

        notifyKmlExportProgress(current, total);
    }

    @Override
    public void onKmlExportFinished(int total) {
        Log.i(LOG_TAG, "Kml export finished.");

        mKmlExportThread = null;

        notifyKmlExportFinished(total);
    }

    @Override
    public void onKmlExportCanceled() {
        Log.i(LOG_TAG, "Kml export canceled.");

        mKmlExportThread = null;
    }

    @Override
    public void onKmlExportFailed(KmlExportError error) {
        Log.e(LOG_TAG, "Kml export failed.");

        mKmlExportThread = null;

        notifyKmlExportFailed(error);
    }

    // --- Helper methods ---

    private void notifyKmlExportStarted() {
        if (mCallback != null) {
            mCallback.onKmlExportStarted();
        } else {
            mLastKmlExportStates.add(KmlExportState.createStartedState());
        }
    }

    private void notifyKmlExportProgress(int current, int total) {
        if (mCallback != null) {
            mCallback.onKmlExportProgress(current, total);
        }
    }

    private void notifyKmlExportFinished(int total) {
        if (mCallback != null) {
            mCallback.onKmlExportFinished(total);
        } else {
            mLastKmlExportStates.add(KmlExportState.createFinishedState(total));
        }
    }

    private void notifyKmlExportFailed(KmlExportError error) {
        if (mCallback != null) {
            mCallback.onKmlExportFailed(error);
        } else {
            mLastKmlExportStates.add(KmlExportState.createFailedState(error));
        }
    }

    private void notifyLastKmlExportStates(Callback callback) {
        KmlExportState state;
        while ((state = mLastKmlExportStates.poll()) != null) {
            switch (state.getState()) {
                case KmlExportState.STATE_STARTED:
                    callback.onKmlExportStarted();
                    break;
                case KmlExportState.STATE_FINISHED:
                    callback.onKmlExportFinished(state.getTotal());
                    break;
                case KmlExportState.STATE_FAILED:
                    callback.onKmlExportFailed(state.getError());
                    break;
                default:
            }
        }
    }

}
