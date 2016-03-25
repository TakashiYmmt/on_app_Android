package jp.co.webshark.on2;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;

import com.facebook.*;
import com.facebook.GraphRequest.*;
import com.facebook.internal.CollectionMapper;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.co.webshark.on2.customViews.ResponceReceiver;
import jp.co.webshark.on2.customViews.UpdateReceiver;

public class fbLoginActivity extends Activity {

    //private AsyncPost fbIdSender;
    //private AsyncPost fbIdListSender;
    List<String> graphUsersList;
    String fbIdList = null;
    CallbackManager callbackManager;
    LoginManager loginManager;
    private boolean isDestroy = false;
    private UpdateReceiver upReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_login);

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        loginManager = LoginManager.getInstance();

        loginManager.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // プロフィール画面へ遷移
                        setFbId(loginResult.getAccessToken().getUserId());
                        getFbFriendsFromSession(loginResult.getAccessToken());

                        Intent intent = new Intent(getApplicationContext(), profileEditActivity.class);
                        startActivity(intent);
                        fbLoginActivity.this.finish();
                    }

                    @Override
                    public void onCancel() {
                        // プロフィール画面へ遷移
                        Intent intent = new Intent(getApplicationContext(), profileEditActivity.class);
                        startActivity(intent);
                        fbLoginActivity.this.finish();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // プロフィール画面へ遷移
                        Intent intent = new Intent(getApplicationContext(), profileEditActivity.class);
                        startActivity(intent);
                        fbLoginActivity.this.finish();
                    }
                });

        Collection<String> permissions = Arrays.asList("public_profile", "user_friends");

        loginManager.logInWithReadPermissions(this, permissions); // Null Pointer Exception here

        upReceiver = new UpdateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(upReceiver, intentFilter);

        upReceiver.registerHandler(updateHandler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onDestroy(){
        isDestroy = true;
        super.onDestroy();
        if( upReceiver != null ){
            unregisterReceiver(upReceiver);
        }
        System.gc();
    }

    @Override
    public void onStart(){
        isDestroy = false;
        super.onStart();
    }

    @Override
    public void onPause(){
        isDestroy = true;
        super.onPause();
    }

    private void getFbFriendsFromSession(AccessToken accessToken){
        GraphRequestBatch batch = new GraphRequestBatch(
            GraphRequest.newMyFriendsRequest(
                    accessToken,
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray jsonArray,GraphResponse response) {
                            JSONObject graphResponse = response.getJSONObject();
                            //graphUsersList = clsJson2Objects.setFbFriendId(graphResponse);
                            fbIdList = clsJson2Objects.setFbFriendId(graphResponse);
                            addFbFriends();
                        }
                    })

    );
        batch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequests) {
                // Application code for when the batch finishes
            }
        });
        batch.executeAsync();

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name");
    }

    private void setFbId(String fbId){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "setFacebookID");
        body.put("user_id", String.valueOf(user_id));
        body.put("facebook_id", fbId);
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // プロフィール取得用API通信のコールバック
        AsyncPost fbIdSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
            }
        });

        // API通信のPOST処理
        fbIdSender.setParams(strURL, body);
        fbIdSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void addFbFriends(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "createFriendFromFacebookID");
        body.put("user_id", String.valueOf(user_id));
        body.put("facebook_id_list", fbIdList);
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // プロフィール取得用API通信のコールバック
        AsyncPost fbIdListSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
            }
        });
        // API通信のPOST処理
        fbIdListSender.setParams(strURL, body);
        fbIdListSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            return;
        }
    };
}
