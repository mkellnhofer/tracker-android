package com.kellnhofer.tracker.service;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.kellnhofer.tracker.Injector;
import com.kellnhofer.tracker.TrackerApplication;
import com.kellnhofer.tracker.data.LocationRepository;
import com.kellnhofer.tracker.data.PersonRepository;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.model.Person;
import org.xmlpull.v1.XmlSerializer;

public class KmlExportThread extends Thread {

    private static final String LOG_TAG = KmlExportThread.class.getSimpleName();

    private static final String EXPORT_FILE_NAME = "Tracker Export";
    private static final String EXPORT_FILE_EXTENSION = "kml";
    private static final String EXPORT_TITLE = "Tracker Export";

    private static final String DATE_FORMAT_FILE_NAME = "yyyy-MM-dd HH-mm-ss";
    private static final String DATE_FORMAT_FILE_CONTENT = "yyyy-MM-dd HH:mm:ss";

    private static final double LOOK_AT_REGENSBURG_LAT = 49.02181388372180;
    private static final double LOOK_AT_REGENSBURG_LNG = 12.10508158473729;
    private static final int LOOK_AT_RANGE = 5000000;

    private static final int POINT_RANGE = 1000;

    private static final String STYLE_SIMPLE = "loc-balloon-style-simple";
    private static final String STYLE_DESCRIPTION = "loc-balloon-style-description";
    private static final String STYLE_PERSONS = "loc-balloon-style-persons";
    private static final String STYLE_DESCRIPTION_PERSONS = "loc-balloon-style-description-persons";

    public interface Callback {
        void onKmlExportStarted();
        void onKmlExportProgress(int current, int total);
        void onKmlExportFinished(int total);
        void onKmlExportCanceled();
        void onKmlExportFailed(KmlExportError error);
    }

    private TrackerApplication mApplication;

    private LocationRepository mLocationRepository;
    private PersonRepository mPersonRepository;

    private DateFormat mFileNameDateFormat;
    private DateFormat mFileContentDateFormat;

    private Callback mCallback;

