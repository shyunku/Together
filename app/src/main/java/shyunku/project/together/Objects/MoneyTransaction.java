package shyunku.project.together.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class MoneyTransaction {
    @PropertyName("type")
    public boolean isGeneral = true;

    //General
    @PropertyName("name")
    public String transactionName = "";
    public String OwedUsername = "";
    public String value = "";

    //Common
    public String timestamp = "";

    public MoneyTransaction(){

    }

    public MoneyTransaction(String name, String owedUsername, String value, String timestamp){
        isGeneral = true;
        this.transactionName = name;
        this.OwedUsername = owedUsername;
        this.value = value;
        this.timestamp = timestamp;
    }

    public MoneyTransaction(String timestamp){
        isGeneral = false;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", this.isGeneral);
        result.put("name", this.transactionName);
        result.put("owed", this.OwedUsername);
        result.put("value", this.value);
        result.put("timestamp", this.timestamp);

        return result;
    }
}
