package in.edu.jaduniv.classroom.object;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * *Final* *singleton* class `CurrentUser`. It keeps record of the current user signed-in.
 * Every request of user data should be done to this class(Even data stored in Firebase's `FirebaseUser` class).
 * This ensures that data is always there if user is logged-in otherwise asks user to login.
 * `signIn()` should be called before any other call to this class.
 */
public final class CurrentUser extends User {

    //private static boolean hasSignInCalled = false;

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
     * It checks whether `signIn()` has been called or not. If not, throws an `IllegalAccessException`
     * This ensures that user is signed-in before any information request.
     *
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    /*private static synchronized void checkSignInCall() throws IllegalAccessException {
        if (!hasSignInCalled)
            throw new IllegalAccessException("Should call CurrentUser.signIn() before any other call to CurrentUser.");
    }*/

    /**
     * It creates object only if initially it is `null`. Thus ensuring that only one object is created.
     *
     * @return An instance(the only instance) of `CurrentUser`.
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    public static synchronized CurrentUser getInstance() throws IllegalAccessException {
        //checkSignInCall();
        if (currentUser == null)
            currentUser = new CurrentUser();
        return currentUser;
    }

    /**
     * It ensures that user is logged-in by setting up tha Firebase Auth listener
     * @param context - From where sign-in confirmation is requested.
     */
    /*
    public static synchronized void signIn(Context context) {
        if (!hasSignInCalled) {
            FirebaseUtils.setupFirebaseAuth(context);
            hasSignInCalled = true;
        }
    }*/

    /**
     * Get `FirebaseUser` for internal use
     *
     * @return A `FirebaseUser` object if user is signed-in otherwise `null`.
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    public static FirebaseUser getFirebaseUser() throws IllegalAccessException {
        //checkSignInCall();
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Checks if user is already signed-in?
     * If we get `null` `FirebaseUser`, user in not signed in.
     *
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    private boolean isSignedIn() throws IllegalAccessException {
        //checkSignInCall();
        return getFirebaseUser() != null;
    }

    /**
     * Returns phone number of the user.
     *
     * @return A string containing phone number of the user
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    public String getPhone() throws IllegalAccessException {
        //checkSignInCall();
        return getFirebaseUser().getPhoneNumber();
    }

    /**
     * Return name of the user.
     *
     * @return A string containing name of the user.
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public String getName() throws IllegalAccessException {
        //checkSignInCall();
        return super.getName();
    }

    /**
     * Set name of the user.
     *
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public void setName(String name) throws IllegalAccessException {
        //checkSignInCall();
        super.setName(name);
    }

    /**
     * Return email of the user.
     *
     * @return A string containing email of the user.
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public String getEmail() throws IllegalAccessException {
        //checkSignInCall();
        return super.getEmail();
    }

    /**
     * Set email of the user.
     *
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public void setEmail(String email) throws IllegalAccessException {
        //checkSignInCall();
        super.setEmail(email);
    }

    /**
     * Return classes of the user.
     *
     * @return An Array of string containing classes of the user.
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public ArrayList<String> getClasses() throws IllegalAccessException {
        //checkSignInCall();
        return super.getClasses();
    }

    /**
     * Set classes of the user.
     *
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public void setClasses(ArrayList<String> classes) throws IllegalAccessException {
        //checkSignInCall();
        super.setClasses(classes);
    }

    /**
     * Return token of the user.
     *
     * @return A string containing token of the user.
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public String getToken() throws IllegalAccessException {
        //checkSignInCall();
        return super.getToken();
    }

    /**
     * Set token of the user.
     *
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    @Override
    public void setToken(String token) throws IllegalAccessException {
        //checkSignInCall();
        super.setToken(token);
    }

    /**
     * Return uid of the user.
     *
     * @return A string containing uid of the user.
     * @throws IllegalAccessException If `signIn()` has not been called.
     */
    public String getUid() throws IllegalAccessException {
        //checkSignInCall();
        return getFirebaseUser().getUid();
    }
}