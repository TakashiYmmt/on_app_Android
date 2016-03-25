package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.Window;

import com.facebook.AccessToken;

import java.util.HashMap;

public class startActivity extends Activity {
    private commonFucntion cf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // FB認証チェック(予約)
        AccessToken accessToken = commonFucntion.checkFbLogin(getApplicationContext());

        try{
            // 許可を求めるダイアログをメインスレッドで出す為にカーソルを作成
            Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            cursor = null;

            // アプリ開始時にアドレス帳リストを作ってしまう(時間がかかる為)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AsyncPost checkFriendSender;
                    String strJsonArray = null;

                    cf = new commonFucntion();
                    cf.setMyAddress(getApplicationContext());

                    /*int user_id = commonFucntion.getUserID(getApplicationContext());

                    strJsonArray = cf.getArrayTelJsonList(getApplicationContext());

                    // プロフィール取得用API通信のコールバック
                    checkFriendSender = new AsyncPost(new AsyncCallback() {
                        public void onPreExecute() {}
                        public void onProgressUpdate(int progress) {}
                        public void onCancelled() {}
                        public void onPostExecute(String result) {}
                    });

                    String strURL = getResources().getString(R.string.api_url);
                    HashMap<String,String> body = new HashMap<String,String>();

                    body.put("entity", "checkUserFriend");
                    body.put("user_id", String.valueOf(user_id));
                    body.put("json_array_tel", strJsonArray);

                    // API通信のPOST処理
                    checkFriendSender.setParams(strURL, body);
                    checkFriendSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    */
                    cf = null;
                }
            }).start();
        }catch (Exception e){}

        Handler hdl = new Handler();

        commonFucntion cf = new commonFucntion();
        //cf.setUserID(this.getApplicationContext(), "4");
        if( cf.getUserID(this.getApplicationContext()) != -1 ) {
            hdl.postDelayed(new splashHandlerMain(), 2000);
        }
        else{
            hdl.postDelayed(new splashHandlerRegist(), 2000);
        }
    }

    class splashHandlerRegist implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), telephoneActivity.class);
            startActivity(intent);
            startActivity.this.finish();
        }
    }
    class splashHandlerMain implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), homeActivity.class);
            startActivity(intent);
            startActivity.this.finish();
        }
    }
}
