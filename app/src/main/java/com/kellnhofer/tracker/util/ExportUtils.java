package com.kellnhofer.tracker.util;

import java.util.Date;

import com.kellnhofer.tracker.Constants;

public final class ExportUtils {

    private ExportUtils() {

    }

    public static String generateKmlExportFileName() {
        Date date = new Date();
        return Constants.KML_EXPORT_FILE_NAME + " " + DateUtils.toFileFormat(date) + "." +
                Constants.KML_EXPORT_FILE_EXTENSION;
    }

    public static String getKmlExportMimeType() {
        return Constants.KML_EXPORT_MIME_TYPE;
    }

}
