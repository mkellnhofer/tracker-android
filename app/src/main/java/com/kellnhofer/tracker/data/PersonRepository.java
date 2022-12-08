package com.kellnhofer.tracker.data;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kellnhofer.tracker.model.Person;

public class PersonRepository {

    public interface PersonRepositoryObserver {
        void onPersonChanged();
    }

    private final PersonDataSource mDataSource;

    private List<PersonRepositoryObserver> mObservers = new ArrayList<>();

    public PersonRepository(PersonDataSource dataSource) {
        mDataSource = dataSource;
    }

    public void addContentObserver(PersonRepositoryObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public void removeContentObserver(PersonRepositoryObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    // --- CRUD methods ---

    public ArrayList<Person> getPersons() {
        return mDataSource.getPersons();
    }

    @Nullable
    public Person getPerson(long id) {
        return mDataSource.getPerson(id);
    }

    public long savePerson(@NonNull Person person) {
        long id = mDataSource.savePerson(person);
        notifyContentObserver();
        return id;
    }

    public int deletePerson(long id) {
        int count = mDataSource.deletePerson(id);
        notifyContentObserver();
        return count;
    }

    // --- Query methods ---

    @Nullable
    public Person getPersonByFirstNameAndLastName(String firstName, String lastName) {
        return mDataSource.getPersonByFirstNameAndLastName(firstName, lastName);
    }

    public ArrayList<Person> getPersonsByLocationId(long locationId) {
        return mDataSource.getPersonsByLocationId(locationId);
    }

    public ArrayList<Person> findPersonsByName(String name) {
        return mDataSource.findPersonsByName(name);
    }

    // --- Update methods ---

    public int deleteUnusedPersons() {
        return mDataSource.deleteUnusedPersons();
    }

    // --- Helper methods ---

    private void notifyContentObserver() {
        for (PersonRepositoryObserver observer : mObservers) {
            observer.onPersonChanged();
        }
    }

}
