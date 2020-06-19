package shyunku.project.together.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class UserDump {
    @PropertyName("username")
    public String username = "-";
    public String deviceID = "";
    @PropertyName("subParty")
    public String subordinatedParty = "";

    public UserDump() {
    }

    public UserDump(String username, String deviceID) {
        this.username = username;
        this.deviceID = deviceID;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", this.username);
        result.put("deviceID", this.deviceID);
        result.put("subParty", this.subordinatedParty);

        return result;
    }
}
