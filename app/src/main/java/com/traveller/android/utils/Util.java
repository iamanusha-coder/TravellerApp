package com.traveller.android.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Util {

    public static final String KEY = "AIzaSyB_wJo8c8VQVH_lI7kOAGgjgaYUHX-_23U";
    public static final int AMUSEPARK_THRESHOLD = 8;

    public static long tripHour(Date from, Date to) {
        long time = -1;
        if (from.before(to)) {
            time = TimeUnit.MILLISECONDS.toHours(to.getTime()) - TimeUnit.MILLISECONDS.toHours(from.getTime());
        } else {
            long tempfrom, tempto;
            tempfrom = TimeUnit.MILLISECONDS.toHours(from.getTime());
            tempto = TimeUnit.MILLISECONDS.toHours(to.getTime());
            if (tempfrom > tempto) {
                if (tempfrom <= 12) {
                    time = 12 - tempfrom + tempto;
                } else {
                    time = 24 - tempfrom + tempto;
                }
            }
        }
        return time;
    }

    public static int getStartInterval(String from, String to) {
        int fromHour = 0, toHour = 0;
        if (from != null && !from.isEmpty()) {
            String[] fr = from.split(":");
            fromHour = Integer.parseInt(fr[0]);
        }
        if (to != null && !to.isEmpty()) {
            String[] frt = to.split(":");
            toHour = Integer.parseInt(frt[0]);
        }
        if ((fromHour >= 9 && fromHour <= 11) || (fromHour >= 12 && fromHour <= 14) ||
                (fromHour >= 17 && fromHour <= 18) || (fromHour >= 21 && fromHour <= 23)) {
            return 0;
        } else if ((fromHour >= 6 && fromHour <= 9) || (fromHour >= 11 && fromHour <= 12) ||
                (fromHour >= 14 && fromHour <= 17) || (fromHour >= 18 && fromHour <= 21)) {
            return 1;
        }
        return 4;  //night time tour plan , randomly show food places
    }
}
