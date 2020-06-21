package shyunku.project.together.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import shyunku.project.together.Activities.LocationActivity;

public class LocationUpdateService extends Service {
    public static final int NOTIFICATION_ID = 1500;

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("LOG", "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("LOG", "onCreate()");
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder tempBuilder = LocationActivity.man.getNotification("현재 내 위치 실시간으로 업데이트 중", "설정하시려면 클릭하십시오.");
            if(tempBuilder!=null) {
                startForeground(NOTIFICATION_ID, tempBuilder.build());
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LOG", "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("LOG", "onDestroy()");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("LOG", "onUnbind()");
        return super.onUnbind(intent);
    }
}
