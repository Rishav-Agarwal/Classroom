package in.edu.jaduniv.classroom.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.adapters.SyllabusAdapter;
import in.edu.jaduniv.classroom.other.DownloadDbHelper;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;
import in.edu.jaduniv.classroom.utility.PermissionUtils;

public class Syllabus extends AppCompatActivity {

    private static int lastPos = -1;
    DatabaseReference subjectReference;
    Toolbar toolbar;
    String classCode;
    //Intent to receive the class code
    Intent intent;
    ArrayList<in.edu.jaduniv.classroom.object.Syllabus> syllabi;
    ArrayList<String> syllabusKeys;
    SyllabusAdapter syllabusAdapter;
    GridView gvSyllabus;
    DownloadManager dm;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);

                Cursor c = dm != null ? dm.query(query) : null;
                String uri = null;
                if (c != null && c.moveToFirst()) {
                    uri = c.getString(c.getColumnIndex("local_uri"));
                }

                if (!openDoc(uri)) {
                    Toast.makeText(getApplicationContext(), "Cannot open file", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d("Exception", "Exception occured");
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus);

        setupActivity();        //Initializes variables and loads references
    }

    private void setupActivity() {
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        //Setup the toolbar
        toolbar = findViewById(R.id.toolbar_syllabus);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.up_button);
        }

        //Setup syllabus arraylist and adapter
        syllabi = new ArrayList<>();
        syllabusKeys = new ArrayList<>();
        syllabusAdapter = new SyllabusAdapter(this, syllabi, syllabusKeys, new SyllabusAdapter.PopupMenuOverflowClickListener() {
            @Override
            public void onPopupMenuOverflowClicked(View parentView, int position) {
                onPopupMenuItemCLicked(parentView, position);
            }
        });

        //Setup the GridVew
        gvSyllabus = findViewById(R.id.grid_view_syllabus);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (displayMetrics.widthPixels / displayMetrics.densityDpi) * 160;
        Log.d("WIDTH", width + "");
        /*
         * If device's width is less than 600, set number of columns in the GridView to be 3
         * If device's width is greater than or equal to 600 but less than 700, set the number of columns to be 4
         * If width is greater than or equal to 700, set the number of columns to be 5
         */
        if (width >= 400)
            gvSyllabus.setNumColumns(3);
        if (width >= 600)
            gvSyllabus.setNumColumns(4);
        if (width >= 700)
            gvSyllabus.setNumColumns(5);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.class_item_anim);
        GridLayoutAnimationController animationController = new GridLayoutAnimationController(anim);
        gvSyllabus.setLayoutAnimation(animationController);
        gvSyllabus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get download reference for the syllabus if available
                DownloadDbHelper helper = DownloadDbHelper.getInstance(getApplicationContext());
                Long downloadReference = helper.getDownloadId(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject());
                Log.d("downloadReference", downloadReference + "");

                long downloadId = 0;

                if (dm != null && downloadReference != null) {
                    //Perform actions depending on the status of the download
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadReference);

                    Cursor cursor = dm.query(query);
                    Log.d("cursor", cursor + "");
                    if (cursor == null || !cursor.moveToFirst()) {
                        Log.d("downloadId", downloadId + "");
                        Long _id = downloadFile(position);
                        if (_id == null)
                            return;
                        downloadId = _id;
                        DownloadDbHelper.getInstance(getApplicationContext()).addDownload(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject(), downloadId);
                        return;
                    }
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int downloadStatus = cursor.getInt(columnIndex);
                    int filePathInt = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    String filePath = cursor.getString(filePathInt);

                    Long _id;
                    switch (downloadStatus) {
                        case DownloadManager.STATUS_FAILED:
                            Log.d("Download status", "STATUS_FAILED");
                        case DownloadManager.ERROR_FILE_ERROR:
                            _id = downloadFile(position);
                            if (_id == null)
                                break;
                            downloadId = _id;
                            Log.d("Download status", "ERROR_FILE_ERROR");
                            boolean deleted = deleteDoc(filePath);
                            DownloadDbHelper.getInstance(getApplicationContext()).addDownload(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject(), downloadId);
                            break;

                        case DownloadManager.STATUS_SUCCESSFUL:
                            Log.d("Download status", "STATUS_SUCCESSFUL");
                            if (filePath != null) {
                                if (!openDoc(filePath)) {
                                    boolean isDeleted = deleteDoc(filePath);
                                    _id = downloadFile(position);
                                    if (_id == null)
                                        break;
                                    downloadId = _id;
                                    DownloadDbHelper.getInstance(getApplicationContext()).addDownload(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject(), downloadId);
                                }
                            } else {
                                _id = downloadFile(position);
                                if (_id == null)
                                    break;
                                downloadId = _id;
                                DownloadDbHelper.getInstance(getApplicationContext()).addDownload(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject(), downloadId);
                            }
                            break;

                        case DownloadManager.STATUS_PENDING:
                        case DownloadManager.STATUS_RUNNING:
                        case DownloadManager.STATUS_PAUSED:
                            Log.d("Download status", "STATUS_PENDING | RUNNING | PAUSED");
                            dm.remove(downloadReference);
                            boolean isDeleted = deleteDoc(filePath);
                            _id = downloadFile(position);
                            if (_id == null)
                                break;
                            downloadId = _id;
                            DownloadDbHelper.getInstance(getApplicationContext()).addDownload(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject(), downloadId);
                            break;
                    }
                } else {
                    Log.d("downloadId", downloadId + "");
                    Long _id = downloadFile(position);
                    if (_id == null)
                        return;
                    downloadId = _id;
                    DownloadDbHelper.getInstance(getApplicationContext()).addDownload(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject(), downloadId);
                }
            }
        });
        gvSyllabus.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onPopupMenuItemCLicked(view, position);
                return true;
            }
        });
        gvSyllabus.setAdapter(syllabusAdapter);
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //Receive class code through intent
        intent = getIntent();
        classCode = intent.getStringExtra("class");

        //Setup reference to the syllabus of the class stored in Firebase Database
        subjectReference = FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("syllabus");
        subjectReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                in.edu.jaduniv.classroom.object.Syllabus syllabus = dataSnapshot.getValue(in.edu.jaduniv.classroom.object.Syllabus.class);
                syllabusAdapter.add(syllabus, dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                in.edu.jaduniv.classroom.object.Syllabus syllabus = dataSnapshot.getValue(in.edu.jaduniv.classroom.object.Syllabus.class);
                int index = syllabusKeys.indexOf(dataSnapshot.getKey());
                syllabi.set(index, syllabus);
                syllabusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //in.edu.jaduniv.classroom.object.Syllabus syllabus = dataSnapshot.getValue(in.edu.jaduniv.classroom.object.Syllabus.class);
                int index = syllabusKeys.indexOf(dataSnapshot.getKey());
                syllabi.remove(index);
                syllabusKeys.remove(index);
                syllabusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Long downloadWithPermission(int position) {
        Long downloadId = null;
        final in.edu.jaduniv.classroom.object.Syllabus syllabus = syllabi.get(position);
        if (syllabus != null) {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(syllabus.getUrl()));
                request.setDescription(classCode + "_Syllabus_" + ((in.edu.jaduniv.classroom.object.Syllabus) syllabusAdapter.getItem(position)).getSubject());
                request.setTitle("Classroom");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("", "/Classroom/" + classCode + "/Syllabus/" + syllabus.getFileName());
                DownloadManager manager = (DownloadManager) getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                if (manager != null) {
                    downloadId = manager.enqueue(request);
                }
            } catch (Exception e) {
                Log.e("Error downloading file", e.getMessage());
                e.printStackTrace();
            }
        }
        return downloadId;
    }

    private Long downloadFile(int position) {
        if (PermissionUtils.isPermitted(this, PermissionUtils.Permissions.WRITE_EXTERNAL_STORAGE))
            return downloadWithPermission(position);
        lastPos = position;
        PermissionUtils.request(this, PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.Permissions.WRITE_EXTERNAL_STORAGE);
        return null;
    }

    private boolean openDoc(String uri) {
        Intent open = new Intent(Intent.ACTION_VIEW);
        open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
        if (uri != null && extension != null) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            open.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(Uri.parse(uri).getPath())), mimeType);
            Intent openChooser = Intent.createChooser(open, "Choose app");
            startActivity(openChooser);
            return true;
        }
        return false;
    }

    private boolean deleteDoc(String uri) {
        boolean deleted = false;
        try {
            File file = new File(Uri.parse(uri).getPath());
            deleted = file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("File deleted?", deleted + "");
        return deleted;
    }

    @SuppressLint("RestrictedApi")
    private void onPopupMenuItemCLicked(View anchor, final int position) {
        MenuBuilder menuBuilder = new MenuBuilder(Syllabus.this);
        MenuInflater menuInflater = new MenuInflater(Syllabus.this);
        menuInflater.inflate(R.menu.menu_syllabus_item, menuBuilder);
        MenuPopupHelper downloadMenu = new MenuPopupHelper(Syllabus.this, menuBuilder, anchor);
        downloadMenu.setForceShowIcon(true);

        // Set Item Click Listener
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                Long _id = downloadFile(position);
                if (_id == null)
                    return false;
                long downloadId = _id;
                DownloadDbHelper.getInstance(getApplicationContext()).addDownload(getPackageName() + "." + classCode + ".Syllabus." + ((in.edu.jaduniv.classroom.object.Syllabus) (syllabusAdapter.getItem(position))).getSubject(), downloadId);
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }
        });

        // Display the menu
        downloadMenu.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadWithPermission(lastPos);
            } else {
                Toast.makeText(this, "Permission denied! :(\nWe need your consent to do that.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}