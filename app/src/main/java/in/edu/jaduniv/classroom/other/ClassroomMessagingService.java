package in.edu.jaduniv.classroom.other;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.activity.ClassInfo;
import in.edu.jaduniv.classroom.activity.EventAndNotice;
import in.edu.jaduniv.classroom.activity.PostRequests;

/**
 * This extends Firebase's `FirebaseMessagingService` class and process incoming data messages and send notification to the users.
 */
public class ClassroomMessagingService extends FirebaseMessagingService {

    /**
     * This is called when a new data message is sent to our application.
     * It processes the message according to its type and sends notifications accordingly.
     *
     * @param remoteMessage - Data message received (Sent from Firebase cloud function)
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Get data message into `Map`
        Map<String, String> map = remoteMessage.getData();

        //Logging the received data
        Log.d("Data received", map.toString());

        /*
         * Get type of message received. Custom message type is used as follows-
         *
         * "post": For a new post created.
         * "post_req": For a new post requested.
         * "join_req": For a new join requested.
         *
         * In all cases, we send notification with the content intent(defines which activity to open when notification is clicked) attached to it.
         */
        String type = map.get("type");

        switch (type) {
            //NEW POST
            case "post":
                //Get the class name in which post was created
                String postClassName = map.get("post_class_name");

                //If app is not in foreground, send notification, otherwise just show a Toast message for new content
                if (isAppIsInBackground(getApplicationContext())) {
                    //Get post details
                    String postId = map.get("post_id");
                    String postClassCode = map.get("post_class_code");
                    String postTitle = map.get("post_title");
                    String postDescription = map.get("post_description");
                    String postPostedByName = map.get("post_postedByName");
                    String postFileName = map.get("post_file_name");

                    //If their was no title set for the post, set it to nothing("")
                    if (postTitle == null || postTitle.equals("undefined"))
                        postTitle = "";
                    //If their was no description(in case of attached file) for the post, set it to nothing("")
                    if (postDescription == null || postDescription.equals("undefined"))
                        postDescription = "";
                    //If a file is attached, append it to description
                    if (postFileName != null && !postFileName.startsWith("null") && !postFileName.startsWith("undefined"))
                        postDescription = "📎  " + postFileName + "\n" + postDescription;

                    //Create intent for notification click. Posts activity should open.
                    Intent postIntent = new Intent(getApplicationContext(), EventAndNotice.class);
                    //Send the class code as with the intent so that correct class' posts open.
                    postIntent.putExtra("class", postClassCode);

                    //When posts activity is opened, user should be able to navigate back to main activity and others, so create a back-stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(postIntent);
                    //Get the PendingIntent for notification
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(1000, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Build the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), /* Notification Channel */postClassName)
                            //Notification group
                            .setGroup(postClassCode)
                            .setSmallIcon(R.drawable.classroom_notification)
                            //When notification is clicked, `pendingIntent` will be fired
                            .setContentIntent(pendingIntent)
                            //Dismiss notification when clicked
                            .setAutoCancel(true)
                            //Set visibility to private since it contains sensitive information
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                            //Set max priority
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                            //Notification's contents(text)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(postDescription))
                            .setContentText(postDescription)
                            .setSubText(postClassName);
                    /*
                     * Set notification's title.
                     * If post has a title, then set notification's title as [PostSenderName ~ PostTitle]
                     * Otherwise, [PostSenderName]
                     */
                    if (!postTitle.equals("")) {
                        builder.setContentTitle(postPostedByName + " ~ " + postTitle);
                    } else {
                        builder.setContentTitle(postPostedByName);
                    }

                    //Get the notification manager
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    //Create notification channel for android oreo and above
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(postClassCode, postClassName, NotificationManager.IMPORTANCE_HIGH);
                        channel.enableLights(true);
                        channel.setLightColor(Color.RED);
                        channel.enableVibration(true);
                        builder.setChannelId(postClassCode);
                        if (nm != null) {
                            nm.createNotificationChannel(channel);
                        }
                    }

