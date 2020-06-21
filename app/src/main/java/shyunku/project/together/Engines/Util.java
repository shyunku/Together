package shyunku.project.together.Engines;

import android.app.Activity;

public class Util {
    public static void terminateApp(Activity activity, int code){
        activity.finishAffinity();
        System.exit(code);
    }
}