    public KmlExportThread(TrackerApplication application) {
        mApplication = application;

        mLocationRepository = Injector.getLocationRepository(mApplication);
        mPersonRepository = Injector.getPersonRepository(mApplication);

        mFileNameDateFormat = new SimpleDateFormat(DATE_FORMAT_FILE_NAME, Locale.getDefault());
        mFileNameDateFormat.setTimeZone(TimeZone.getDefault());
        mFileContentDateFormat = new SimpleDateFormat(DATE_FORMAT_FILE_CONTENT, Locale.getDefault());
        mFileContentDateFormat.setTimeZone(TimeZone.getDefault());
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void run() {
        try {
            // Notify started
            notifyStarted();

            // Get not deleted locations
            List<Location> locations = mLocationRepository.getNotDeletedLocationsByDateDesc();
            // Export locations
            export(locations);

            // Notify finished
            notifyFinished(locations.size());
        } catch (InterruptedException e) {
            // Notify canceled
            notifyCanceled();
        } catch (IOException e) {
            Log.e(LOG_TAG, "File IO failed at export of locations.", e);

            // Notify failed
            notifyFailed(KmlExportError.FILE_IO_ERROR);
        }
    }

    private void export(List<Location> locations) throws InterruptedException, IOException {
        Date currentDate = new Date();

        // Get directory
        File dir = mApplication.getExternalFilesDir(null);
        if (dir == null) {
            return;
        }
        dir.mkdirs();

        // Open file
        File file = new File(dir, EXPORT_FILE_NAME + " " + mFileNameDateFormat.format(currentDate) +
                "." + EXPORT_FILE_EXTENSION);

        // Try to write file
        FileOutputStream fos = null;
        try {
            // Open file output stream
            fos = new FileOutputStream(file);

            // Create serializer
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");

            // Start document
            serializer.startDocument("UTF-8", null);

            // Write document body
            writeHeader(serializer, currentDate);
            writeStyles(serializer);
            writeLookAt(serializer, LOOK_AT_REGENSBURG_LAT, LOOK_AT_REGENSBURG_LNG, LOOK_AT_RANGE);
            writePlacemarks(serializer, locations);
            writeFooter(serializer);

            // End document
            serializer.endDocument();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        // Update media content provider
        updateMediaContentProvider(file);
    }

    private void writeHeader(XmlSerializer serializer, Date date) throws IOException {
        serializer.startTag("", "kml");
        serializer.attribute("", "xmlns", "http://www.opengis.net/kml/2.2");
        serializer.startTag("", "Document");
        serializer.startTag("", "name");
        serializer.text(EXPORT_TITLE + " - " + mFileNameDateFormat.format(date));
        serializer.endTag("", "name");
    }

    private void writeStyles(XmlSerializer serializer) throws IOException {
        writeStyle(serializer, STYLE_SIMPLE,
                "<b>$[name]</b><br/>" +
                        "$[locDate]");
        writeStyle(serializer, STYLE_DESCRIPTION,
                "<b>$[name]</b><br/>" +
                        "$[locDate]<br/><br/>" +
                        "$[locDescription]");
        writeStyle(serializer, STYLE_PERSONS,
                "<b>$[name]</b><br/>" +
                        "$[locDate]<br/><br/>" +
                        "Persons:<br/>$[locPersons]");
        writeStyle(serializer, STYLE_DESCRIPTION_PERSONS,
                "<b>$[name]</b><br/>" +
                        "$[locDate]<br/><br/>" +
                        "$[locDescription]<br/><br/>" +
                        "Persons:<br/>$[locPersons]");
    }

    private void writeStyle(XmlSerializer serializer, String id, String text) throws IOException {
        serializer.startTag("", "Style");
        serializer.attribute("", "id", id);
        serializer.startTag("", "BalloonStyle");
        serializer.startTag("", "text");
        serializer.cdsect(text);
        serializer.endTag("", "text");
        serializer.endTag("", "BalloonStyle");
        serializer.endTag("", "Style");
    }

    private void writePlacemarks(XmlSerializer serializer, List<Location> locations)
            throws InterruptedException, IOException {
        // Process locations
        for (int i = 0; i < locations.size(); i++) {
            // Export location
            writePlacemark(serializer, locations.get(i));
            // Notify progress
            notifyProgress(i + 1, locations.size());
            // If canceled: Abort
            if (interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    private void writePlacemark(XmlSerializer serializer, Location location) throws IOException {
        // Get persons
        ArrayList<Person> persons = getPersons(location.getPersonIds());

        serializer.startTag("", "Placemark");

        // Write location data
        writeName(serializer, location.getName());
        writePoint(serializer, location.getLatitude(), location.getLongitude());
        writeLookAt(serializer, location.getLatitude(), location.getLongitude(), POINT_RANGE);
        writeExtendedData(serializer, location, persons);
        writeStyleUrl(serializer, location, persons);

        serializer.endTag("", "Placemark");
    }

    private ArrayList<Person> getPersons(ArrayList<Long> personIds) {
        ArrayList<Person> persons = new ArrayList<>();
        for (Long personId : personIds) {
            persons.add(mPersonRepository.getPerson(personId));
        }
        return persons;
    }

    private void writeName(XmlSerializer serializer, String name) throws IOException {
        serializer.startTag("", "name");
        serializer.text(name);
        serializer.endTag("", "name");
    }

    private void writePoint(XmlSerializer serializer, double lat, double lng) throws IOException {
        serializer.startTag("", "Point");
        serializer.startTag("", "coordinates");
        serializer.text(Double.toString(lng) + "," + Double.toString(lat));
        serializer.endTag("", "coordinates");
        serializer.endTag("", "Point");
    }

    private void writeLookAt(XmlSerializer serializer, double lat, double lng, int range)
            throws IOException {
        serializer.startTag("", "LookAt");
        serializer.startTag("", "longitude");
        serializer.text(Double.toString(lng));
        serializer.endTag("", "longitude");
        serializer.startTag("", "latitude");
        serializer.text(Double.toString(lat));
        serializer.endTag("", "latitude");
        serializer.startTag("", "range");
        serializer.text(Integer.toString(range));
        serializer.endTag("", "range");
        serializer.startTag("", "heading");
        serializer.text("0");
        serializer.endTag("", "heading");
        serializer.startTag("", "tilt");
        serializer.text("0");
        serializer.endTag("", "tilt");
        serializer.endTag("", "LookAt");
    }

    private void writeExtendedData(XmlSerializer serializer, Location location,
            ArrayList<Person> persons) throws IOException {
        serializer.startTag("", "ExtendedData");
        writeData(serializer, "locDate", mFileContentDateFormat.format(location.getDate()));
        if (location.getDescription() != null) {
            writeData(serializer, "locDescription", location.getDescription());
        }
        if (!persons.isEmpty()) {
            writeData(serializer, "locPersons", toPersonsString(persons));
        }
        serializer.endTag("", "ExtendedData");
    }

    private void writeData(XmlSerializer serializer, String name, String value) throws IOException {
        serializer.startTag("", "Data");
        serializer.attribute("", "name", name);
        serializer.startTag("", "value");
        serializer.text(value);
        serializer.endTag("", "value");
        serializer.endTag("", "Data");
    }

    private void writeStyleUrl(XmlSerializer serializer, Location location,
            ArrayList<Person> persons) throws IOException {
        serializer.startTag("", "styleUrl");
        if (!location.getDescription().isEmpty() && !persons.isEmpty()) {
            serializer.text("#" + STYLE_DESCRIPTION_PERSONS);
        } else if (!location.getDescription().isEmpty()) {
            serializer.text("#" + STYLE_DESCRIPTION);
        } else if (!persons.isEmpty()) {
            serializer.text("#" + STYLE_PERSONS);
        } else {
            serializer.text("#" + STYLE_SIMPLE);
        }
        serializer.endTag("", "styleUrl");
    }

    private void writeFooter(XmlSerializer serializer) throws IOException {
        serializer.endTag("", "Document");
        serializer.endTag("", "kml");
    }

    // --- Helper methods ---

    private void notifyStarted() {
        if (mCallback != null) {
            mCallback.onKmlExportStarted();
        }
    }

    private void notifyProgress(int current, int total) {
        if (mCallback != null) {
            mCallback.onKmlExportProgress(current, total);
        }
    }

    private void notifyFinished(int total) {
        if (mCallback != null) {
            mCallback.onKmlExportFinished(total);
        }
    }

    private void notifyCanceled() {
        if (mCallback != null) {
            mCallback.onKmlExportCanceled();
        }
    }

    private void notifyFailed(KmlExportError error) {
        if (mCallback != null) {
            mCallback.onKmlExportFailed(error);
        }
    }

    private static String toPersonsString(ArrayList<Person> persons) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            if (!person.getFirstName().isEmpty()) {
                sb.append(person.getFirstName()).append(" ").append(person.getLastName());
            } else {
                sb.append(person.getLastName());
            }
            if (i < persons.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private void updateMediaContentProvider(File file) {
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScannerIntent.setData(Uri.fromFile(file));
        mApplication.sendBroadcast(mediaScannerIntent);
    }

}
