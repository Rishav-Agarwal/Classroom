package in.edu.jaduniv.classroom.object;

public class JoinRequest {
    private String name;
    private String phone;

    public JoinRequest() {}

    public JoinRequest(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}