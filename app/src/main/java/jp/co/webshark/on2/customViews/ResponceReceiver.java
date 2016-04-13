package jp.co.webshark.on2.customViews;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by takashi on 2016/03/15.
 */
public class ResponceReceiver extends BroadcastReceiver {

    public static Handler handler;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        String request = bundle.getString("request");
        String result = bundle.getString("result");

        if(handler !=null){
            Message msg = new Message();

            Bundle data = new Bundle();
            data.putString("request", request);
            data.putString("result", result);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    /**
     * メイン画面の表示を更新
     */
    public void registerHandler(Handler locationUpdateHandler) {
        handler = locationUpdateHandler;
    }

}
