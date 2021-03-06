package shyunku.project.together.Engines;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import shyunku.project.together.Activities.MainActivity;
import shyunku.project.together.Constants.Global;
import shyunku.project.together.Objects.User;
import shyunku.project.together.Objects.UserDump;
import shyunku.project.together.R;

public class FirebaseManageEngine {
    static private FirebaseDatabase LocalDB = FirebaseDatabase.getInstance();
    static private DatabaseReference LocalDBref = FirebaseDatabase.getInstance().getReference();

    public FirebaseManageEngine(){
    }

    public static void RenewDatabaseReference(){
        LocalDBref = FirebaseDatabase.getInstance().getReference();
    }

    public static void RenewDatabase(){
        LocalDB = FirebaseDatabase.getInstance();
    }


    //getter & setter
    public static DatabaseReference getFreshLocalDBref(){
        RenewDatabaseReference();
        return LocalDBref;
    }

    public static FirebaseDatabase getFreshLocalDB(){
        RenewDatabase();
        return LocalDB;
    }

    public static DatabaseReference getPartiesRef(){
        return FirebaseDatabase.getInstance().getReference().child("parties");
    }

    public static DatabaseReference getUserDumpListRef(){
        return FirebaseDatabase.getInstance().getReference().child("userDumplist");
    }

    public static DatabaseReference getPartyRef(){
        return FirebaseDatabase.getInstance().getReference().child("parties").child(Global.curParty);
    }

    public static DatabaseReference getPartyTransactionsRef(){
        return FirebaseDatabase.getInstance().getReference().child("parties").child(Global.curParty).child("transactions");
    }

    public static DatabaseReference getPartyUsersRef(){
        return FirebaseDatabase.getInstance().getReference().child("parties").child(Global.curParty).child("users");
    }

    public static DatabaseReference getPartyChatsRef(){
        return FirebaseDatabase.getInstance().getReference().child("parties").child(Global.curParty).child("chats");
    }

    public static void pushUserDump(UserDump userDump){
        DatabaseReference ref = getUserDumpListRef();
        Map<String, Object> post = userDump.toMap();
        Map<String, Object> child = new HashMap<>();
        child.put(userDump.deviceID, post);

        ref.updateChildren(child);
    }

    public static void registerJoinedPartyCode(String code){
        getUserDumpListRef().child(Global.curDeviceID).child("subParty").setValue(code);
    }

    public static void pushSomething(DatabaseReference parentRef, String key, Map<String, Object> map){
        Map<String, Object> child = new HashMap<>();
        child.put(key, map);
        parentRef.updateChildren(child);
    }

    public static void sendNotificationChatMessage(final String msgContent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // FMC 메시지 생성 start
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", msgContent);
                    notification.put("title", Global.getOwner());
                    notification.put("tag", "chat");
                    root.put("data", notification);
                    root.put("to", Global.getOppKey());

                    // FMC 메시지 생성 end
                    URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.addRequestProperty("Authorization", "key=" + Global.FCM_SERVER_KEY);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-type", "application/json");
                    OutputStream os = conn.getOutputStream();
                    os.write(root.toString().getBytes("utf-8"));
                    os.flush();
                    conn.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendNotificationRequestMessage(final String msgContent) {
        Lgm.g("OPP_FCM_KEY = "+Global.getOppKey());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // FMC 메시지 생성 start
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", msgContent);
                    notification.put("title", Global.getOwner()+"님의 요청");
                    notification.put("tag", "request");
                    root.put("data", notification);
                    root.put("to", Global.getOppKey());

                    // FMC 메시지 생성 end
                    URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.addRequestProperty("Authorization", "key=" + Global.FCM_SERVER_KEY);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-type", "application/json");
                    OutputStream os = conn.getOutputStream();
                    os.write(root.toString().getBytes("utf-8"));
                    os.flush();
                    conn.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendLocationRequestMessage() {
        Lgm.g("OPP_FCM_KEY = "+Global.getOppKey());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // FMC 메시지 생성 start
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", "request_location");
                    notification.put("tag", "location");
                    root.put("data", notification);
                    root.put("to", Global.getOppKey());

                    // FMC 메시지 생성 end

                    URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.addRequestProperty("Authorization", "key=" + Global.FCM_SERVER_KEY);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-type", "application/json");
                    OutputStream os = conn.getOutputStream();
                    os.write(root.toString().getBytes("utf-8"));
                    os.flush();
                    conn.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendNotificationResponseMessage(final boolean response) {
        Lgm.g("OPP_FCM_KEY = "+Global.getOppKey());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // FMC 메시지 생성 start
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", Global.getOwner()+"님이 요청을 "+(response?"승낙":"거부")+"했습니다");
                    notification.put("title", "요청 응답");
                    notification.put("tag", "response");
                    root.put("data", notification);
                    root.put("to", Global.getOppKey());

                    // FMC 메시지 생성 end

                    URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.addRequestProperty("Authorization", "key=" + Global.FCM_SERVER_KEY);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-type", "application/json");
                    OutputStream os = conn.getOutputStream();
                    os.write(root.toString().getBytes("utf-8"));
                    os.flush();
                    conn.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void noticeWhoIam(Context context){
        @SuppressLint("HardwareIds") final String deviceId = Global.sha256(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        Global.setCurrentDeviceID(deviceId);
        Global.introduceMyself();
    }

    public static void getOppKeyFromFirebaseServer(){
        DatabaseReference oppref = FirebaseManageEngine.getPartyUsersRef();
        oppref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User opp = snapshot.getValue(User.class);
                    if(!Global.curDeviceID.equals(opp.deviceID)){
                        Global.setOppFCMkey(opp.FCMtoken);
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
