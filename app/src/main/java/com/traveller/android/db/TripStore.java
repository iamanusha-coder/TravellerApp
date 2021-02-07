
package com.traveller.android.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class TripStore {
    @Query("SELECT id FROM trip where emailid=:emailid ORDER BY id DESC LIMIT 1")
    public abstract int getLastEntryTripId(String emailid);

    @Query("SELECT * FROM trip where emailid=:emailid ORDER BY id DESC")
    public abstract List<Trip> selectAll(String emailid);

    @Query("SELECT * FROM trip WHERE id=:id AND emailid=:emailid")
    public abstract Trip findById(int id,String emailid);

    @Insert
    public abstract long insert(Trip trip);

    public long insertPlan(Trip trip,String emailid, List<Placee> placees) {
        long lng = insert(trip);
        if (lng != -1) {
            int id = getLastEntryTripId(emailid);
            for (Placee placee :
                    placees) {
                if (placee != null) {
                    placee.tripId = id;
                }
            }
            Placee[] convPlacees = new Placee[placees.size()];
            for (int i = 0; i < placees.size(); i++) {
                convPlacees[i] = placees.get(i);
            }
            if (convPlacees.length > 0)
                insert(convPlacees);
        }
        return lng;
    }

    //Placee
    @Query("SELECT * FROM Placee WHERE tripId=:id ORDER BY tripId DESC")
    public abstract List<Placee> selectAllPlaces(int id);

    @Insert
    public abstract void insert(Placee... placees);

}
