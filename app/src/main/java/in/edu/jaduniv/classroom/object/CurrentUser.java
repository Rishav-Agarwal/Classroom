package in.edu.jaduniv.classroom.object;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * *Final* *singleton* class `CurrentUser`. It keeps record of the current user signed-in.
 * Every request of user data should be done to this class(Even data stored in Firebase's `FirebaseUser` class).
 * This ensures that data is always there if user is logged-in otherwise asks user to login.
 */
public final class CurrentUser extends User {

    /**
     * Private static object of `CurrentUser`
     */
    private static CurrentUser currentUser = null;

    /**
     * Private constructor so that its object can't be created externally
     */
    private CurrentUser() {
    }

    /**
     * It creates object only if initially it is `null`. Thus ensuring that only one object is created.
     *
     * @return An instance(the only instance) of `CurrentUser`.
     */
    public static synchronized CurrentUser getInstance() {
        if (currentUser == null)
            currentUser = new CurrentUser();
        return currentUser;
    }

    /**
     * Get `FirebaseUser` for internal use
     *
     * @return A `FirebaseUser` object if user is signed-in otherwise `null`.
     */
    public static FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Checks if user is already signed-in?
     * If we get `null` `FirebaseUser`, user in not signed in.
     */
    public boolean isSignedIn() {
        return getFirebaseUser() != null;
    }

    /**
     * Returns phone number of the user.
     *
     * @return A string containing phone number of the user
     */
    public String getPhone() {
        return getFirebaseUser().getPhoneNumber();
    }

    /**
     * Return name of the user.
     *
     * @return A string containing name of the user.
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * Set name of the user.
     */
    @Override
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * Return email of the user.
     *
     * @return A string containing email of the user.
     */
    @Override
    public String getEmail() {
        return super.getEmail();
    }

    /**
     * Set email of the user.
     */
    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }

    /**
     * Return classes of the user.
     *
     * @return An Array of string containing classes of the user.
     */
    @Override
    public ArrayList<String> getClasses() {
        return super.getClasses();
    }

    /**
     * Set classes of the user.
     */
    @Override
    public void setClasses(ArrayList<String> classes) {
        super.setClasses(classes);
    }

    /**
     * Return token of the user.
     *
     * @return A string containing token of the user.
     */
    @Override
    public String getToken() {
        return super.getToken();
    }

    /**
     * Set token of the user.
     */
    @Override
    public void setToken(String token) {
        super.setToken(token);
    }

    /**
     * Return uid of the user.
     *
     * @return A string containing uid of the user.
     */
    public String getUid() {
        return getFirebaseUser().getUid();
    }
}