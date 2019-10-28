package shyunku.project.together.Objects;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserLocation {
    private double latitude;
    private double longitude;

    public UserLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", this.latitude);
        result.put("longitude", this.longitude);

        return result;
    }
}
