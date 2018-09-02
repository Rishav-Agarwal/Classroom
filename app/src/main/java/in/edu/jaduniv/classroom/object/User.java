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

    User(String name, String uid, String phone, String email, String token, ArrayList<String> classes) {
        this.name = name;
        this.email = email;
        this.token = token;
        this.classes = classes;
    }

    public String getName() throws IllegalAccessException {
        return name;
    }

    public void setName(String name) throws IllegalAccessException {
        this.name = name;
    }

    public String getEmail() throws IllegalAccessException {
        return email;
    }

    public void setEmail(String email) throws IllegalAccessException {
        this.email = email;
    }

    public ArrayList<String> getClasses() throws IllegalAccessException {
        return classes;
    }

    public void setClasses(ArrayList<String> classes) throws IllegalAccessException {
        this.classes = classes;
    }

    public String getToken() throws IllegalAccessException {
        return token;
    }

    public void setToken(String token) throws IllegalAccessException {
        this.token = token;
    }
}