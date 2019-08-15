package shyunku.project.together.Objects;

import android.content.Context;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import shyunku.project.together.Activities.MainActivity;
import shyunku.project.together.R;

public class User {
    public String name = "";
    public String status = "";
    public int happiness = 0;
    public String FCMtoken = "EMPTY";

    public User(){

    }

    public String getStatusDescription(Context context){
        if(status.equals(context.getResources().getString(R.string.status_boring_message)))
            return context.getResources().getString(R.string.status_boring_description);
        if(status.equals(context.getResources().getString(R.string.status_hungry_message)))
            return context.getResources().getString(R.string.status_hungry_description);
        if(status.equals(context.getResources().getString(R.string.status_out_message)))
            return context.getResources().getString(R.string.status_out_description);
        if(status.equals(context.getResources().getString(R.string.status_private_message)))
            return context.getResources().getString(R.string.status_private_description);
        if(status.equals(context.getResources().getString(R.string.status_sleepy_message)))
            return context.getResources().getString(R.string.status_sleepy_description);
        if(status.equals(context.getResources().getString(R.string.status_sleeping_message)))
            return context.getResources().getString(R.string.status_sleeping_description);
        return context.getResources().getString(R.string.status_public_description);
    }

    public int getStatusBackgroundColorTag(Context context){
        if(status.equals(context.getResources().getString(R.string.status_boring_message)))
            return R.color.status_boring;
        if(status.equals(context.getResources().getString(R.string.status_hungry_message)))
            return R.color.status_hungry;
        if(status.equals(context.getResources().getString(R.string.status_out_message)))
            return R.color.status_out;
        if(status.equals(context.getResources().getString(R.string.status_private_message)))
            return R.color.status_private;
        if(status.equals(context.getResources().getString(R.string.status_sleepy_message)))
            return R.color.status_sleepy;
        if(status.equals(context.getResources().getString(R.string.status_sleeping_message)))
            return R.color.status_sleeping;
        return R.color.status_opened;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("happiness", this.happiness);
        result.put("name", this.name);
        result.put("status", this.status);
        result.put("token", this.FCMtoken);

        return result;
    }
}
