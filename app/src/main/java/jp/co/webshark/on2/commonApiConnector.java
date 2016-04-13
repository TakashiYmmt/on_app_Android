package jp.co.webshark.on2;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.HashMap;

/**
 * Created by takashi on 2016/03/15.
 */
public class commonApiConnector{
    private Context context;
    private String entity;

    public commonApiConnector(Context context){
        this.context = context;
    }

    public void requestTask(HashMap<String,String> body, String strURL){
        entity = body.get("entity");

        // API通信のコールバック
        AsyncPost connector = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                sendBroadCast(entity, result);
            }
        });
        // API通信のPOST処理
        connector.setParams(strURL, body);
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void sendBroadCast(String request, String result) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("trigger", "API");
        broadcastIntent.putExtra("request", request);
        broadcastIntent.putExtra("result", result);
        broadcastIntent.setAction("UPDATE_ACTION");
        this.context.sendBroadcast(broadcastIntent);

    }
}
