package shyunku.project.together.Objects;

import android.content.Context;
import android.util.Pair;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

import shyunku.project.together.R;

public class User {
    public String name = "-";
    public String status = "-";
    public int happiness = 0;
    @PropertyName("token")
    public String FCMtoken = "-";
    //public UserLocation location = new UserLocation(37.56, 126.97);
    public double latitude = 0;
    public double longitude = 0;
    public boolean allowLocShare = true;
    public String deviceID = "";
    public long registerTime = System.currentTimeMillis();

    public User(){
    }

    public User(String username, String deviceID){
        this.name = username;
        this.deviceID = deviceID;
    }

    public User(String username, String deviceID, String FCMtoken){
        this.name = username;
        this.deviceID = deviceID;
        this.FCMtoken = FCMtoken;
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
        if(status.equals(context.getResources().getString(R.string.status_inclass_message)))
            return context.getResources().getString(R.string.status_inclass_description);
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
        if(status.equals(context.getResources().getString(R.string.status_inclass_message)))
            return R.color.status_inclass;
        return R.color.status_opened;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("happiness", this.happiness);
        result.put("name", this.name);
        result.put("status", this.status);
        result.put("token", this.FCMtoken);
        result.put("latitude", this.latitude);
        result.put("longitude", this.longitude);
        result.put("location_share", this.allowLocShare);
        result.put("deviceID", this.deviceID);

        return result;
    }
}
