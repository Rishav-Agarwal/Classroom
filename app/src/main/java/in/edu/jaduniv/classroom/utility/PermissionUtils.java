package in.edu.jaduniv.classroom.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.IntRange;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class for Permissions required for this app.
 */
public final class PermissionUtils {

    /**
     * Request code for Storage permission.
     */
    public static final int WRITE_EXTERNAL_STORAGE = 999;

    /**
     * Check if given permission is granted.
     *
     * @param context    - From where the request is called
     * @param permission - Permission which is to be checked
     * @return A boolean specifying if the permission is granted
     */
    public static boolean isPermitted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests all the permissions given in String array.
     *
     * @param activity    - From where the request in called
     * @param permissions - Array of String representing permissions to ask for
     */
    public static void request(Activity activity, @IntRange(from = 0) int requestCode, String... permissions) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * This stores all the required permissions for our app.
     */
    public static final class Permissions {

        /**
         * Permission for Storage.
         */
        public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }
}