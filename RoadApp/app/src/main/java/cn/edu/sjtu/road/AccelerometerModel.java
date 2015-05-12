package cn.edu.sjtu.road;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jason on 5/5/2015.
 */
public class AccelerometerModel {
    public int device;
    public double longitudinal;
    public double transverse;
    public long time;
    public double longitude;
    public double latitude;
    public double x;
    public double y;
    public double z;
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("device", device);
        object.put("longitudinal", longitudinal);
        object.put("transverse", transverse);
        object.put("time", time);
        object.put("longitude", longitude);
        object.put("latitude", latitude);
        object.put("x", x);
        object.put("y", y);
        object.put("z", z);
        return object;
    }
}
