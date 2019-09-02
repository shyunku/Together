package shyunku.project.together.Constants;

import android.content.Context;
import android.provider.Settings;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

import shyunku.project.together.Activities.MainActivity;
import shyunku.project.together.Engines.LogEngine;


public class Global {
    public static final boolean DEBUG_MODE = true;
    public static String version = "v0.9.4-2.15j";
    public static String curDeviceID = "";
    public static String oppFCMkey = "";

    //functions
    public static String getOppKey(){
        return oppFCMkey;
    }

    private static int getCurrentUserIndex(){
        if(DEBUG_MODE)
            return curDeviceID.equals(DEBUG_DEVICE_ID[1])?1:0;
        return curDeviceID.equals(DEVICE_ID[1])?1:0;
    }


    public static String getOwner(){
        return userList[getCurrentUserIndex()];
    }

    public static String getOpper(){
        return userList[1-getCurrentUserIndex()];
    }

    public static void introduceMyself(){
        new LogEngine().sendLog("Device owner : "+userList[getCurrentUserIndex()]);
    }

    //never changes
    public static final String[] userList = {
            "조재훈", "조영훈"
    };
    public static final String rootName = "party-01482";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 a h:mm", Locale.KOREA);
    public static final SimpleDateFormat transactionDateFormat = new SimpleDateFormat("YYMMddhhmm");
    public static final SimpleDateFormat transactionReleaseDateFormat = new SimpleDateFormat("YY.MM.dd a h:mm", Locale.KOREA);
    public static final String FMC_SERVER_KEY = "AAAARb8XDHU:APA91bFj6ysDKxywfmeQDRL4kMPAZj2jgWAGlKtjL7cpXkRhpiyjaWPo2ENO_8sdK8KajOFCoYFh7quvmu2q6KF9BqN4Irf_j1ihEPts51cGOzFVf0kJfkf0FtVOjPcQ6XYjIbLz9PQS";
    public static final String[] DEBUG_DEVICE_ID={
            "a045f52f82e2166e",
            "2960fb27b944c8cc"
    };
    public static final String[] DEVICE_ID={
            "-",
            "844e11dd79045736"
    };

    //재훈 a045f52f82e2166e 디바이스
    //영훈 2960fb27b944c8cc 디바이스

    public static void makeToast(Context context, String alert){
        Toast.makeText(context.getApplicationContext(), alert, Toast.LENGTH_SHORT).show();
    }

    public static void setCurrentDeviceID(String id){
        curDeviceID = id;
    }

    public static void setOppFCMkey(String token){
        oppFCMkey = token;
    }

    public static final int NOTIFICATION_CHAT_ID = 4015;
    public static final int NOTIFICATION_REQUEST_ID = 4016;
    public static final int NOTIFICATON_RESPONSE_ID = 4017;
    public static final String NOTIFICATION_CHAT_CHANNEL_ID = "TOGETHER_TALK";
}