
package com.traveller.android.db;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "trip")
public class Trip implements Serializable {
  @PrimaryKey(autoGenerate = true)
  @NonNull
  public int id;
  public final String emailid;
  public final String areaName;
  public final String from, to, placename;
  public final int diameter;

  public Trip(String emailid, String areaName, String from, String to, String placename, int diameter) {
    this.emailid = emailid;
    this.areaName = areaName;
    this.from = from;
    this.to = to;
    this.placename = placename;
    this.diameter = diameter;
  }

  @Override
  public String toString() {
    return((placename!=null?placename.toUpperCase():"Placee...") +"\nnear "+(areaName!=null?areaName+"...\n":"...\n")+"Time: "+from+" - "+to);
  }
}
