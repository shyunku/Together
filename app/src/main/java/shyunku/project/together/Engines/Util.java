package shyunku.project.together.Engines;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class Util {
    public static void terminateApp(Activity activity, int code){
        activity.finishAffinity();
        System.exit(code);
    }
}
