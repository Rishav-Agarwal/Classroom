package in.edu.jaduniv.classroom.utility;

import android.util.SparseArray;

public final class TimeUtils {

    private static final String[] days;
    private static final SparseArray<String> times = new SparseArray<>();

    static {
        times.put(0, "12 AM");
        times.put(1, "01 AM");
        times.put(2, "02 AM");
        times.put(3, "03 AM");
        times.put(4, "04 AM");
        times.put(5, "05 AM");
        times.put(6, "06 AM");
        times.put(7, "07 AM");
        times.put(8, "08 AM");
        times.put(9, "09 AM");
        times.put(10, "10 AM");
        times.put(11, "11 AM");
        times.put(12, "12 PM");
        times.put(13, "01 PM");
        times.put(14, "02 PM");
        times.put(15, "03 PM");
        times.put(16, "04 PM");
        times.put(17, "05 PM");
        times.put(18, "06 PM");
        times.put(19, "07 PM");
        times.put(20, "08 PM");
        times.put(21, "09 PM");
        times.put(22, "10 PM");
        times.put(23, "11 PM");

        days = new String[]{
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday"
        };
    }

    public static String getDisplayableTime(int time) {
        return time >= 0 && time <= 23 ? times.get(time) : "?";
    }

    public static String getDay(int index) {
        return index >= 0 && index <= 5 ? days[index] : "?";
    }
}