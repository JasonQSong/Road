package cn.edu.sjtu.road;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jason on 5/5/2015.
 */
public class AccelerometerModel {
    public String device;
    public double longitudinal;
    public double transverse;
    public long timeUTC;
    public double longitude;
    public double latitude;
    public double x;
    public double y;
    public double z;
    public boolean sent=false;
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("device", device);
        object.put("timeUTC", timeUTC);
        object.put("x", x);
        object.put("y", y);
        object.put("z", z);
        return object;
    }
}
