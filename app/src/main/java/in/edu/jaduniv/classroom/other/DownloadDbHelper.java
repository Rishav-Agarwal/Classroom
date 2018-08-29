package in.edu.jaduniv.classroom.other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public final class DownloadDbHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "DownloadsManager";

    // Contacts table name
    private static final String TABLE_DOWNLOADS = "downloads";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_FILE = "file";
    private static final String KEY_DOWNLOAD_ID = "download_id";

    private static DownloadDbHelper downloadDbHelper = null;

    private DownloadDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DownloadDbHelper getInstance(Context context) {
        if (downloadDbHelper == null) {
            downloadDbHelper = new DownloadDbHelper(context);
        }
        return downloadDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create table command
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_DOWNLOADS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_FILE + " TEXT,"
                + KEY_DOWNLOAD_ID + " INTEGER" + ")";

        //Execute create table command
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADS);

        // Create tables again
        onCreate(db);
    }

    // Adding new download
    public synchronized void addDownload(String file, long downloadId) {
        Log.d("Adding download", file + "::" + downloadId);
        SQLiteDatabase db = this.getWritableDatabase();

        Long downloadRef = getDownloadId(file);
        if (downloadRef != null) {
            updateDownload(file, downloadId);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_FILE, file);
        values.put(KEY_DOWNLOAD_ID, downloadId);

        // Inserting Row
        db.insert(TABLE_DOWNLOADS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single download id
    public synchronized Long getDownloadId(String file) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DOWNLOADS, new String[]{KEY_ID,
                        KEY_FILE, KEY_DOWNLOAD_ID}, KEY_FILE + "=?",
                new String[]{file}, null, null, null, null);
        Long downloadId = null;

        if (cursor != null && cursor.moveToFirst()) {
            downloadId = cursor.getLong(cursor.getColumnIndex(KEY_DOWNLOAD_ID));
            cursor.close();
        }
        // return download id
        return downloadId;
    }

    // Updating single download
    public synchronized int updateDownload(String file, long downloadReference) {
        Log.d("Updating download", file + "::" + downloadReference);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FILE, file);
        values.put(KEY_DOWNLOAD_ID, downloadReference);

        // updating row
        return db.update(TABLE_DOWNLOADS, values, KEY_FILE + " = ?",
                new String[]{file});
    }

    // Deleting single download
    public synchronized void deleteDownload(String file) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOWNLOADS, KEY_FILE + " = ?",
                new String[]{file});
        db.close();
    }
}
