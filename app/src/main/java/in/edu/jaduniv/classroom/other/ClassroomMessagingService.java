package in.edu.jaduniv.classroom.other;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.activity.EventAndNotice;

public class ClassroomMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> map = remoteMessage.getData();

        Log.d("Data received", map.toString());

        String type = map.get("type");

        switch (type) {
            case "post":
                if (isAppIsInBackground(getApplicationContext())) {
                    String postId = map.get("post_id");
                    String postClassName = map.get("post_class_name");
                    String postClassCode = map.get("post_class_code");
                    String postTitle = map.get("post_title");
                    String postDescription = map.get("post_description");
                    String postPostedByName = map.get("post_postedByName");

                    if (postTitle == null || postTitle.equals("undefined"))
                        postTitle = "";
                    if (postDescription == null || postDescription.equals("undefined"))
                        postDescription = "";

                    Intent postIntent = new Intent(getApplicationContext(), EventAndNotice.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(postIntent);
                    postIntent.putExtra("class", postClassCode);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(1000, PendingIntent.FLAG_UPDATE_CURRENT);
                    //PendingIntent postPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1000, postIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), postClassName)
                            .setSmallIcon(R.drawable.classroom_notification)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                            .setContentText(postDescription).setSubText(postClassName);

                    if (!postTitle.equals("")) {
                        builder.setContentTitle(postPostedByName + " ~ " + postTitle);
                    } else {
                        builder.setContentTitle(postPostedByName);
                    }

                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (nm != null) {
                        nm.notify((int) Long.parseLong(postId), builder.build());
                    }
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "New post", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                break;
        }
    }

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

        return isInBackground;
    }
}