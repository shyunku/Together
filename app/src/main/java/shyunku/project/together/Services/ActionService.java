package shyunku.project.together.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import shyunku.project.together.Engines.FirebaseManageEngine;

public class ActionService extends IntentService {
    public static final String RESPONSE_YES_ACTION_FLAG = "RYAF";
    public static final String RESPONSE_NO_ACTION_FLAG = "RNAF";

    public ActionService() {
        super("DISPLAY");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        if(action.equals(RESPONSE_YES_ACTION_FLAG)){
            FirebaseManageEngine.sendNotificationResponseMessage(true);
        }else if(action.equals(RESPONSE_NO_ACTION_FLAG)){
            FirebaseManageEngine.sendNotificationResponseMessage(false);
        }else{
            throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

}
