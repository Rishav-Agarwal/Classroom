package in.edu.jaduniv.classroom.utility;

import android.support.annotation.NonNull;

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
                            Boolean fetchedAdmin = dataSnapshot.getValue(Boolean.class);
                            isAdmin = false;
                            if (fetchedAdmin != null)
                                isAdmin = fetchedAdmin;
                            synchronized (AdminState.this) {
                                valueEventListener.onValueChanged(isAdmin, null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
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
}