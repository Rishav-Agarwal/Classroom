package in.edu.jaduniv.classroom.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.adapters.PostAdapter;
import in.edu.jaduniv.classroom.fragment.FileSelectedFragment;
import in.edu.jaduniv.classroom.helper.FileUploadHelper;
import in.edu.jaduniv.classroom.interfaces.OnCompleteListener;
import in.edu.jaduniv.classroom.object.CurrentUser;
import in.edu.jaduniv.classroom.object.Post;
import in.edu.jaduniv.classroom.utility.CloudinaryUtils;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;
import in.edu.jaduniv.classroom.utility.PermissionUtils;

public class EventAndNotice extends AppCompatActivity {

    private static final int RC_CHOOSE_FILE = 1000;
    public static String attachedUri = null, attachedFileName = null, attachedMimeType = null;
    public static File attachedFile = null;
    public static OnCompleteListener listener;
    private static EventAndNotice eventAndNotice = null;

    //ListView which contains the posts
    ListView lvPosts;
    //Adapter for the ListView `lvPosts`
    PostAdapter postArrayAdapter;
    ArrayList<Post> postArrayList;
    ArrayList<String> postKeys;
    //Support action bar
    android.support.v7.widget.Toolbar toolbar;

    //EditText which sends the message
    EditText etSendPost;
    ImageView ivSendPost, ivSendFile;

    DatabaseReference mPostReference;
    FirebaseAuth.AuthStateListener authStateListener = null;
    //Contains class code (not name)
    private String classCode;
    private FirebaseUtils.AdminState adminState;
    private FileSelectedFragment sendFragment;

    public static EventAndNotice getInstance() {
        if (eventAndNotice == null) {
            eventAndNotice = new EventAndNotice();
        }
        return eventAndNotice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_and_notice);

        Intent intent = getIntent();
        classCode = intent.getStringExtra("class");

