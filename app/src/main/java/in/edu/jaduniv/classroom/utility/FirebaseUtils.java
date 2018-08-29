package in.edu.jaduniv.classroom.utility;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.edu.jaduniv.classroom.object.CurrentUser;

/**
 * Utility functions of Firebase
 */
public final class FirebaseUtils {

    public static int PERMISSION_STORAGE = 2000;

    //private static FirebaseAuth.AuthStateListener authStateListener = null;

    //Stores our database instance
    private static FirebaseDatabase mDatabase = null;

    //Stores our database's reference
    private static DatabaseReference mReference = null;

    //  private static boolean isOnline = false;

    //Stores current user
    //private static CurrentUser currentUser = null;

    /**
     * - Returns database instance.
     * - Allows only one instance to be created.
     * - If instance is not created, it creates one, enables caching and returns
     * otherwise returns the created instance.
     */
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    /**
     * - Returns database reference.
     * - Allows only one instance of db reference.
     * - If instance is not created, it creates one and returns
     * otherwise returns the created instance.
     */
    public static DatabaseReference getDatabaseReference() {
        if (mReference == null) {
            mReference = FirebaseUtils.getDatabase().getReference();
            mReference.keepSynced(true);
            /* Get connection status
            mReference.child(".info/connected").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean isConnected = dataSnapshot.getValue(boolean.class);
                    isOnline = isConnected;
                    if (isConnected) {
                        Log.d("Connection", "Connected!");
                    } else {
                        Log.d("Connection", "Disconnected!");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });*/
        }
        return mReference;
    }

    /* Function to return online status
    public static boolean isOnline() {
        return isOnline;
    } */

    /*
    //Sets the current user
    public static void setCurrentUser(CurrentUser user) {
        currentUser = user;
    }*/

    /*
    //Returns the current user

    public static CurrentUser getCurrentUser() {
        if (currentUser == null) {
            currentUser = CurrentUser.getInstance();
        }
        return currentUser;
    }*/

    /*public static FirebaseAuth.AuthStateListener getAuthStateListener() {
        return authStateListener;
    }*/

    /*public static synchronized void setupFirebaseAuth(final Context context) {
        if (authStateListener == null) {
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        //try {
                            //TODO: User logged-in already.
                            //CurrentUser.getInstance().setPhone(in.edu.jaduniv.classroom.object.CurrentUser.getInstance().getPhone(context));
                        //} catch (IllegalAccessException e) {
                        //    Log.e("User not defined", "Probably user is not signed in");
                       // }
                    } else {
                        Intent loginIntent = new Intent(context, LoginInfoActivity.class);
                        ((Activity) context).startActivityForResult(loginIntent, LoginInfoActivity.RC_LOGIN_INFO);
                    }
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        }
    }*/
/*
    //Setup Firebase's AuthStateListener
    public static void setupFirebaseAuthStateListener(final Context context, final Activity activity, final FirebaseAuthChangeListener listener) {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                getCurrentUser().setFirebaseUser(firebaseAuth.getCurrentUser());
                if (CurrentUser.firebaseUser != null) {
                    if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_STORAGE);
                    }
                    listener.onAlreadySigned();
                } else {
                    listener.onSignIn();
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }*/

    /**
     * Contains state of admin and performs required tasks
     */
    public static class AdminState {

        //Stores the admin state
        private boolean isAdmin;

        //Stores the code of the class
        private String classCode;

        //ValueEventListener whose method - onValueChanged is triggered when admin state changes
        private in.edu.jaduniv.classroom.interfaces.ValueEventListener valueEventListener = null;

        /**
         * - Constructor which takes in the class code and Value event listener object
         * and attaches the admin state change listener
         */
        public AdminState(String classCode, in.edu.jaduniv.classroom.interfaces.ValueEventListener valueEventListener) throws IllegalAccessException {
            this.classCode = classCode;
            this.valueEventListener = valueEventListener;
            attachAdminStateChangeListener();
        }

        /**
         * Updates ValueEventListener
         */
        public synchronized void setValueEventListener(in.edu.jaduniv.classroom.interfaces.ValueEventListener valueEventListener) {
            this.valueEventListener = valueEventListener;
        }

        /**
         * Method to attach the admin state change listener to the database
         */
        private synchronized void attachAdminStateChangeListener() throws IllegalAccessException {
            getDatabaseReference().child("classes")
                    .child(classCode)
                    .child("participants")
                    .child(CurrentUser.getInstance().getPhone())
                    .child("admin")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            isAdmin = dataSnapshot.getValue(Boolean.class);
                            synchronized (AdminState.this) {
                                valueEventListener.onValueChanged(isAdmin, null);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }

        /**
         * Returns if the user is the admin of this class (@classCode)
         */
        public synchronized boolean isAdmin() {
            return isAdmin;
        }
    }

    /*public static final class CurrentUser {
        private static User user = null;
        private static FirebaseUser firebaseUser = null;
        private static CurrentUser currentUser = null;

        private CurrentUser() {
            user = null;
            firebaseUser = null;
        }

        private CurrentUser(User user, FirebaseUser firebaseUser) {
            CurrentUser.user = user;
            CurrentUser.firebaseUser = firebaseUser;
        }

        public static CurrentUser getInstance() {
            if (currentUser == null) {
                currentUser = new CurrentUser();
            }
            return currentUser;
        }

        public static CurrentUser getInstance(User user, FirebaseUser firebaseUser) {
            if (currentUser == null) {
                currentUser = new CurrentUser(user, firebaseUser);
            }
            return currentUser;
        }

        public User getUser() {
            if (user == null) {
                user = new User();
            }
            return user;
        }

        public void setUser(User user) {
            CurrentUser.user = user;
        }

        public FirebaseUser getFirebaseUser() {
            if (firebaseUser == null) {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            }
            return firebaseUser;
        }

        public void setFirebaseUser(FirebaseUser firebaseUser) {
            CurrentUser.firebaseUser = firebaseUser;
        }
    }*/
}