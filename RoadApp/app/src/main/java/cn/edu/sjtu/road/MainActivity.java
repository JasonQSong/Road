package cn.edu.sjtu.road;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
//如果使用地理围栏功能，需要import如下类
import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocationStatusCodes;
import com.baidu.location.GeofenceClient;
import com.baidu.location.GeofenceClient.OnAddBDGeofencesResultListener;
import com.baidu.location.GeofenceClient.OnGeofenceTriggerListener;
import com.baidu.location.GeofenceClient.OnRemoveBDGeofencesResultListener;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;


import android.provider.Settings.Secure;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        SettingsFragment.SettingsCallbacks,
        HomeFragment.HomeCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    private HomeFragment mHomeFragment;
    private SettingsFragment mSettingsFragment;

    private String mDevice;// = Integer.parseInt(getString(R.string.settings_device_id_value));
    private double mLongitudinal;// = Double.parseDouble(getString(R.string.settings_longitudinal_wheelbase_value));
    private double mTransverse;// = Double.parseDouble(getString(R.string.settings_transverse_wheelbase_value));
    private double mLongitude;// = Double.parseDouble(getString(R.string.default_longitude));
    private double mLatitude;// = Double.parseDouble(getString(R.string.default_latitude));
    private int mLocationType;
    private String mServer;// = getString(R.string.settings_server_value);
    private LinkedList<AccelerometerModel> AccelerometerModelLinkedList;// = new LinkedList<AccelerometerModel>();
    private int mPackageTotal;// = Integer.parseInt(getString(R.string.sensor_package_total));
    private int mPackageCollect;// = Integer.parseInt(getString(R.string.sensor_package_collect));
    private boolean isPassingHole;//=false;
    private int passingHoleCount;// = 0;
    private double mVelocity;
    private double mEntryRatioX;
    private double mEntryRatioY;
    private double mDirection;
    private LocationModel mLastLocation;
    private double lastVar;
    private double allVar;

    private JSONObject runtimeData;


    protected void sendAccelerometerArray(final LinkedList<AccelerometerModel> accelerometerArrayData, final ICallback iCallback) {
        Log.v("sendAccelerometer", "size:" + accelerometerArrayData.size());
        if (accelerometerArrayData.size() == 0) {
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < accelerometerArrayData.size(); i++) {
                if (accelerometerArrayData.get(i).sent == false) {
                    Log.v("sendAccelerometer", accelerometerArrayData.get(i).toJson().toString());
                    jsonArray.put(accelerometerArrayData.get(i).toJson());
                    accelerometerArrayData.get(i).sent = true;
                }
            }
            PostThread postThread = new PostThread();
            postThread.urlStr = "http://" + mServer + "/api/accelerometers/createArray";
            postThread.contentType = "application/json";
            postThread.content = jsonArray.toString();
            postThread.iCallback = new ICallback() {
                @Override
                public void callback(Object object) {
                    Log.v("SendAccelerometer", object.toString());
                    if (iCallback != null) {
                        iCallback.callback(object);
                    }
                }
            };
            PostThread.threadPool.execute(postThread);
        } catch (Exception e) {
            e.printStackTrace();
        }

        class SendAccelerometerArrayThread extends Thread {
            LinkedList<AccelerometerModel> accelerometerArrayData;

            public SendAccelerometerArrayThread(LinkedList<AccelerometerModel> accelerometerArrayData) {
                this.accelerometerArrayData = (LinkedList<AccelerometerModel>) accelerometerArrayData.clone();
            }

            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < accelerometerArrayData.size(); i++) {
                        Log.v("sendAccelerometer", accelerometerArrayData.get(i).toJson().toString());
                        jsonArray.put(accelerometerArrayData.get(i).toJson());
                    }
                    byte[] entity = jsonArray.toString().getBytes();
                    URL url = new URL("http://" + mServer + "/api/accelerometers/createArray");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Content-Length", String.valueOf(entity.length));
                    OutputStream os = connection.getOutputStream();
                    os.write(entity);
                    int responseCode = connection.getResponseCode();
                    Log.v("sendAccelerometer", "" + responseCode);
                    if (responseCode == 201) {
                        url = new URL("http://" + mServer + "/api/holes/test/" + mDevice + "/" + accelerometerArrayData.get(0).timeUTC + "/" + accelerometerArrayData.size());
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        responseCode = connection.getResponseCode();
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        String responseStr = response.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void sendAccelerometer(AccelerometerModel accelerometerData, final ICallback iCallback) {
        if (accelerometerData.sent == true) {
            if (iCallback != null) {
                iCallback.callback(new JSONObject());
            }
            return;
        }
        try {
            accelerometerData.sent = true;
            Log.v("SendAccelerometer", accelerometerData.toJson().toString());
            PostThread postThread = new PostThread();
            postThread.urlStr = "http://" + mServer + "/api/accelerometers";
            postThread.contentType = "application/json";
            postThread.content = accelerometerData.toJson().toString();
            postThread.iCallback = new ICallback() {
                @Override
                public void callback(Object object) {
                    Log.v("SendAccelerometer", object.toString());
                    if (iCallback != null) {
                        iCallback.callback(object);
                    }
                }
            };
            PostThread.threadPool.execute(postThread);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected double calcAvg(List<AccelerometerModel> accelerometerModelLinkedList) {
        double sum = 0;
        for (int i = 0; i < accelerometerModelLinkedList.size(); i++) {
            sum += accelerometerModelLinkedList.get(i).z;
        }
        return sum / accelerometerModelLinkedList.size();
    }

    protected double calcVar(List<AccelerometerModel> accelerometerModelLinkedList) {
        double avg = calcAvg(accelerometerModelLinkedList);
        double sumVar = 0;
        for (int i = 0; i < accelerometerModelLinkedList.size(); i++) {
            double dev = accelerometerModelLinkedList.get(i).z - avg;
            sumVar += dev * dev;
        }
        return sumVar / accelerometerModelLinkedList.size();
    }

    public LocationManager mLocationManager = null;
    public LocationClient mBDLocationClient = null;

    protected void onCreateSetupGoogleLocationService() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    mLongitude = location.getLongitude();
                    mLatitude = location.getLatitude();
                    Log.v("AndroidLocation", "LocationChanged" + ",longitude:" + mLongitude + ",latitude" + mLatitude);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.v("AndroidLocation", "LocationStatusChanged" + ",provider:" + provider + ",status:" + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.v("AndroidLocation", "ProviderEnabled" + ",provider:" + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.v("AndroidLocation", "ProviderDisabled" + ",provider:" + provider);
            }
        });
    }

    protected void onCreateSetupBaiduLocationService() {
        mBDLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        //option.setCoorType("gcj02");
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        option.setOpenGps(true);
        mBDLocationClient.setLocOption(option);
    }

    public long lastCountTime = 0;
    public int lastCountNum = 0;
    public long lastUpdateAccelerometerUITime = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //init
        mDevice = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        mLongitudinal = Double.parseDouble(getString(R.string.settings_longitudinal_wheelbase_value));
        mTransverse = Double.parseDouble(getString(R.string.settings_transverse_wheelbase_value));
        mLongitude = Double.parseDouble(getString(R.string.default_longitude));
        mLatitude = Double.parseDouble(getString(R.string.default_latitude));
        mServer = getString(R.string.settings_server_value);
        AccelerometerModelLinkedList = new LinkedList<AccelerometerModel>();
        mPackageTotal = Integer.parseInt(getString(R.string.sensor_package_total));
        mPackageCollect = Integer.parseInt(getString(R.string.sensor_package_collect));
        mVelocity = 1;
        mEntryRatioX = 0.5;
        mEntryRatioY = 0.5;
        isPassingHole = false;
        passingHoleCount = 0;
        try {
            runtimeData = new JSONObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //onCreateSetupGoogleLocationService();
        onCreateSetupBaiduLocationService();

        mBDLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation != null) {
                    mLongitude = bdLocation.getLongitude();
                    mLatitude = bdLocation.getLatitude();
                    mLocationType = bdLocation.getLocType();
                    mDirection = bdLocation.getDirection();
                    LocationModel nowLocationModel = new LocationModel();
                    nowLocationModel.device = mDevice;
                    nowLocationModel.timeUTC = new Date().getTime();
                    nowLocationModel.longitude = mLongitude;
                    nowLocationModel.latitude = mLatitude;
                    nowLocationModel.locType = mLocationType;
                    nowLocationModel.direction = mDirection;
                    double distance =0;
                    mVelocity=0;
                    if(mLastLocation!=null) {
                        DistanceUtil.getDistance(new LatLng(mLatitude, mLongitude), new LatLng(mLastLocation.latitude, mLastLocation.longitude));
                        mVelocity = distance * 1000 / (mLastLocation.timeUTC - nowLocationModel.timeUTC);
                    }
                    nowLocationModel.velocity = mVelocity;
                    if (mHomeFragment != null && mHomeFragment.isResumed()) {
                        ((TextView) findViewById(R.id.text_view_home_latitude)).setText(Double.toString(mLatitude));
                        ((TextView) findViewById(R.id.text_view_home_longitude)).setText(Double.toString(mLongitude));
                        ((TextView) findViewById(R.id.text_view_home_direction)).setText(Double.toString(mDirection));
                        ((TextView) findViewById(R.id.text_view_home_loc_type)).setText(Double.toString(mLocationType));
                        ((TextView) findViewById(R.id.text_view_home_velocity)).setText(Double.toString(mVelocity));
                    }
                    if (mHomeFragment != null && mHomeFragment.isResumed() && mHomeFragment.mMapView != null) {
                        mHomeFragment.mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                                        .target(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude()))
                                        .zoom(16)
                                        .build()
                        ));
                        mHomeFragment.mMapView.getMap().setMyLocationData(new MyLocationData.Builder()
                                        .accuracy(bdLocation.getRadius())
                                        .direction(bdLocation.getDirection())
                                        .latitude(bdLocation.getLatitude())
                                        .longitude(bdLocation.getLongitude())
                                        .build()
                        );
                    }
                    String urlStr = "http://" + mServer + "/api/locations";
                    try {
                        PostThread.postJson(urlStr, nowLocationModel.toJson(), null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mLastLocation = nowLocationModel;
                    Log.v("BDLocation", "LocationChanged" + ",locType:" + mLocationType + ",longitude:" + mLongitude + ",latitude" + mLatitude);
                }
            }
        });    //注册监听函数
        mBDLocationClient.start();


        //sensor
        SensorManager sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        final Sensor accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                AccelerometerModel data = new AccelerometerModel();
                data.device = mDevice;
                data.longitudinal = mLongitudinal;
                data.transverse = mTransverse;
                data.timeUTC = (new Date()).getTime();
                data.longitude = mLongitude;
                data.latitude = mLatitude;
                data.x = event.values[SensorManager.DATA_X];
                data.y = event.values[SensorManager.DATA_Y];
                data.z = event.values[SensorManager.DATA_Z];
                try {
                    if (mHomeFragment != null && mHomeFragment.isResumed() && data.timeUTC > lastUpdateAccelerometerUITime + 100) {
                        ((TextView) findViewById(R.id.text_view_home_time)).setText(DateFormat.getDateTimeInstance().format(new Date(data.timeUTC)));
                        ((TextView) findViewById(R.id.text_view_home_accelerometer_x)).setText(Double.toString(data.x));
                        ((TextView) findViewById(R.id.text_view_home_accelerometer_y)).setText(Double.toString(data.y));
                        ((TextView) findViewById(R.id.text_view_home_accelerometer_z)).setText(Double.toString(data.z));
                        lastUpdateAccelerometerUITime = data.timeUTC;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //sendAccelerometer(data, null);

                while (AccelerometerModelLinkedList.size() >= mPackageTotal)
                    AccelerometerModelLinkedList.remove(0);
                AccelerometerModelLinkedList.add(data);
                try {
                    if (data.timeUTC > lastCountTime + 1000) {
                        Log.d("SensorCount", "lastCountTime:" + lastCountTime + ",lastCountNum:" + lastCountNum);
                        lastCountTime = data.timeUTC - data.timeUTC % 1000;
                        lastCountNum = 0;
                    }
                    lastCountNum++;
                    Log.v("sensor", data.toJson().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int start = AccelerometerModelLinkedList.size() - 5;
                start = start < 0 ? 0 : start;
                lastVar = calcVar(AccelerometerModelLinkedList.subList(start, AccelerometerModelLinkedList.size()));
                allVar = calcVar(AccelerometerModelLinkedList);
                if (lastVar > allVar * 2 && lastVar > 100 && AccelerometerModelLinkedList.size() > 10 && !isPassingHole) {
                    isPassingHole = true;
                    passingHoleCount = 0;
                    Log.v("CalcVar", "isPassingHole:" + isPassingHole + "lastVar:" + lastVar + "allVar:" + allVar);
                }
                if (isPassingHole) {
                    passingHoleCount++;
                    if (passingHoleCount >= mPackageCollect) {
                        sendAccelerometerArray(AccelerometerModelLinkedList, new ICallback() {
                            @Override
                            public void callback(Object object) {
                                int responseCode = 0;
                                try {
                                    responseCode = ((JSONObject) object).getInt("responseCode");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (responseCode == 201) {
                                    String urlStr = "http://" + mServer + "/api/holes/test/" + mDevice + "/" + AccelerometerModelLinkedList.get(0).timeUTC + "/" + AccelerometerModelLinkedList.size()
                                            + "?velocity=" + mVelocity + "&entryRatioX=" + mEntryRatioX + "&entryRatioY=" + mEntryRatioY + "&longitude=" + mLongitude + "&latitude=" + mLatitude;
                                    //urlStr="http://192.168.2.100:9000/api/holes/test/7feb16c3e9406d80/1432823706650/500?velocity=1&entryRatioX=0.5&entryRatioY=0.5&longitude=0&latitude=0";
                                    GetThread.get(urlStr, new ICallback() {
                                        @Override
                                        public void callback(Object object) {
                                            try {
                                                final int responseCode = ((JSONObject) object).getInt("responseCode");
                                                final String response = ((JSONObject) object).getString("response");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        double lastDiameter = 0;
                                                        double lastDepth = 0;
                                                        String lastHoleId="0";
                                                        try {
                                                            JSONObject jsonObject = new JSONObject(response);
                                                            lastDiameter = jsonObject.getDouble("diameter");
                                                            lastDepth = jsonObject.getDouble("depth");
                                                            lastHoleId=                                 jsonObject.getString("_id");
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        ((TextView) findViewById(R.id.text_view_home_last_diameter)).setText(Double.toString(lastDiameter));
                                                        ((TextView) findViewById(R.id.text_view_home_last_depth)).setText(Double.toString(lastDepth));

                                                        String urlStr = "http://" + mServer + "/api/combinedHoles/test/" + lastHoleId + "/" + new Date().getTime();
                                                        GetThread.get(urlStr, null);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        isPassingHole = false;
                        passingHoleCount = 0;
                    }
                }

                ((TextView) findViewById(R.id.text_view_home_runtime_data_cached)).setText(Integer.toString(AccelerometerModelLinkedList.size()));
                ((TextView) findViewById(R.id.text_view_home_runtime_is_passing_hole)).setText(Boolean.toString(isPassingHole));
                ((TextView) findViewById(R.id.text_view_home_runtime_passing_hole_count)).setText(Integer.toString(passingHoleCount));
                ((TextView) findViewById(R.id.text_view_home_runtime_last_var)).setText(Double.toString(lastVar));
                ((TextView) findViewById(R.id.text_view_home_runtime_all_var)).setText(Double.toString(allVar));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onHomeFragmentResume() {
        if (mHomeFragment != null) {
            ((TextView) findViewById(R.id.text_view_home_device)).setText(mDevice);
            ((TextView) findViewById(R.id.text_view_home_server)).setText(mServer);
            ((TextView) findViewById(R.id.text_view_home_longitudinal)).setText(Double.toString(mLongitudinal));
            ((TextView) findViewById(R.id.text_view_home_transverse)).setText(Double.toString(mTransverse));
            ((TextView) findViewById(R.id.text_view_home_entry_ratio_x)).setText(Double.toString(mEntryRatioX));
            ((TextView) findViewById(R.id.text_view_home_entry_ratio_y)).setText(Double.toString(mEntryRatioY));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                mHomeFragment = HomeFragment.newInstance();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mHomeFragment)
                        .commit();
                break;
            case 1:
                mSettingsFragment = SettingsFragment.newInstance();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mSettingsFragment)
                        .commit();
                break;
            default:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
        }
    }

    public void onButtonRefreshHomePressed(View view) {
        showSettings();
    }

    public void onButtonSetPressed(View view) {
        onSettingsChanged();
    }

    @Override
    public void onSettingsChanged() {
        EditText et;
        et = (EditText) findViewById(R.id.settings_device_id_value);
        mDevice = (et.getText().toString());
        et = (EditText) findViewById(R.id.settings_device_id_value);
        mServer = et.getText().toString();
        et = (EditText) findViewById(R.id.settings_device_id_value);
        mLongitudinal = Double.parseDouble(et.getText().toString());
        et = (EditText) findViewById(R.id.settings_device_id_value);
        mTransverse = Double.parseDouble(et.getText().toString());
    }

    public void showSettings() {
//        TextView tv = (TextView) findViewById(R.id.home_screen_settings);
//        String settingsString = "";
//        settingsString += "mDevice:" + mDevice + "\n";
//        settingsString += "mLongitudinal:" + mLongitudinal + "\n";
//        settingsString += "mTransverse:" + mTransverse + "\n";
//        settingsString += "mLongitude:" + mLongitude + "\n";
//        settingsString += "mLatitude:" + mLatitude + "\n";
//        settingsString += "mServer:" + mServer + "\n";
//        tv.setText(settingsString);


    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }

    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
