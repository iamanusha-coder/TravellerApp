
package com.traveller.android.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class PersonStore {

  @Query("SELECT * FROM person WHERE emailid=:emailid AND pass=:pass")
  public abstract Person findById(String emailid, String pass);
  @Query("SELECT * FROM person WHERE emailid=:emailid")
  public abstract Person findProfileById(String emailid);

  public long insertCheck(Person person, String emailid){
    long insertVal = -2;
    if (findProfileById(emailid) == null){
      insertVal = insert(person);
    }
    return insertVal;
  }
  @Insert
  public abstract long insert(Person people);

  @Update
  public abstract int update(Person people);

}
