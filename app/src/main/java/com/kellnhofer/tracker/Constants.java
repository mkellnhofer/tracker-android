package com.kellnhofer.tracker;

public interface Constants {

    String DATE_FORMAT_UI = "yyyy-MM-dd - HH:mm";
    String DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    String DATE_FORMAT_FILE = "yyyy-MM-dd HH-mm-ss";

    String DATE_VALIDATOR_UI = "^\\d{1,4}\\-\\d{1,2}\\-\\d{1,2} \\- \\d{1,2}\\:\\d{1,2}$";

    String KML_EXPORT_FILE_NAME = "Tracker Export";
    String KML_EXPORT_FILE_EXTENSION = "kml";
    String KML_EXPORT_MIME_TYPE = "application/vnd.google-earth.kml+xml";

}
