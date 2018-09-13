package in.edu.jaduniv.classroom.object;

import java.util.ArrayList;

/**
 * Class to store a single user's data
 */
public class User {
    private String name;
    private String email;
    private String token;
    private ArrayList<String> classes;

    public User() {
        name = null;
        email = null;
        classes = null;
    }

    User(String name, String email, String token, ArrayList<String> classes) {
        this.name = name;
        this.email = email;
        this.token = token;
        this.classes = classes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    public void setClasses(ArrayList<String> classes) {
        this.classes = classes;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}