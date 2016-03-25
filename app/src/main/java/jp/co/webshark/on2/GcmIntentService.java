package jp.co.webshark.on2;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    private Handler toaster = new Handler();
    private Handler handler;
    private String toastValue;

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.d("LOG","messageType(error): " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d("LOG","messageType(deleted): " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d("LOG","messageType(message): " + messageType + ",body:" + extras.toString());

                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> processInfoList = am.getRunningAppProcesses();
                boolean isActive = false;

                for( ActivityManager.RunningAppProcessInfo info : processInfoList){
                    if(info.processName.equals(getPackageName())){
                        if( info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                            // app is FOREGROUND
                            //Log.e("","app is FOREGROUND");
                            isActive = true;
                            break;
                        }
                    }
                }

                Iterator<String> it = extras.keySet().iterator();
                String key;
                String value = (String) extras.get("article");

                int user_id = commonFucntion.getUserID(this.getApplicationContext());
                if( user_id > 0 ) {
                    // アプリ起動中ならトーストでメッセージを出す
                    if( isActive ){
                        toastValue = value;
                        if( toastValue != null ){
                            Thread service_thread = new Thread(null, task, toastValue);
                            service_thread.start();
                        }
                    }else{
                        if (value.indexOf("TALK,") == 0) {
                            String[] talkArray = value.split(",", -1);
                            if( talkArray.length >= 5 ){
                                if( talkArray[5].equals("00") ){
                                    //通知バーに表示
                                    sendNotification("メッセージが届いています");
                                }
                            }
                        }else{
                            //通知バーに表示
                            sendNotification(value);
                        }

                    }
                }

            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);


    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            toaster.post(new Runnable() {
                public void run() {
                    // 実行中のクラス名はここで取得できる
                    ActivityManager activityManager = (ActivityManager) getSystemService(Service.ACTIVITY_SERVICE);
                    String className = activityManager.getRunningTasks(3).get(0).topActivity.getClassName();

                    if (toastValue.indexOf("TALK,") == 0) {
                        // トーク画面を開いている時は読み込みイベントを発生させる
                        if (className.equals("jp.co.webshark.on2.talkActivity")) {
                            sendBroadCast(toastValue);
                        }
                    } else {
                        Toast.makeText(GcmIntentService.this, toastValue, Toast.LENGTH_LONG).show();
                    }

                    // フッタメニューがある画面を開いている時は読み込みイベントを発生させる
                    if (className.equals("jp.co.webshark.on2.homeActivity")
                            || className.equals("jp.co.webshark.on2.onListActivity")
                            || className.equals("jp.co.webshark.on2.hiListActivity")) {
                        sendBroadCast(toastValue);
                    }

                }
            });
            stopSelf();
        }
    };

    private void sendNotification(String msg) {

        if( msg == null ){
            return;
        }else if( msg.equals("") ){
            return;
        }

        Notification notification;
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, startActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle("ON")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        long[] vibrate_ptn = {0, 100, 100, 100}; // 振動パターン（適当）
        notification = mBuilder.build();
        notification.vibrate = vibrate_ptn;
        notification.defaults |= Notification.DEFAULT_LIGHTS; // デフォルトLED点滅パターンを設定
        //notification.flags = Notification.FLAG_ONGOING_EVENT;

        //mBuilder.setContentIntent(contentIntent);
        Calendar now = Calendar.getInstance(); //インスタンス化

        int h = now.get(now.HOUR_OF_DAY); //時を取得
        int m = now.get(now.MINUTE);      //分を取得
        int s = now.get(now.SECOND);      //秒を取得
        int ms = now.get(now.MILLISECOND); //ミリ秒を取得
        int uniqueId = (h*10000000)+(m*100000)+(s*1000)+ms;
        mNotificationManager.notify(uniqueId, notification);
        now = null;
        notification = null;
        mBuilder = null;
        mNotificationManager = null;

    }

    public void registerHandler(Handler UpdateHandler) {
        handler = UpdateHandler;
    }

    protected void sendBroadCast(String message) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("message", message);
        broadcastIntent.setAction("UPDATE_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);

    }
}