package com.kellnhofer.tracker.service;

import com.kellnhofer.tracker.R;

public enum KmlExportError {

    FILE_IO_ERROR(R.string.error_export_file_io);

    private int mTextResId;

    KmlExportError(int textResId) {
        mTextResId = textResId;
    }

    public int getTextResId() {
        return mTextResId;
    }

}
