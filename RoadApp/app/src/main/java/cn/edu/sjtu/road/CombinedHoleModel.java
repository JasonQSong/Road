package cn.edu.sjtu.road;

import org.json.JSONObject;

/**
 * Created by jason on 5/28/2015.
 */
public class CombinedHoleModel {

    public double diameter = 0;
    public double depth = 0;
    public double longitude = 0;
    public double latitude = 0;
    public int trust = 0;
    public double dis = 0;

    public void fromJson(JSONObject jsonObject) {
        try {
            diameter = jsonObject.getDouble("diameter");
            depth = jsonObject.getDouble("depth");
            longitude = jsonObject.getDouble("longitude");
            latitude = jsonObject.getDouble("latitude");
            trust = jsonObject.getInt("trust");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "trust:" + trust + ",depth:" + depth + ",diameter:" + diameter + ",dis:" + dis;
    }
}
