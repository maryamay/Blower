package com.codisats.blower;

import java.util.Calendar;

public class LocationEntry {
    public double latitude;
    public double longitude;
    public Calendar calendar;
    public String day;

    public LocationEntry(Calendar calendar, double latitude, double longitude) {
        this.calendar = calendar;
        this.latitude = latitude;
        this.longitude = longitude;
        this.day = Utils.getHashedDay(calendar);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocationEntry that = (LocationEntry) o;

        if (calendar.getTimeInMillis() != that.calendar.getTimeInMillis()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return calendar.hashCode();
    }
}
