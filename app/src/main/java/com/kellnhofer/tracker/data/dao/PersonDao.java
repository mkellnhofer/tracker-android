package com.kellnhofer.tracker.data.dao;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.kellnhofer.tracker.data.AsyncResult;
import com.kellnhofer.tracker.data.DbContract.LocationPersonTbl;
import com.kellnhofer.tracker.data.DbContract.PersonTbl;
import com.kellnhofer.tracker.model.Person;

@Dao
public abstract class PersonDao extends BaseDao {

    public AsyncResult<List<Person>> getPersonsAsync() {
        return AsyncResult.createAsyncResult(this::getPersons);
    }

    @Query("SELECT * FROM " + PersonTbl.NAME)
    protected abstract List<Person> getPersons();

    public AsyncResult<List<Person>> getPersonsByLocationIdAsync(long locationId) {
        return AsyncResult.createAsyncResult(() -> getPersonsByLocationId(locationId));
    }

    @Query("SELECT * FROM " + PersonTbl.NAME +
            " WHERE " + PersonTbl._ID + " IN (" +
            "SELECT " + LocationPersonTbl.COLUMN_PERSON_ID + " FROM " + LocationPersonTbl.NAME +
            " WHERE " + LocationPersonTbl.COLUMN_LOCATION_ID + " = :locationId" +
            ")")
    public abstract List<Person> getPersonsByLocationId(long locationId);

    public List<Person> findPersonsByNameWithWildcards(String name) {
        if (name == null || name.isEmpty()) {
            return new ArrayList<>();
        }

        String escapedName  = escapeSearchString(name);

        return findPersonsByName(escapedName);
    }

    @Query("SELECT p1.* FROM " + PersonTbl.NAME + " p1" +
            " INNER JOIN (" +
            "SELECT " + PersonTbl._ID + "," +
            " TRIM(" +
            PersonTbl.COLUMN_FIRST_NAME + " || ' ' || " + PersonTbl.COLUMN_LAST_NAME +
            ") AS joined_name" +
            " FROM " + PersonTbl.NAME +
            " WHERE joined_name LIKE :name" +
            ") p2 ON p1." + PersonTbl._ID + " = p2." + PersonTbl._ID)
    protected abstract List<Person> findPersonsByName(String name);

    @Nullable
    @Query("SELECT * FROM " + PersonTbl.NAME +
            " WHERE " + PersonTbl.COLUMN_FIRST_NAME + " LIKE :firstName" +
            " AND " + PersonTbl.COLUMN_LAST_NAME + " LIKE :lastName")
    public abstract Person getPersonByFirstNameAndLastName(String firstName, String lastName);

    @Transaction
    public long savePerson(Person person) {
        long id = insertPerson(person);
        if (id > 0L) {
            return id;
        }
        updatePerson(person);
        return person.getId();
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract long insertPerson(Person person);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void updatePerson(Person person);

    @Query("DELETE FROM " + PersonTbl.NAME +
            " WHERE " + PersonTbl._ID + " NOT IN (" +
            "SELECT " + LocationPersonTbl.COLUMN_PERSON_ID + " FROM " + LocationPersonTbl.NAME +
            ")")
    public abstract void deleteUnusedPersons();

}
