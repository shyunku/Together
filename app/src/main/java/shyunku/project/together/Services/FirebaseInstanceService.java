package shyunku.project.together.Services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import shyunku.project.together.Activities.MainActivity;
import shyunku.project.together.Activities.TogetherTalkActivity;
import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.BackgroundLocationFetcher;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Engines.Lgm;
import shyunku.project.together.Engines.LocationFetcherListener;
import shyunku.project.together.R;

public class FirebaseInstanceService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Background Process
        FirebaseManageEngine.noticeWhoIam(this);
        FirebaseManageEngine.getOppKeyFromFirebaseServer();

        String MessageTag = remoteMessage.getData().get("tag");
        Lgm.g(MessageTag + ") Message Received, content = "+remoteMessage.getData().get("body"));
        if(MessageTag.equals("chat")) {
            if (isForeground()) return;
            sendChatNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }else if(MessageTag.equals("request")){
            sendRequestNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }else if(MessageTag.equals("response")){
            sendResponseNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }else if(MessageTag.equals("location")){
            //위치 정보
            updateLocationInProcess();
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    private void sendChatNotification(String title, String message){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Global.NOTIFICATION_CHAT_CHANNEL_ID);

        NotificationChannel notificationChannel = new NotificationChannel(Global.NOTIFICATION_CHAT_CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent resultIntent = new Intent(this, TogetherTalkActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.main_icon_real)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        notificationManager.notify(Global.NOTIFICATION_CHAT_ID, notificationBuilder.build());
    }

    private void sendRequestNotification(String title, String message){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Global.NOTIFICATION_CHAT_CHANNEL_ID);

        NotificationChannel notificationChannel = new NotificationChannel(Global.NOTIFICATION_CHAT_CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent resultIntent = new Intent(this, TogetherTalkActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent actionIntent_yes = new Intent(this, ActionService.class);
        Intent actionIntent_no = new Intent(this, ActionService.class);
        actionIntent_yes.setAction(ActionService.RESPONSE_YES_ACTION_FLAG);
        actionIntent_no.setAction(ActionService.RESPONSE_NO_ACTION_FLAG);
        PendingIntent piAction1 = PendingIntent.getService(this, Global.NOTIFICATION_REQUEST_ID, actionIntent_yes, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piAction2 = PendingIntent.getService(this, Global.NOTIFICATION_REQUEST_ID, actionIntent_no, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder
                .setSmallIcon(R.mipmap.main_icon_real)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_send_yes, "승낙", piAction1)
                .addAction(R.drawable.ic_send_no, "거부", piAction2)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        notificationManager.notify(Global.NOTIFICATION_REQUEST_ID, notificationBuilder.build());
    }

    private void sendResponseNotification(String title, String message){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Global.NOTIFICATION_CHAT_CHANNEL_ID);

        NotificationChannel notificationChannel = new NotificationChannel(Global.NOTIFICATION_CHAT_CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        //PendingIntent pendingIntent = new PendingIntent(sas);

        notificationBuilder
                .setSmallIcon(R.mipmap.main_icon_real)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        notificationManager.notify(Global.NOTIFICATON_RESPONSE_ID, notificationBuilder.build());
    }

    private void updateLocationInProcess(){
        Lgm.g("background? "+(isForeground()?"false":"true"));
        //상대가 권한을 얻었다고 가정
        if(isForeground())return;

        // Background
        final DatabaseReference locref = FirebaseManageEngine.getPartyUsersRef().child(Global.curDeviceID);
        DatabaseReference refs = locref.child("location_share");
        refs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isShareAllowed = dataSnapshot.getValue(Boolean.class);
                Lgm.g(isShareAllowed+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        BackgroundLocationFetcher.getInstance().onResultFetched(new LocationFetcherListener<LatLng>() {
            @Override
            public void locationFetchListen(LatLng latLng) {
                double lng = latLng.longitude;
                double lat = latLng.latitude;
                Lgm.g("Background Updated: long : "+lng+", lat : "+lat);

                locref.child("longitude").setValue(lng);
                locref.child("latitude").setValue(lat);
            }
        });
    }

    public boolean isForeground(){
        ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(info);
        return (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND || info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE);
    }
}
