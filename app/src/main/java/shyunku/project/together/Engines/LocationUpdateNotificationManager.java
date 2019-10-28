package shyunku.project.together.Engines;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import shyunku.project.together.Activities.LocationActivity;
import shyunku.project.together.Activities.MainActivity;
import shyunku.project.together.Constants.Global;
import shyunku.project.together.R;

public class LocationUpdateNotificationManager extends ContextWrapper {
    private NotificationManager manager;

    public static final String CHANNEL_ID = "A";
    public static final String CHANNEL_NAME = "B";

    public LocationUpdateNotificationManager(Context context){
        super(context);

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(channel);
        }
    }

    public NotificationCompat.Builder getNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Global.NOTIFICATION_CHAT_CHANNEL_ID);

        NotificationChannel notificationChannel = new NotificationChannel(Global.NOTIFICATION_CHAT_CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent resultIntent = new Intent(this, LocationActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setSmallIcon(getSmallIcon());
        builder.setAutoCancel(true);

        return builder;
    }

    private int getSmallIcon() {
        return R.mipmap.main_icon;
    }

    private NotificationManager getManager(){
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}
