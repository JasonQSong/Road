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
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


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

    private int mDevice;// = Integer.parseInt(getString(R.string.settings_device_id_value));
    private double mLongitudinal;// = Double.parseDouble(getString(R.string.settings_longitudinal_wheelbase_value));
    private double mTransverse;// = Double.parseDouble(getString(R.string.settings_transverse_wheelbase_value));
    private double mLongitude;// = Double.parseDouble(getString(R.string.default_longitude));
    private double mLatitude;// = Double.parseDouble(getString(R.string.default_latitude));
    private String mServer;// = getString(R.string.settings_server_value);
    private LinkedList<AccelerometerModel> AccelerometerModelLinkedList;// = new LinkedList<AccelerometerModel>();
    private int mPackageTotal;// = Integer.parseInt(getString(R.string.sensor_package_total));
    private int mPackageCollect;// = Integer.parseInt(getString(R.string.sensor_package_collect));
    private boolean isPassingHole;//=false;
    private int passingHoleCount;// = 0;

    private JSONObject runtimeData;

    private ExecutorService threadPool;

    protected void sendAccelerometerArray(final LinkedList<AccelerometerModel> accelerometerArray) {
        Log.v("sendAccelerometer", ""+accelerometerArray.size());
        if(accelerometerArray.size()==0)
            return;
//        for(int i=0;i<accelerometerArray.size();i++){
//            sendAccelerometer(accelerometerArray.get(i));
//        }

        class SendAccelerometerArrayThread extends Thread {
            LinkedList<AccelerometerModel> accelerometerArrayData;

            public SendAccelerometerArrayThread(LinkedList<AccelerometerModel> accelerometerArrayData) {
                this.accelerometerArrayData =(LinkedList<AccelerometerModel>) accelerometerArrayData.clone();
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
                        url = new URL("http://" + mServer + "/api/holes/test/" + mDevice + "/" + accelerometerArrayData.get(0).time + "/" + accelerometerArrayData.size());
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        responseCode = connection.getResponseCode();
                        BufferedReader in = new BufferedReader(                                    new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        String responseStr = response.toString();
                        TextView tv = (TextView) findViewById(R.id.home_screen_last_hole);
                        if (tv != null)
                            tv.setText(responseStr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        threadPool.execute(new SendAccelerometerArrayThread(accelerometerArray));
    }
    protected void sendAccelerometer(AccelerometerModel accelerometerData) {
        class SendAccelerometerThread extends Thread {
            AccelerometerModel accelerometerData;

            public SendAccelerometerThread(AccelerometerModel accelerometerData) {
                this.accelerometerData = accelerometerData;
            }

            @Override
            public void run() {
                try {
                    Log.v("sendAccelerometer", accelerometerData.toJson().toString());
                    byte[] entity = accelerometerData.toJson().toString().getBytes();
                    URL url = new URL("http://" + mServer + "/api/accelerometers");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Content-Length", String.valueOf(entity.length));
                    OutputStream os = connection.getOutputStream();
                    os.write(entity);
                    Log.v("sendAccelerometer", "" + connection.getResponseCode());
                    Log.v("sendAccelerometer", "" + connection.getResponseMessage());
            /*
            String model = "";
            model += "device=" + data.device + "\n";
            model += "&longitudinal=" + data.longitudinal + "\n";
            model += "&transverse=" + data.transverse + "\n";
            model += "&time=" + data.time + "\n";
            model += "&longitude=" + data.longitude + "\n";
            model += "&latitude=" + data.latitude + "\n";
            model += "&x=" + data.x + "\n";
            model += "&y=" + data.y + "\n";
            model += "&z=" + data.z + "\n";
            */
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        threadPool.execute(new SendAccelerometerThread(accelerometerData));
    }
    protected double calcAvg(List<AccelerometerModel> accelerometerModelLinkedList){
        double sum=0;
        for(int i=0;i<accelerometerModelLinkedList.size();i++){
            sum+=accelerometerModelLinkedList.get(i).z;
        }
        return sum/accelerometerModelLinkedList.size();
    }

    protected double calcVar(List<AccelerometerModel> accelerometerModelLinkedList){
        double sum=0;
        for(int i=0;i<accelerometerModelLinkedList.size();i++){
            sum+=accelerometerModelLinkedList.get(i).z;
        }
        double avg=sum/accelerometerModelLinkedList.size();
        double sumvar=0;
        for(int i=0;i<accelerometerModelLinkedList.size();i++){
            sumvar+=accelerometerModelLinkedList.get(i).z-avg;
        }
        return sumvar/accelerometerModelLinkedList.size();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //init
        threadPool = Executors.newCachedThreadPool();
        mDevice = Integer.parseInt(getString(R.string.settings_device_id_value));
        mLongitudinal = Double.parseDouble(getString(R.string.settings_longitudinal_wheelbase_value));
        mTransverse = Double.parseDouble(getString(R.string.settings_transverse_wheelbase_value));
        mLongitude = Double.parseDouble(getString(R.string.default_longitude));
        mLatitude = Double.parseDouble(getString(R.string.default_latitude));
        mServer = getString(R.string.settings_server_value);
        AccelerometerModelLinkedList = new LinkedList<AccelerometerModel>();
        mPackageTotal = Integer.parseInt(getString(R.string.sensor_package_total));
        mPackageCollect = Integer.parseInt(getString(R.string.sensor_package_collect));
        isPassingHole = false;
        passingHoleCount = 0;
        try {
            runtimeData = new JSONObject();
    } catch (Exception e) {
        e.printStackTrace();
    }
        //sensor
        SensorManager sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        final Sensor accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sm.registerListener(new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {

                LocationManager locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE) ;
                Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location!=null) {
                    mLongitude = location.getLongitude();
                    mLatitude = location.getLatitude();
                }
                AccelerometerModel data = new AccelerometerModel();
                data.device = mDevice;
                data.longitudinal = mLongitudinal;
                data.transverse = mTransverse;
                data.time = (new java.util.Date()).getTime();
                data.longitude = mLongitude;
                data.latitude = mLatitude;
                data.x = event.values[SensorManager.DATA_X];
                data.y = event.values[SensorManager.DATA_Y];
                data.z = event.values[SensorManager.DATA_Z];

                TextView tv = (TextView) findViewById(R.id.home_screen_text_view);
                try {
                    if(tv!=null)
                        tv.setText(data.toJson().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //sendAccelerometer(data);

                while (AccelerometerModelLinkedList.size() >= mPackageTotal)
                    AccelerometerModelLinkedList.remove(0);
                AccelerometerModelLinkedList.add(data);
                try {
                    Log.v("sensor", data.toJson().toString());
                } catch (Exception e){
                    e.printStackTrace();
                }
                int start=AccelerometerModelLinkedList.size()-5;
                start=start<0?0:start;
                double lastVar=calcVar(AccelerometerModelLinkedList.subList(start,AccelerometerModelLinkedList.size()));
                double allVar=calcVar(AccelerometerModelLinkedList);
                if(lastVar> allVar&&AccelerometerModelLinkedList.size()>10)           {
                    isPassingHole = true;
                }
                if (data.x != 0||data.y!=0||data.z!=0) {//TODO filter
                    isPassingHole = true;
                }
                if (isPassingHole) {
                    passingHoleCount++;
                    if (passingHoleCount >= mPackageCollect) {
                        sendAccelerometerArray(AccelerometerModelLinkedList);
                        isPassingHole = false;
                        passingHoleCount = 0;
                    }
                }

                try {
                runtimeData.put("isPassingHole",isPassingHole);
                    runtimeData.put("progress",AccelerometerModelLinkedList.size());
                tv = (TextView) findViewById(R.id.home_screen_runtime);
                    if(tv!=null)
                        tv.setText(runtimeData.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                mHomeFragment = HomeFragment.newInstance("", "");
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

    public void onButtonRefreshHomePressed(View view){
        showSettings();
    }
    public void onButtonSetPressed(View view){
        onSettingsChanged();
    }
    @Override
    public void onSettingsChanged() {
        EditText et;
        et=(EditText)findViewById(R.id.settings_device_id_value);
        mDevice= Integer.parseInt(et.getText().toString());
        et=(EditText)findViewById(R.id.settings_device_id_value);
        mServer= et.getText().toString();
        et=(EditText)findViewById(R.id.settings_device_id_value);
        mLongitudinal= Double.parseDouble(et.getText().toString());
        et=(EditText)findViewById(R.id.settings_device_id_value);
        mTransverse = Double.parseDouble(et.getText().toString());
    }
    public void showSettings() {
        TextView tv = (TextView) findViewById(R.id.home_screen_settings);
        String settingsString = "";
        settingsString += "mDevice:" + mDevice + "\n";
        settingsString += "mLongitudinal:" + mLongitudinal + "\n";
        settingsString += "mTransverse:" + mTransverse + "\n";
        settingsString += "mLongitude:" + mLongitude + "\n";
        settingsString += "mLatitude:" + mLatitude + "\n";
        settingsString += "mServer:" + mServer + "\n";
        tv.setText(settingsString);
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

    @Override
    public void onHomeChanged() {

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
