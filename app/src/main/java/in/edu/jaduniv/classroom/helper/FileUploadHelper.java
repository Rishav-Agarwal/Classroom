package in.edu.jaduniv.classroom.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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
import in.edu.jaduniv.classroom.interfaces.OnCompleteListener;
import in.edu.jaduniv.classroom.object.Post;
import in.edu.jaduniv.classroom.utility.CloudinaryUtils;

/**
 * This is a {@link Service} which helps to upload file to cloudinary servers asynchronously
 */
public class FileUploadHelper extends Service {

    public FileUploadHelper() {
    }

    //Invoked by bindService()
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Invoked by startService(). Check is input {@link Intent} is valid and then process the upload task in a separate thread.
     *
     * @param intent  {@link Intent} carrying extra data for upload task
     * @param flags   Flags for the service
     * @param startId Id for a service. Uniquely identifies each call.
     * @return An integer denoting non-sticky behavior
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Verify that intent exists
        if (intent == null)
            return Service.START_NOT_STICKY;

        //Get the action
        String action = intent.getAction();

        //Verify action exists
        if (action == null)
            return Service.START_NOT_STICKY;

        //If everything is OK, start a new thread for upload task
        new UploadThread(action, intent, startId).start();

        return Service.START_NOT_STICKY;
    }

    private class UploadThread implements Runnable {

        /**
         * The thread on which upload task will be done
         */
        Thread uploadThread;

        /**
         * {@link Intent} Input intent
         */
        Intent intent;

        /**
         * {@link Intent}'s action
         */
        String action;

        /**
         * {@link OnCompleteListener} It's `onUploadCompleted` method is called after successful upload
         */
        OnCompleteListener listener;

        /**
         * The unique id for this upload task
         */
        int startId;

        /**
         * Constructor takes in {@link Intent} intent, action and unique startId
         *
         * @param action  Action to perform
         * @param intent  Input intent
         * @param startId Unique start id for this task
         */
        UploadThread(String action, Intent intent, int startId) {
            this.action = action;
            uploadThread = new Thread(this);
            this.intent = intent;
            this.startId = startId;
        }

        /**
         * Start the tread [upload task]
         */
        public void start() {
            uploadThread.start();
        }

        /**
         * This method is called in a separate after start is called
         */
        @Override
        public void run() {
            switch (action) {
                //FILE UPLOAD ACTION
                case CloudinaryUtils.ACTION_POST_UPLOAD:
                    //Get the listener
                    listener = EventAndNotice.listener;

                    //Get extra data for upload
                    Bundle data = intent.getBundleExtra("uploadData");
                    String title = data.getString("title", "");
                    String content = data.getString("content", "");
                    boolean pinned = data.getBoolean("pinned", false);
                    String phone = data.getString("phone", "");
                    String name = data.getString("name", "");
                    final long longTime = data.getLong("longTime", 0);
                    String receivedUriString = data.getString("uri", "");
                    final String fileName = data.getString("fileName", "");
                    File file = (File) data.getSerializable("file");
                    final String classCode = data.getString("classCode", "");
                    String mimeType = data.getString("mime");

                    Log.d("fileUri", receivedUriString + "");

                    //Build notification
                    Intent postIntent = new Intent(getApplicationContext(), EventAndNotice.class);
                    postIntent.putExtra("class", classCode);
                    PendingIntent postPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1000, postIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "Uploader");
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationBuilder.setSmallIcon(R.drawable.classroom_notification)
                            .setAutoCancel(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                            .setOngoing(true)
                            .setContentText("Uploading")
                            .setContentIntent(postPendingIntent)
                            .setContentTitle(fileName)
                            .setSubText(classCode);
                    //Notification channel for android oreo and above users
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("Uploader", "Upload", NotificationManager.IMPORTANCE_HIGH);
                        channel.enableLights(true);
                        channel.setLightColor(Color.RED);
                        channel.enableVibration(true);
                        notificationBuilder.setChannelId("Uploader");
                        if (notificationManager != null) {
                            notificationManager.createNotificationChannel(channel);
                        }
                    }
                    Notification notification = notificationBuilder.build();

                    //Start this service as foreground service by showing notification
                    startForeground(startId, notification);

                    if (receivedUriString != null && !receivedUriString.trim().equals("")) {
                        //If user has selected a file, start uploading

                        Log.d("Mime Type", String.valueOf(mimeType));
                        Cloudinary cloudinary = CloudinaryUtils.getInstance();
                        try {
                            //Get the configuration for our Cloudinary cloud
                            Map<String, String> config = new HashMap<>(CloudinaryUtils.getCloudinaryConfig());
                            //Put the folder
                            config.put("folder", "classroom/" + classCode + "/posts/");
                            Map upload;
                            try {
                                long lastTime[] = new long[]{0L};
                                //Upload the file
                                upload = cloudinary.uploader().uploadLarge(file, config,
                                        new ProgressCallback() {

                                            /**
                                             * The progress callback of our upload process.
                                             * We update the upload notification after every 0.5 seconds.
                                             *
                                             * @param bytesUploaded Total bytes uploaded
                                             * @param totalBytes Total bytes to be uploaded
                                             */
                                            @Override
                                            public void onProgress(long bytesUploaded, long totalBytes) {
                                                //If network lost, cancel
                                                if (!isNetworkConnected()) {
                                                    Toast.makeText(getApplicationContext(), "Download cancelled", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                //If difference between last update time and current time is less than 500 ms, return
                                                long currTime = System.currentTimeMillis();
                                                if (currTime - lastTime[0] < 500)
                                                    return;

                                                //Oterwise, update the notification and last update time
                                                lastTime[0] = currTime;
                                                notificationBuilder.setProgress(100, (int) (bytesUploaded / (float) totalBytes * 100), false);
                                                if (notificationManager != null)
                                                    notificationManager.notify(startId, notificationBuilder.build());
                                            }
                                        });
                            } catch (IOException | IllegalStateException e) {
                                //Handle thrown exception. Most probably because of network failure
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Upload cancelled", Toast.LENGTH_SHORT).show();
                                notificationBuilder.setOngoing(false).setProgress(0, 0, false).setContentText("Upload cancelled");
                                if (notificationManager != null)
                                    notificationManager.notify(startId, notificationBuilder.build());
                                return;
                            }

                            //Upload complete. Set notification to be completed
                            notificationBuilder.setOngoing(false)
                                    .setProgress(0, 0, false)
                                    .setContentText("Upload complete");
                            if (notificationManager != null)
                                notificationManager.notify(startId, notificationBuilder.build());
                            Log.d("Data", upload + "");

                            //Get the new post data and call the complete callback
                            String fileUri = (String) upload.get("secure_url");
                            String resourceType = (String) upload.get("resource_type");
                            String publicId = (String) upload.get("public_id");
                            //Create new post object which is passed to complete listener's callback function
                            Post post = new Post(title, content, pinned, phone, name, ServerValue.TIMESTAMP, longTime, fileUri, fileName, resourceType, publicId, mimeType != null ? mimeType : "*/*");
                            listener.onUploadCompleted(post);
                        } catch (RuntimeException e) {
                            //Handle exception thrown at run-time most probably because the file is not supported by cloudinary
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Cannot upload file", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        //No file was selected, just create new `Post` object and pass to the complete listener
                        if (content != null && !content.trim().equals("")) {
                            if (name == null || name.equals("") || name.startsWith("undefined") || name.startsWith("null"))
                                name = "Admin";
                            Log.d("NAME :: PHONE", name + " :: " + phone);
                            Post post = new Post(title, content, pinned, phone, name, ServerValue.TIMESTAMP, longTime, null, fileName, null, null, "*/*");
                            listener.onUploadCompleted(post);
                        }
                    }

                    //Since current upload is complete, cancel the ongoing notification
                    if (notificationManager != null)
                        notificationManager.cancel(startId);
            }
            //Stop service as foreground
            stopForeground(true);
            //Stop the service
            stopSelf(startId);
        }

        /**
         * Checks is user is connected to network.
         *
         * @return A boolean. `True` is user is connected otherwise `False`.
         */
        private boolean isNetworkConnected() {
            //Check connected to a network and internet available
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = null;
            if (cm != null) {
                info = cm.getActiveNetworkInfo();
            }
            return info != null && info.isConnected();
        }
    }
}