package shyunku.project.together.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import shyunku.project.together.Activities.MainActivity;
import shyunku.project.together.Activities.TogetherTalkActivity;
import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.LogEngine;
import shyunku.project.together.R;

public class FirebaseInstanceService extends FirebaseMessagingService {
    private final int NOTIFICATION_ID = 999;
    private final String NOTIFICATION_CHANNEL_ID = "TOGETHER_TALK";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    private void sendNotification(String title, String message){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent resultIntent = new Intent(this, TogetherTalkActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TogetherTalkActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
