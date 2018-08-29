package in.edu.jaduniv.classroom.object;

public class Participant {
    private boolean admin;
    private String phone;
    private String name;

    public Participant() {}

    public Participant(boolean admin, String phone, String name) {
        this.admin = admin;
        this.phone = phone;
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
