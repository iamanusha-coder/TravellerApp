
package com.traveller.android.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Person.class, Trip.class, Placee.class}, version = 1)
public abstract class TripDatabase extends RoomDatabase {
    public abstract PersonStore personStore();
    public abstract TripStore tripStore();

    private static final String DB_NAME = "traveller.db";
    private static volatile TripDatabase INSTANCE = null;

    public synchronized static TripDatabase get(Context ctxt) {
        if (INSTANCE == null) {
            INSTANCE = create(ctxt);
        }
        return (INSTANCE);
    }

    static TripDatabase create(Context ctxt) {
        RoomDatabase.Builder<TripDatabase> b;
        b = Room.databaseBuilder(ctxt.getApplicationContext(), TripDatabase.class,
                DB_NAME);
        return (b.build());
    }
}
