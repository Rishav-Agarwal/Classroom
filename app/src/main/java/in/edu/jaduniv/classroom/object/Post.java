package in.edu.jaduniv.classroom.object;

import java.util.Map;

public class Post {

    private String title;
    private String description;
    private boolean pinned;
    private String postedByNumber;
    private String postedByName;
    private Map<String, String> time;
    private Long longTime;
    private String url;
    private String fileName;
    private String resourceType;
    private String publicId;

    public Post() {
    }

    public Post(String title, String description, boolean pinned, String postedByNumber, String postedByName, Map<String, String> time, Long longTime, String url, String fileName, String resourceType, String publicId) {
        this.title = title;
        this.description = description;
        this.pinned = pinned;
        this.postedByNumber = postedByNumber;
        this.postedByName = postedByName;
        this.time = time;
        this.longTime = longTime;
        this.url = url;
        this.fileName = fileName;
        this.resourceType = resourceType;
        this.publicId = publicId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public Map<String, String> getTime() {
        return time;
    }

    public void setTime(Map<String, String> time) {
        this.time = time;
    }

    public String getPostedByNumber() {
        return postedByNumber;
    }

    public void setPostedByNumber(String postedByNumber) {
        this.postedByNumber = postedByNumber;
    }

    public String getPostedByName() {
        return postedByName;
    }

    public void setPostedByName(String postedByName) {
        this.postedByName = postedByName;
    }

    public Long getLongTime() {
        return longTime;
    }

    public void setLongTime(Long longTime) {
        this.longTime = longTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    @Override
    public String toString() {
        return "Post[" + hashCode() + "]: "
                + title + " ~ " + description
                + " :: posted by [" + postedByName + ", " + postedByNumber + "]"
                + "file?: " + fileName;
    }
}