        attachedUri = intent.getStringExtra("uri");
        attachedFileName = intent.getStringExtra("fileName");
        attachedFile = (File) intent.getSerializableExtra("file");
        attachedMimeType = intent.getType();
        if (attachedUri != null) {
            sendFragment = new FileSelectedFragment();
            Bundle bundle = new Bundle();
            bundle.putString("fileName", attachedFileName != null && !attachedFileName.equals("null") ? attachedFileName : "Untitled");
            sendFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frame_selectedFile, sendFragment);
            fragmentTransaction.commit();
        }

        try {
            initializeVariables();      //Initialize the variables
        } catch (IllegalAccessException e) {
            Log.e("User not defined", "Probably user is not signed in");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected()) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        if (authStateListener == null) {
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser firebaseUser = CurrentUser.getFirebaseUser();
                    if (firebaseUser == null) {
                        if (isNetworkConnected()) {
                            Intent loginIntent = new Intent(EventAndNotice.this, LoginInfoActivity.class);
                            startActivityForResult(loginIntent, LoginInfoActivity.RC_LOGIN_INFO);
                        } else {
                            Toast.makeText(getApplicationContext(), "Check your internet connection!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission denied! :(\nWe need your consent to do that.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CHOOSE_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d("data", data.toString());
            Cursor cursor = queryDoc(getContentResolver(), data.getData());
            if (cursor == null) {
                return;
            }
            attachedMimeType = cursor.getString(cursor.getColumnIndex("mime_type"));
            try {
                attachedFile = handleIntent(data);
            } catch (IOException e) {
                Log.d("Couldn't get file", e.getMessage());
                e.printStackTrace();
                return;
            }
            attachedUri = String.valueOf(data.getData());
            attachedFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            Log.d("Mime", String.valueOf(attachedMimeType));
            Log.d("File", String.valueOf(attachedFile));

            sendFragment = new FileSelectedFragment();
            Bundle bundle = new Bundle();
            bundle.putString("fileName", attachedFileName);
            sendFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frame_selectedFile, sendFragment);
            fragmentTransaction.commit();
        }
    }

    private void initializeVariables() throws IllegalAccessException {
        lvPosts = findViewById(R.id.lv_posts);
        postArrayList = new ArrayList<>();
        postKeys = new ArrayList<>();
        postArrayAdapter = new PostAdapter(this, R.layout.post, postArrayList, classCode);
        lvPosts.setAdapter(postArrayAdapter);

        toolbar = findViewById(R.id.post_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.up_button);
        }

        mPostReference = FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("posts");

        etSendPost = findViewById(R.id.et_send_post);
        ivSendPost = findViewById(R.id.iv_send_post);
        ivSendFile = findViewById(R.id.iv_attach_post);

        setOnClickListeners();      //Listener for click event of views
        setPostAndAdminListener();          //Listener for post add and change

        adminState = new FirebaseUtils.AdminState(classCode, new in.edu.jaduniv.classroom.interfaces.ValueEventListener() {
            @Override
            public void onValueChanged(Object newValue, Object oldValue) {
                Log.d("Admin", newValue.toString());
                supportInvalidateOptionsMenu();
            }
        });
    }

    private void setOnClickListeners() {
        //When send button is clicked, send the post if admin else push it to post requests
        ivSendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkConnected()) {
                    if (!PermissionUtils.isPermitted(EventAndNotice.this, PermissionUtils.Permissions.WRITE_EXTERNAL_STORAGE)) {
                        PermissionUtils.request(EventAndNotice.this, PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.Permissions.WRITE_EXTERNAL_STORAGE);
                    } else {
                        String strPost = etSendPost.getText().toString().trim();
                        etSendPost.setText("");
                        Intent fileUploadService = new Intent(EventAndNotice.this, FileUploadHelper.class);
                        fileUploadService.setAction(CloudinaryUtils.ACTION_POST_UPLOAD);
                        Bundle fileUploadBundle = new Bundle();
                        fileUploadBundle.putString("title", "");
                        fileUploadBundle.putString("content", strPost);
                        fileUploadBundle.putBoolean("pinned", false);
                        fileUploadBundle.putString("phone", CurrentUser.getInstance().getPhone());
                        fileUploadBundle.putString("name", CurrentUser.getInstance().getName());
                        fileUploadBundle.putLong("longTime", 0L);
                        fileUploadBundle.putString("uri", attachedUri);
                        fileUploadBundle.putString("fileName", attachedFileName);
                        fileUploadBundle.putSerializable("file", attachedFile);
                        fileUploadBundle.putString("mime", attachedMimeType);
                        fileUploadBundle.putString("classCode", classCode);
                        listener = new OnCompleteListener() {
                            @Override
                            public void onUploadCompleted(Post post) {
                                Log.d("Post", post.toString());
                                if (adminState.isAdmin()) {
                                    FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("posts").push().setValue(post);
                                } else {
                                    FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("post_req").push().setValue(post).addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(), "Post request sent!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        };
                        fileUploadService.putExtra("uploadData", fileUploadBundle);

                        startService(fileUploadService);
                        if (sendFragment != null) {
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                            fragmentTransaction.remove(sendFragment);
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                        setSendFragment(null);
                        attachedUri = attachedFileName = attachedMimeType = null;
                        attachedFile = null;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileSelectIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                fileSelectIntent.setType("*/*")
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "audio/*", "video/*", "application/*"});
                Intent fileIntent = Intent.createChooser(fileSelectIntent, "Choose file");
                startActivityForResult(fileIntent, RC_CHOOSE_FILE);
            }
        });
    }

    private String getFileName(Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()))
            return queryName(getContentResolver(), uri);
        return new File(Uri.parse(attachedUri).getPath()).getName();
    }

    private void setPostAndAdminListener() {
        mPostReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                String title = (String) dataSnapshot.child("title").getValue();
                String description = (String) dataSnapshot.child("description").getValue();
                boolean pinned = (boolean) dataSnapshot.child("pinned").getValue();
                String postedByNumber = (String) dataSnapshot.child("postedByNumber").getValue();
                String postedByName = (String) dataSnapshot.child("postedByName").getValue();
                Long time = (Long) dataSnapshot.child("time").getValue();
                String uri = String.valueOf(dataSnapshot.child("url").getValue());
                String fileName = String.valueOf(dataSnapshot.child("fileName").getValue());
                String resourceType = String.valueOf(dataSnapshot.child("resourceType").getValue());
                String publicId = String.valueOf(dataSnapshot.child("publicId").getValue());
                String mimeType = String.valueOf(dataSnapshot.child("mimeType").getValue());
                Post post = new Post(title, description, pinned, postedByNumber, postedByName, null, time, uri, fileName, resourceType, publicId, mimeType);
                postArrayList.add(post);
                postKeys.add(dataSnapshot.getKey());
                postArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                String title = (String) dataSnapshot.child("title").getValue();
                String description = (String) dataSnapshot.child("description").getValue();
                boolean pinned = (boolean) dataSnapshot.child("pinned").getValue();
                String postedByNumber = (String) dataSnapshot.child("postedByNumber").getValue();
                String postedByName = (String) dataSnapshot.child("postedByName").getValue();
                Long time = (Long) dataSnapshot.child("time").getValue();
                String uri = String.valueOf(dataSnapshot.child("url").getValue());
                String fileName = String.valueOf(dataSnapshot.child("fileName").getValue());
                String resourceType = String.valueOf(dataSnapshot.child("resourceType").getValue());
                String publicId = String.valueOf(dataSnapshot.child("publicId").getValue());
                String mimeType = String.valueOf(dataSnapshot.child("mimeType").getValue());
                Post post = new Post(title, description, pinned, postedByNumber, postedByName, null, time, uri, fileName, resourceType, publicId, mimeType);
                String key = dataSnapshot.getKey();
                int index = postKeys.indexOf(key);
                postArrayList.set(index, post);
                postArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = postKeys.indexOf(key);
                postArrayList.remove(index);
                postKeys.remove(index);
                postArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("onCreateOptionsMenu", menu.toString());
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Item selected", item.toString());
        switch (item.getItemId()) {
            case R.id.menu_post_requests:
                Intent postReqIntent = new Intent(this, PostRequests.class);
                postReqIntent.putExtra("class", classCode);
                startActivity(postReqIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d("onPrepareOptions", menu.toString());
        MenuItem item = menu.findItem(R.id.menu_post_requests);
        if (adminState.isAdmin()) {
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private boolean isNetworkConnected() {
        //Check connected to a network and internet available
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = null;
        if (cm != null) {
            info = cm.getActiveNetworkInfo();
        }
        return info != null && info.isConnected();
    }

    public FileSelectedFragment getSendFragment() {
        return sendFragment;
    }

    public FileSelectedFragment setSendFragment(FileSelectedFragment fragment) {
        sendFragment = fragment;
        return sendFragment;
    }

    /**
     * Read the content and file name from the received uri.
     *
     * @param intent Intent received from other app.
     * @return The generated file after consuming the uri.
     * @throws IOException If we are unable to read content from uri or read the content to cache file.
     */
    private File handleIntent(Intent intent) throws IOException {
        //Get the uri of the content
        Uri uri = intent.getData();
        //If received uri is null, return null
        if (uri == null)
            return null;
        //Get the file name
        String fileName = queryName(getContentResolver(), uri);
        //If the filename is null, return null
        if (fileName == null)
            return null;
        //Create the file in cache directory
        File file = new File(getCacheDir(), fileName);
        //Consume content from uri
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            //Write content to created file
            try (OutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                //Consume content from uri and write it to the file
                while ((read = inputStream != null ? inputStream.read(buffer) : 0) != -1) {
                    output.write(buffer, 0, read);
                }
                //Close the output stream
                output.flush();
                output.close();
            }
            //Close the input stream
            inputStream.close();
        }
        //Return the file
        return file;
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        if (returnCursor == null)
            return null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        Log.d("Cursor details", Arrays.toString(returnCursor.getColumnNames()));
/*        for (int i = 0; i < returnCursor.getColumnCount())
            Log.d("Cursor val", returnCursor.get)*/
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private Cursor queryDoc(ContentResolver resolver, Uri uri) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor == null)
            return null;
        cursor.moveToFirst();
        return cursor;
    }
}