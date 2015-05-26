package cn.edu.sjtu.road;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 5/25/2015.
 */
public class GetThread extends Thread{
    String urlStr="";
    ICallback iCallback=null;

    public GetThread(){}
    public GetThread(String urlStr,ICallback iCallback){
        this.urlStr=urlStr;
        this.iCallback=iCallback;
    }
    @Override
    public void run(){
        JSONObject jsonObject=new JSONObject();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            Log.v("GetThread", "url:" + urlStr);
            int responseCode = connection.getResponseCode();
            String responseStr = "";
            if(responseCode<400) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                responseStr = response.toString();
            }
            Log.v("GetThread","responseCode:"+responseCode+",response:"+responseStr);
            jsonObject.put("responseCode",responseCode)      ;
            jsonObject.put("response",responseStr)      ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(iCallback!=null){
            iCallback.callback(jsonObject);
        }
    }
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static void get(String urlStr,ICallback iCallback){
        GetThread getThread=new GetThread(urlStr,iCallback);
        threadPool.execute(getThread);
    }
}
