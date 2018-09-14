package in.edu.jaduniv.classroom.object;

import java.io.Serializable;

public final class __Class implements Serializable {
    private int day;
    private int length;
    private String name = null;
    private String description = null;
    private String prof = null;
    private String location = null;
    private String startTime = null;
    private String endTime = null;

    public __Class() {
    }

    public __Class(int day, int length, String name, String description, String prof, String location, String startTime, String endTime) {
        this.day = day;
        this.length = length;
        this.name = name;
        this.description = description;
        this.prof = prof;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProf() {
        return prof;
    }

    public void setProf(String prof) {
        this.prof = prof;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof __Class))
            return false;
        __Class _class = (__Class) obj;
        return this == _class
                || day == _class.day
                && length == _class.length
                && name.equals(_class.name)
                && description.equals(_class.description)
                && prof.equals(_class.prof)
                && location.equals(_class.location)
                && startTime.equals(_class.startTime)
                && endTime.equals(_class.endTime);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}