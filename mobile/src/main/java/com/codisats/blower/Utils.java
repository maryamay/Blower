package com.codisats.blower;

import java.util.Calendar;

public class Utils {
    private Utils() {
    }

    /**
     * Builds a simple hash for a day by concatenating year and day of year together. Note that two
     * {@link Calendar} inputs that fall on the same day will be hashed to the same
     * string.
     */
    public static String getHashedDay(Calendar day) {
        return day.get(Calendar.YEAR) + "-" + day.get(Calendar.DAY_OF_YEAR);
    }

}
