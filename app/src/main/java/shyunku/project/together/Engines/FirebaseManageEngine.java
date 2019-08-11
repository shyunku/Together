package shyunku.project.together.Engines;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import shyunku.project.together.Constants.Global;

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

    public static void sendNotificationMessage(final String msgContent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // FMC 메시지 생성 start
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", msgContent);
                    notification.put("title", Global.getOwner());
                    root.put("data", notification);
                    root.put("to", Global.getOppKey());
                    // FMC 메시지 생성 end

                    URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.addRequestProperty("Authorization", "key=" + Global.FMC_SERVER_KEY);
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
}
