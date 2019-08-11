package shyunku.project.together.Objects;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String name;
    public String status;
    public int happiness;

    public User(){

    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("happiness", this.happiness);
        result.put("name", this.name);
        result.put("status", this.status);

        return result;
    }
}
