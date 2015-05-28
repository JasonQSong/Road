package cn.edu.sjtu.road;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jason on 5/27/2015.
 */
public class LocationModel {

    public String device;
    public long timeUTC;
    public double longitude;
    public double latitude;
    public double direction;
    public double velocity;
    public double locType;
    public boolean sent=false;

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("device", device);
        object.put("timeUTC", timeUTC);
        object.put("longitude", longitude);
        object.put("latitude", latitude);
        object.put("direction", direction);
        object.put("velocity", velocity);
        object.put("locType", locType);
        return object;
    }
}