                    //Send the notification
                    if (nm != null) {
                        nm.notify((int) Long.parseLong(postId), builder.build());
                    }
                } else {
                    //App is in foreground, so just show a Toast message
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast newPostToast = Toast.makeText(getApplicationContext(), "New post in " + postClassName, Toast.LENGTH_LONG);
                            newPostToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            newPostToast.show();
                        }
                    });
                }
                break;

            //NEW POST REQUEST
            case "post_req":
                //Get the class name in which post request was sent
                postClassName = map.get("post_class_name");

                //If app is not in foreground, send notification, otherwise just show a Toast message for new content
                if (isAppIsInBackground(getApplicationContext())) {
                    //Get post details
                    String postId = map.get("post_id");
                    String postClassCode = map.get("post_class_code");
                    String postTitle = map.get("post_title");
                    String postDescription = map.get("post_description");
                    String postPostedByName = map.get("post_postedByName");
                    String postFileName = map.get("post_file_name");

                    //If their was no title set for the post, set it to nothing("")
                    if (postTitle == null || postTitle.equals("undefined"))
                        postTitle = "";
                    //If their was no description(in case of attached file) for the post, set it to nothing("")
                    if (postDescription == null || postDescription.equals("undefined"))
                        postDescription = "";
                    //If a file is attached, append it to description
                    if (postFileName != null && !postFileName.startsWith("null") && !postFileName.startsWith("undefined"))
                        postDescription = "📎  " + postFileName + "\n" + postDescription;
                    //Prepend `(New post request)` to description to specify that it is a request
                    postDescription = "(New post request)\n" + postDescription;

                    //Create intent for notification click. PostRequests activity should open.
                    Intent postIntent = new Intent(getApplicationContext(), PostRequests.class);
                    //Send the class code as with the intent so that correct class' post requests opens.
                    postIntent.putExtra("class", postClassCode);

                    //When PostRequests activity is opened, user should be able to navigate back to main activity and others, so create a back-stack.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(postIntent);
                    //Since, its parent activity is Posts activity, it need the classCode. So add the class code to posts' activity.
                    int index = stackBuilder.getIntentCount() - 2;
                    Intent posIntent;
                    if (index >= 0) {
                        posIntent = stackBuilder.editIntentAt(index);
                        if (posIntent != null)
                            posIntent.putExtra("class", postClassCode);
                    }

                    //Get the PendingIntent for notification
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(1000, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Build the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), postClassName)
                            .setGroup(postClassCode)
                            .setSmallIcon(R.drawable.classroom_notification)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(postDescription))
                            .setContentText(postDescription).setSubText(postClassName);
                    if (!postTitle.equals("")) {
                        builder.setContentTitle(postPostedByName + " ~ " + postTitle);
                    } else {
                        builder.setContentTitle(postPostedByName);
                    }

                    //Get the notification manager
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    //Create notification channel for android oreo and above
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(postClassCode, postClassName, NotificationManager.IMPORTANCE_HIGH);
                        channel.enableLights(true);
                        channel.setLightColor(Color.RED);
                        channel.enableVibration(true);
                        builder.setChannelId(postClassCode);
                        if (nm != null) {
                            nm.createNotificationChannel(channel);
                        }
                    }

                    //Send the notification
                    if (nm != null) {
                        nm.notify((int) Long.parseLong(postId), builder.build());
                    }
                } else {
                    //App is in foreground, so just show a Toast message
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast newPostToast = Toast.makeText(getApplicationContext(), "New post request in " + postClassName, Toast.LENGTH_LONG);
                            newPostToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            newPostToast.show();
                        }
                    });
                }
                break;

            //NEW JOIN REQUEST
            case "join_req":
                //Get the class name in which post request was sent
                String joinClassName = map.get("class_name");

                //If app is not in foreground, send notification, otherwise just show a Toast message for new content
                if (isAppIsInBackground(getApplicationContext())) {
                    Log.d("JOIN REQ", "App in background");
                    //Get post details
                    String joinClassCode = map.get("class_code");
                    String joinName = map.get("name");
                    //String joinPhone = map.get("phone");      //Currently not required and not sent by our cloud function too

                    //Create intent for notification click. ClassInfo activity should open.
                    Intent postIntent = new Intent(getApplicationContext(), ClassInfo.class);
                    //Send the class code as with the intent so that correct class' info opens.
                    postIntent.putExtra("class", joinClassCode);

                    //When PostRequests activity is opened, user should be able to navigate back to main activity and others, so create a back-stack.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(postIntent);

                    //Get the PendingIntent for notification
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(1000, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Build the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), joinClassCode)
                            .setGroup(joinClassName)
                            .setSmallIcon(R.drawable.classroom_notification)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(joinName + " has requested to join " + joinClassName))
                            .setContentText(joinName + " has requested to join " + joinClassName).setSubText(joinClassName)
                            .setContentTitle(joinName);

                    //Get the notification manager
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    //Create notification channel for android oreo and above
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(joinClassCode, joinClassName, NotificationManager.IMPORTANCE_HIGH);
                        channel.enableLights(true);
                        channel.setLightColor(Color.RED);
                        channel.enableVibration(true);
                        builder.setChannelId(joinClassCode);
                        if (nm != null) {
                            nm.createNotificationChannel(channel);
                        }
                    }

                    //Send the notification
                    Log.d("Notification Manager?", String.valueOf(nm));
                    if (nm != null) {
                        Log.d("Notification builder", builder.toString());
                        Log.d("Notification build", builder.build().toString());
                        nm.notify((int) System.currentTimeMillis(), builder.build());
                    }
                } else {
                    //App is in foreground, so just show a Toast message
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast newPostToast = Toast.makeText(getApplicationContext(), "New join request in " + joinClassName, Toast.LENGTH_LONG);
                            newPostToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            newPostToast.show();
                        }
                    });
                }
                break;
        }
    }

    /**
     * Checks if our application is in foreground.
     *
     * @param context - From where it is called.
     * @return A boolean representing if our application is running or not.
     */
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        Log.d("Background", String.valueOf(isInBackground));
        return isInBackground;
    }
}