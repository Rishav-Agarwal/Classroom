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

public class SendFileFromOtherApp extends AppCompatActivity {

    private DatabaseReference userClassRef;

    private ArrayList<String> userClassCodes, userClassNames;

    private RecyclerView rvClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file_from_other_app);

        if (!isValidUser())
            return;

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri data = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        Log.d("Intent", action + " :: " + type + " :: " + data);

        File file;
        try {
            file = handleIntent(intent);
            Log.d("File opened", String.valueOf(file));
            if (file != null)
                startActivity(new Intent(SendFileFromOtherApp.this, EventAndNotice.class)
                        .putExtra("class", "juit1620")
                        .putExtra("uri", String.valueOf(Uri.fromFile(file)))
                );
            finish();
            return;
        } catch (IOException e) {
            Log.e("SendFileFromOtherApp", "IOException - " + e.getMessage());
            e.printStackTrace();
        }

        loadClasses();
        initializeVariables();
    }

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

    private void loadClasses() {
        userClassRef = FirebaseUtils.getDatabaseReference()
                .child("users/" + CurrentUser.getInstance().getPhone() + "/classes");
        userClassRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Classes", String.valueOf(dataSnapshot.getValue()));
                if (dataSnapshot.exists() && dataSnapshot.hasChildren())
                    userClassCodes = (ArrayList<String>) dataSnapshot.getValue();
                if (userClassCodes == null || userClassCodes.size() == 0)
                    return;
                userClassNames = new ArrayList<>(userClassCodes.size());
                for (int i = 0; i < userClassCodes.size(); ++i)
                    userClassNames.add("");

                for (int i = 0; i < userClassCodes.size(); ++i) {
                    String code = userClassCodes.get(i);
                    int finalI = i;
                    FirebaseUtils.getDatabaseReference()
                            .child("classes/" + code + "/name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d("Class name[" + finalI + "]", dataSnapshot.getValue(String.class));
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

    private boolean isValidUser() {
        if (!CurrentUser.getInstance().isSignedIn()) {
            Toast.makeText(getApplicationContext(), "You are not signed in!", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
    }

    private File handleIntent(Intent intent) throws IOException {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri != null) {
            String fileName = queryName(getContentResolver(), uri);
            if (fileName == null)
                return null;
            File file = new File(getCacheDir(), fileName);
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                try (OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4 * 1024];
                    int read;

                    while ((read = inputStream != null ? inputStream.read(buffer) : 0) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                    output.close();
                }
                inputStream.close();
            }
            return file;
        }
        return null;
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        if (returnCursor == null)
            return null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }
}