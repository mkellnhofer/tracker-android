package com.kellnhofer.tracker.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;

public class ExportServiceAdapter {

    public interface Listener {
        void onKmlExportStarted();
        void onKmlExportProgress(int current, int total);
        void onKmlExportFinished(int total);
        void onKmlExportFailed(KmlExportError error);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ExportService.Binder binder = (ExportService.Binder) service;
            mService = binder.getService();
            mService.setCallback(new ExportService.Callback() {
                @Override
                public void onKmlExportStarted() {
                    mListener.onKmlExportStarted();
                }

                @Override
                public void onKmlExportProgress(int current, int total) {
                    mListener.onKmlExportProgress(current, total);
                }

                @Override
                public void onKmlExportFinished(int total) {
                    mListener.onKmlExportFinished(total);
                }

                @Override
                public void onKmlExportFailed(KmlExportError error) {
                    mListener.onKmlExportFailed(error);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private final Context mContext;

    private ExportService mService;

    private Listener mListener;

    public ExportServiceAdapter(Context context) {
        mContext = context;
    }

    public void setListener(Listener listener) {
        mListener = listener;

        if (mService == null) {
            mContext.startService(new Intent(mContext, ExportService.class));
            mContext.bindService(new Intent(mContext, ExportService.class),
                    mServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    public void removeListener() {
        if (mService != null) {
            mService.setCallback(null);
            mService = null;
            mContext.unbindService(mServiceConnection);
        }

        mListener = null;
    }

    public void startKmlExport(Uri fileUri) {
        Intent intent = new Intent(mContext, ExportService.class);
        intent.setAction(ExportService.ACTION_START_KML_EXPORT);
        intent.setData(fileUri);
        mContext.startService(intent);
    }

    public void stopKmlExport() {
        Intent intent = new Intent(mContext, ExportService.class);
        intent.setAction(ExportService.ACTION_STOP_KML_EXPORT);
        mContext.startService(intent);
    }

}
