package com.kellnhofer.tracker.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kellnhofer.tracker.data.DbContract.LocationPersonEntry;
import com.kellnhofer.tracker.data.DbContract.PersonEntry;
import com.kellnhofer.tracker.model.Person;
import com.kellnhofer.tracker.util.DbUtils;

public class PersonDataSource {

    private final DbHelper mDbHelper;

    public PersonDataSource(Context context) {
        mDbHelper = new DbHelper(context);
    }

    // --- CRUD methods ---

    public ArrayList<Person> getPersons() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(PersonEntry.TABLE, PersonEntry.PROJECTION_ALL, null, null, null,
                null, null);

        ArrayList<Person> persons = createPersonsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        return persons;
    }

    @Nullable
    public Person getPerson(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(PersonEntry.TABLE, PersonEntry.PROJECTION_ALL,
                PersonEntry._ID + " = ?", new String[]{Long.toString(id)}, null, null, null);

        Person person = createPersonFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        return person;
    }

    public long savePerson(@NonNull Person person) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (person.getId() != 0L) {
            values.put(PersonEntry._ID, person.getId());
        }
        values.put(PersonEntry.COLUMN_FIRST_NAME, person.getFirstName());
        values.put(PersonEntry.COLUMN_LAST_NAME, person.getLastName());

        return db.replace(PersonEntry.TABLE, null, values);
    }

    public int deletePerson(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(PersonEntry.TABLE, PersonEntry._ID + " = ?", new String[]{
                Long.toString(id)});
    }

    // --- Query methods ---

    @Nullable
    public Person getPersonByFirstNameAndLastName(String firstName, String lastName) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(PersonEntry.TABLE, PersonEntry.PROJECTION_ALL,
                PersonEntry.COLUMN_FIRST_NAME + " LIKE ?"
                        + " AND " + PersonEntry.COLUMN_LAST_NAME + " LIKE ?",
                new String[]{firstName, lastName}, null, null, null);

        Person person = createPersonFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        return person;
    }

    public ArrayList<Person> getPersonsByLocationId(long locationId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DbUtils.toColumnsString(PersonEntry.PROJECTION_ALL)
                        + " FROM " + PersonEntry.TABLE
                        + " INNER JOIN " + LocationPersonEntry.TABLE
                        + " ON " + PersonEntry._ID + " = " + LocationPersonEntry.COLUMN_PERSON_ID
                        + " WHERE " + LocationPersonEntry.COLUMN_LOCATION_ID + " = ?",
                new String[]{Long.toString(locationId)});

        ArrayList<Person> persons = createPersonsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        return persons;
    }

    public ArrayList<Person> findPersonsByName(String name) {
        if (name == null || name.isEmpty()) {
            return new ArrayList<>();
        }

        String n = DbUtils.escapeString(name);

        if (n.indexOf('*') < 0) {
            n = "*" + n + "*";
        }

        n = n.replace('*', '%');

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DbUtils.toColumnsString(PersonEntry.PROJECTION_ALL)
                        + ", TRIM(" + PersonEntry.COLUMN_FIRST_NAME + " || ' ' || "
                        + PersonEntry.COLUMN_LAST_NAME + ") AS joined_name"
                        + " FROM " + PersonEntry.TABLE
                        + " WHERE joined_name LIKE ?",
                new String[]{n});

        ArrayList<Person> persons = createPersonsFromCursor(cursor);

        if (cursor != null) {
            cursor.close();
        }

        return persons;
    }

    // --- Update methods ---

    public int deleteUnusedPersons() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.delete(PersonEntry.TABLE, PersonEntry._ID + " NOT IN ("
                        + "SELECT " + LocationPersonEntry.COLUMN_PERSON_ID
                        + " FROM " + LocationPersonEntry.TABLE
                        + ")",
                null);
    }

    // --- Helper methods ---

    private ArrayList<Person> createPersonsFromCursor(Cursor cursor) {
        ArrayList<Person> persons = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            persons.add(readPersonFromCursor(cursor));
        }
        return persons;
    }

    private Person createPersonFromCursor(Cursor cursor) {
        Person person = null;
        while (cursor != null && cursor.moveToNext()) {
            person = readPersonFromCursor(cursor);
        }
        return person;
    }

    private Person readPersonFromCursor(Cursor cursor) {
        Person person = new Person();
        person.setId(cursor.getLong(0));
        person.setFirstName(cursor.getString(1));
        person.setLastName(cursor.getString(2));
        return person;
    }

}
