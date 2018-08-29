package in.edu.jaduniv.classroom.object;

public class Syllabus {

    private String subject;
    private String url;
    private String fileName;

    public Syllabus() {
    }

    public Syllabus(String subject, String url, String fileName) {
        this.subject = subject;
        this.url = url;
        this.fileName = fileName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
}
