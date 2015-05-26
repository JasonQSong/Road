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
 * Created by jason on 5/28/2015.
 */
public class PostThread extends Thread {
    String urlStr = "";
    String content = "";
    String contentType = "text";
    ICallback iCallback = null;

    public PostThread() {
    }

    public PostThread(String urlStr, String content, ICallback iCallback) {
        this.urlStr = urlStr;
        this.content = content;
        this.iCallback = iCallback;
    }

    @Override
    public void run() {
        JSONObject jsonObject = new JSONObject();
        try {
            byte[] entity = content.toString().getBytes();
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Length", String.valueOf(entity.length));
            OutputStream os = connection.getOutputStream();
            Log.v("PostThread", "url:" + urlStr + ",content:" + content);
            os.write(entity);
            int responseCode = connection.getResponseCode();
            String responseStr = "";
            if (responseCode < 400) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                responseStr = response.toString();
            }
            Log.v("PostThread", "responseCode:" + responseCode + ",response:" + responseStr);
            jsonObject.put("responseCode", responseCode);
            jsonObject.put("response", responseStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (iCallback != null) {
            iCallback.callback(jsonObject);
        }
    }

    public static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void post(String urlStr, String content, ICallback iCallback) {
        PostThread postThread = new PostThread(urlStr, content, iCallback);
        threadPool.execute(postThread);
    }
}
