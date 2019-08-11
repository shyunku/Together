package shyunku.project.together.Objects;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Chat {
    public String sender;
    public String timestamp;
    public boolean isRead;
    public String content;
    public String id;


    public Chat(){

    }

    public Chat(String sender, String timestamp, boolean isRead, String content, String id){
        this.sender = sender;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.content = content;
        this.id = id;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("sender", this.sender);
        result.put("content", this.content);
        result.put("timestamp", this.timestamp);
        result.put("isRead", this.isRead);
        result.put("id", this.id);

        return result;
    }
}
