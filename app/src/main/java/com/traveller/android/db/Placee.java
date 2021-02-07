package com.traveller.android.db;


import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.versionedparcelable.ParcelField;

import org.parceler.Parcel;

import static androidx.room.ForeignKey.CASCADE;

@Parcel
@Entity(
        tableName = "placee",
        foreignKeys = @ForeignKey(
                entity = Trip.class,
                parentColumns = "id",
                childColumns = "tripId",
                onDelete = CASCADE))

public class Placee {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int placeId;
    public int tripId;
    public String placName;
    public String address;
    public String rating;
    public String type;

    @Embedded
    public Location location;

    @Ignore
    public Placee() {
    }

    public Placee(int tripId, String placName, String address, String rating, Location location, String type) {
        this.tripId = tripId;
        this.placName = placName;
        this.address = address;
        this.rating = rating;
        this.location = location;
        this.type = type;
    }

    @Override
    public String toString() {
        return placName + "➭";
    }
/*
    @Override
    public String toString() {
        return "Placee{" +
                "placName='" + placName + '\'' +
                ", rating='" + rating + '\'' +
                ", address=" + address +
                '}';
    }
*/
}
