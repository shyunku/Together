package shyunku.project.together.Constants;

import android.content.Context;
import android.provider.Settings;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import shyunku.project.together.Engines.Lgm;


public class Global {
    public static String version = "v1.0.4.513";

    public static String curDeviceID = "";
    public static String curParty = "";


    public static String oppFCMkey = "";

    public static String OwnerName = "";
    public static String OpperName = "";

    //functions
    public static String getOppKey(){
        return oppFCMkey;
    }


    public static String getOwner(){
        return OwnerName;
    }
    public static String getOpper(){
        return OpperName;
    }

    public static void introduceMyself(){
        Lgm.g("Device owner : "+ getOwner());
    }

    public static final SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 a h:mm", Locale.KOREA);
    public static final SimpleDateFormat transactionDateFormat = new SimpleDateFormat("yyMMddHHmmss", Locale.KOREA);
    public static final SimpleDateFormat transactionReleaseDateFormat = new SimpleDateFormat("yy.MM.dd a h:mm:ss", Locale.KOREA);
    public static final String FCM_SERVER_KEY = "AAAARb8XDHU:APA91bFj6ysDKxywfmeQDRL4kMPAZj2jgWAGlKtjL7cpXkRhpiyjaWPo2ENO_8sdK8KajOFCoYFh7quvmu2q6KF9BqN4Irf_j1ihEPts51cGOzFVf0kJfkf0FtVOjPcQ6XYjIbLz9PQS";

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

    public static String sha256(String str){
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            SHA = null;
        }
        return SHA.substring(50);
    }
}