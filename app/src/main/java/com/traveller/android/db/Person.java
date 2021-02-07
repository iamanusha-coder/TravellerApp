package com.traveller.android.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "person")
public class Person {
  @PrimaryKey
  @NonNull
  public final String emailid;
  public final String phone;
  public final String fullname;
  public final String address;
  public final String cuisines;
  public final String pass;
  public final boolean isNonveg;
  public final boolean isDrinker;

  public Person(@NonNull String emailid, String phone, String fullname, String address, String pass, boolean isNonveg, boolean isDrinker, String cuisines) {
    this.emailid = emailid;
    this.phone = phone;
    this.fullname = fullname;
    this.address = address;
    this.pass = pass;
    this.isNonveg = isNonveg;
    this.isDrinker = isDrinker;
    this.cuisines = cuisines;
  }

  @Override
  public String toString() {
    return(cuisines);
  }
}
