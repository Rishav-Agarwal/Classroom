package in.edu.jaduniv.classroom.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.CurrentUser;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;

/**
 * This activity receives intent from other apps willing to share data which is then forwarded to #EventAndNotice activity.
 */
public class SendFileFromOtherApp extends AppCompatActivity {

    //Firebase's database reference to store current user's classes
    private DatabaseReference userClassRef;

    //Store user's class codes and names
    private ArrayList<String> userClassCodes, userClassNames;

    //RecyclerView which shows the list of classes and codes
    private RecyclerView rvClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file_from_other_app);

        //Check if user is signed in.
        if (!isValidUser())
            return;

        //Get the received intent.
        Intent intent = getIntent();
        //Get the action : ACTION_SEND.
        String action = intent.getAction();
        //Get mime type of the data received.
        String type = intent.getType();
        //Get the uri of the data received.
        Uri data = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        /*
         * Get the file from the received uri using content resolver.
         * If we are unable to get the file, just return.
         */
        File file;
        try {
            file = handleIntent(intent);
        } catch (IOException e) {
            Log.e("File error", e.getMessage());
            e.printStackTrace();
            finish();
            return;
        }
        if (file == null) {
            finish();
            return;
        }
        //Query for the file name using the received uri
        String fileName = queryName(getContentResolver(), data);

        Log.d("Intent", action + " :: " + type + " :: " + data);
        Log.d("Name", fileName);

        //If everything goes fine and we receive all the data properly, send the data to #EventAndNotice activity and start it
        startActivity(new Intent(SendFileFromOtherApp.this, EventAndNotice.class)
                .putExtra("class", "juit1620")
                .putExtra("uri", String.valueOf(data))
                .putExtra("fileName", String.valueOf(fileName))
                .putExtra("file", file)
                .setType(intent.getType()));
        finish();
        //return;

/*        loadClasses();
        initializeVariables();*/
    }

    /**
     * Initialize the views and adapter.
     */
    private void initializeVariables() {
        rvClasses = findViewById(R.id.rv_send_to_classes);
        rvClasses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvClasses.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sendto_class, parent, false)) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                View view = holder.itemView;
                int pos = holder.getAdapterPosition();
                TextView tvClassName = view.findViewById(R.id.tv_sendto_class_name);
                TextView tvClassCode = view.findViewById(R.id.tv_sendto_class_code);
                tvClassName.setText(userClassNames.get(pos));
                tvClassCode.setText(userClassCodes.get(pos));
            }

            @Override
            public int getItemCount() {
                if (userClassCodes != null)
                    return userClassCodes.size();
                return 0;
            }
        });
    }

    /**
     * Load the user's classes
     */
    private void loadClasses() {
        userClassRef = FirebaseUtils.getDatabaseReference()
                .child("users/" + CurrentUser.getInstance().getPhone() + "/classes");
        //Read all the classes once and receive as ArrayList
        userClassRef.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * This method is triggered once we read all the data successfully.
             *
             * @param dataSnapshot Containing the data.
             */
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Classes", String.valueOf(dataSnapshot.getValue()));
                //If data received is valid, retrieve it into ArrayList
                if (dataSnapshot.exists() && dataSnapshot.hasChildren())
                    userClassCodes = (ArrayList<String>) dataSnapshot.getValue();
                //If user is not present in any class, simply return.
                if (userClassCodes == null || userClassCodes.size() == 0)
                    return;
                //Create user class' name array and initialize them to "" [Nothing] (We will update it later)
                userClassNames = new ArrayList<>(userClassCodes.size());
                for (int i = 0; i < userClassCodes.size(); ++i)
                    userClassNames.add("");

                //Iterate over the user's class codes and query for the class names
                for (int i = 0; i < userClassCodes.size(); ++i) {
                    String code = userClassCodes.get(i);
                    int finalI = i;
                    //Get the class name once
                    FirebaseUtils.getDatabaseReference()
                            .child("classes/" + code + "/name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                /**
                                 * This method is triggered once we read all the data successfully.
                                 *
                                 * @param dataSnapshot Containing the data.
                                 */
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d("__Class name[" + finalI + "]", dataSnapshot.getValue(String.class));
                                    //Get the class name, update our RecyclerView and notify its adapter.
                                    userClassNames.set(finalI, dataSnapshot.getValue(String.class));
                                    rvClasses.getAdapter().notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * This method checks if user is signed in or not.
     *
     * @return A boolean denoting sign-in status (#True for signed-in otherwise #False).
     */
    private boolean isValidUser() {
        if (!CurrentUser.getInstance().isSignedIn()) {
            Toast.makeText(getApplicationContext(), "You are not signed in!", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
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
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
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

    /**
     * Query the content resolver for the file name of the received uri
     *
     * @param resolver The content resolver
     * @param uri      The received uri
     * @return The filename
     */
    private String queryName(ContentResolver resolver, Uri uri) {
        //Query with the uri.
        Cursor returnCursor = resolver.query(uri, null, null, null, null);
        //If query fails, return null.
        if (returnCursor == null)
            return null;

        //If query was successful, get the file name and return it.
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }
}