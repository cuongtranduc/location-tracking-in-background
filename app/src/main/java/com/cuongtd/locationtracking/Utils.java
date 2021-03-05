package com.cuongtd.locationtracking;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
    }
}
