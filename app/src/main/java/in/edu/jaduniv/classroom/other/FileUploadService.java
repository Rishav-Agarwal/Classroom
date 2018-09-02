package in.edu.jaduniv.classroom.other;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.google.firebase.database.ServerValue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.activity.EventAndNotice;
import in.edu.jaduniv.classroom.object.Post;
import in.edu.jaduniv.classroom.utility.CloudinaryUtils;

public class FileUploadService extends IntentService {

    OnUploadCompleteListener completeListener;

    public FileUploadService() {
        super("FileUploadService");
    }

    public FileUploadService(String name) {
        super("FileUploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        completeListener = EventAndNotice.listener;

        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }

        if (action != null) {
            switch (action) {
                case CloudinaryUtils.ACTION_FILE_UPLOAD:
                    String title = intent.getStringExtra("title");
                    String content = intent.getStringExtra("content");
                    boolean pinned = intent.getBooleanExtra("pinned", false);
                    String phone = intent.getStringExtra("phone");
                    String name = intent.getStringExtra("name");
                    final long longTime = intent.getLongExtra("longTime", 0);
                    String fileUri = intent.getStringExtra("uri");
                    final String fileName = intent.getStringExtra("fileName");
                    final String classCode = intent.getStringExtra("classCode");

                    Log.d("fileUri", fileUri + "");

                    if (fileUri != null && !fileUri.trim().equals("")) {
                        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Uploading");
                        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Cloudinary cloudinary = CloudinaryUtils.getInstance();
                        try {
                            Map<String, String> config = new HashMap<>(CloudinaryUtils.getCloudinaryConfig());
                            config.put("folder", "classroom/juit1620/posts/");
                            //config.put("public_id", new File(Uri.parse(fileUri).getPath()).getName());
                            Map upload = null;
                            try {
                                upload = cloudinary.uploader().uploadLarge(new File(Uri.parse(fileUri).getPath()), config, new ProgressCallback() {
                                    @Override
                                    public void onProgress(long bytesUploaded, long totalBytes) {
                                        if (!isNetworkConnected()) {
                                            Toast.makeText(getApplicationContext(), "Download cancelled", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        //Log.d("Progress", bytesUploaded + " of " + totalBytes);
                                        Intent postIntent = new Intent(getApplicationContext(), EventAndNotice.class);
                                        postIntent.putExtra("class", classCode);
                                        PendingIntent postPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1000, postIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        builder.setSmallIcon(R.drawable.classroom_notification)
                                                .setContentIntent(postPendingIntent)
                                                .setAutoCancel(true)
                                                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                                                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                                                .setContentTitle(fileName)
                                                .setSubText(classCode)
                                                .setProgress(100, (int) (bytesUploaded / (float) totalBytes * 100), false)
                                                .setOngoing(true)
                                                .setContentText("Uploading");
                                        if (nm != null) nm.notify((int) longTime, builder.build());
                                    }
                                });
                            } catch (IOException | IllegalStateException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Upload cancelled", Toast.LENGTH_SHORT).show();
                                builder.setOngoing(false).setProgress(0, 0, false).setContentText("Upload cancelled");
                                if (nm != null) nm.notify((int) longTime, builder.build());
                                return;
                            }
                            builder.setOngoing(false).setProgress(0, 0, false).setContentText("Upload complete");
                            if (nm != null) nm.notify((int) longTime, builder.build());
                            Log.d("Data", upload + "");
                            fileUri = (String) upload.get("secure_url");
                            String resourceType = (String) upload.get("resource_type");
                            String publicId = (String) upload.get("public_id");

                            Post post = new Post(title, content, pinned, phone, name, ServerValue.TIMESTAMP, longTime, fileUri, fileName, resourceType, publicId);
                            completeListener.onUploadCompleted(post);
                        } catch (RuntimeException e) {
                            Toast.makeText(getApplicationContext(), "Cannot upload this file", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (content != null && !content.trim().equals("")) {
                            if (name == null || name.equals("") || name.startsWith("undefined") || name.startsWith("null"))
                                name = "Admin";
                            Log.d("NAME :: PHONE", name + " :: " + phone);
                            Post post = new Post(title, content, pinned, phone, name, ServerValue.TIMESTAMP, longTime, null, fileName, null, null);
                            completeListener.onUploadCompleted(post);
                        }
                    }
            }
        }
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

    public interface OnUploadCompleteListener {
        void onUploadCompleted(Post post);
    }
}