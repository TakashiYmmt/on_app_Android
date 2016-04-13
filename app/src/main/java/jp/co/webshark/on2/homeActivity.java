package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
////import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import android.net.Uri;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.co.webshark.on2.customViews.DetectableKeyboardEventLayout;
import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.SwipeListView;
import jp.co.webshark.on2.customViews.UpdateReceiver;
import jp.co.webshark.on2.customViews.UrlImageView;

import static java.lang.Thread.sleep;

public class homeActivity extends commonActivity {
//public class homeActivity extends Activity {

    private clsUserInfo userInfo;
    private ArrayList<clsGroupInfo> groupList;
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    private LinearLayout footerLayout;
    private int footerHeight;
    //private ListView listView;
    private SwipeListView listView;
    private ScrollView scrollView;
    private EditText editText;
    //private AsyncPost profileSender;
    //private AsyncPost profileGetter;
    //private AsyncPost onCountGetter;
    //private AsyncPost groupGetter;
    //private AsyncPost groupHiSender;
    //private AsyncPost groupOnSender;
    //private AsyncPost sysInfoGetter;
    //private AsyncPost badgeInfoGetter;
    //private AsyncPost messageGetter;
    private AsyncPost commonAsyncPost;
    private String sendGroupIndex;
    private String inviteMessage;
    private boolean openKeyBoard;
    private GoogleCloudMessaging gcm;
    private ImageView allSwitchButton;
    private View eventTriggerView;
    private String refreshOnFlg;
    private boolean forceUpdate;
    private clsSystemInfo sysInfo;
    //private String profileComment;
    private boolean commentFlg;
    private UpdateReceiver upReceiver;
    private IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        commentFlg = true;

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        footerLayout = (LinearLayout)findViewById(R.id.footer);

        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // 画面上のオブジェクト
        //listView = (ListView) findViewById(R.id.listView1);
        listView = (SwipeListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);
        editText = (EditText) findViewById(R.id.profileCommentEdit);
        allSwitchButton = (ImageView)findViewById(R.id.friends_switch_icon);

        DetectableKeyboardEventLayout root = (DetectableKeyboardEventLayout)findViewById(R.id.body);
        root.setKeyboardListener(new DetectableKeyboardEventLayout.KeyboardListener() {

            @Override
            public void onKeyboardShown() {
                //Log.d(TAG, "keyboard shown");
                openKeyBoard = true;
                footerLayout.setVisibility(View.INVISIBLE);
                ViewGroup.LayoutParams params = footerLayout.getLayoutParams();
                params.height = 0;
                footerLayout.setLayoutParams(params);
            }

            @Override
            public void onKeyboardHidden() {
                if (openKeyBoard) {
                    if( commentFlg ){
                        checkComment();
                        openKeyBoard = false;
                        footerLayout.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params = footerLayout.getLayoutParams();
                        params.height = getResources().getDimensionPixelSize(R.dimen.footer_height);
                        footerLayout.setLayoutParams(params);
                    }
                    //commentFlg = true;
                }
                footerLayout.setVisibility(View.VISIBLE);
            }
        });

        // GCMに端末IDを登録しておく
        try{
            gcm = GoogleCloudMessaging.getInstance(this);
            registerInBackground();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"emurator?", Toast.LENGTH_LONG).show();
        }

        // TestOnly
        if( getResources().getString(R.string.is_test).equals("1") ){
            ImageView logo = (ImageView) findViewById(R.id.navigationLogo);
            registerForContextMenu(logo);
        }

        onGlobal onGlobal = (onGlobal) this.getApplication();
        Object checkUpdate = onGlobal.getShareData("checkUpdate");
        if( checkUpdate != null && checkUpdate.equals("chcked")){
            forceUpdate = false;
        }else{
            forceUpdate = true;
        }

        ((TextView) findViewById(R.id.tabFriendButtonBadge)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tabHiButtonBadge)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setVisibility(View.GONE);
        upReceiver = new UpdateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(upReceiver, intentFilter);

        upReceiver.registerHandler(updateHandler);
  }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if( upReceiver != null ){
            unregisterReceiver(upReceiver);
        }

        this.userInfo = null;
        this.groupList = null;
        this.inputMethodManager = null;
        this.footerLayout = null;
        this.editText = null;
        //this.profileSender = null;
        //this.profileGetter = null;
        //this.onCountGetter = null;
        //this.groupGetter = null;
        //this.groupHiSender = null;
        //this.groupOnSender = null;
        this.sendGroupIndex = null;
        this.listView = null;
        this.scrollView = null;
        this.mainLayout = null;

        System.gc();
    }

    // 実験
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        //コンテキストメニューの設定
        menu.setHeaderTitle("デバッグ用メニュー");        //Menu.add(int groupId, int itemId, int order, CharSequence title)
        menu.add(0, 0, 0, "初期登録画面から始める");
        //menu.add(0, 1, 0, "友達を全削除する");
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;

        switch (item.getItemId()) {
            case 0:
                intent = new Intent(getApplicationContext(),telephoneActivity.class);
                startActivity(intent);
                return true;
            case 1:
                setDebugSender();
                this.delAllFriend();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // APIコールバック定義
    /*
    private void setProfileGetter(){
    }
    private void setCountGetter(){
    }
    private void setGroupGetter(){
    }
    private void setGroupHiSender(){
    }
    private void setGroupOnSender(){
    }
    private void setProfileSender(){
    }

    private void setSysInfoGetter(){
    }

    private void setBadgeInfoGetter(){
    }

    private void setMessageGetter(){
    }
    private void setFbIdListSender(){
    }

    */

    @Override
    public void onResume(){
        System.gc();
        super.onResume();

        /*
        // コールバックの初期化
        this.setProfileGetter();
        this.setCountGetter();
        this.setGroupGetter();
        this.setGroupHiSender();
        this.setGroupOnSender();
        this.setProfileSender();
        this.setSysInfoGetter();
        this.setBadgeInfoGetter();
        this.setMessageGetter();
        this.setFbIdListSender();
        */

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getUserInfo();
        this.getCountInfo();
        this.getGroupInfo();
        this.getBadgeInfo();

        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

        if(forceUpdate) {
            getSysInfo();
        }
    }

    private void getUserInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getUserInfo");
        body.put("device_type", "2");
        body.put("user_id", String.valueOf(user_id));

        // プロフィール取得用API通信のコールバック
        AsyncPost profileGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    drawUserInfo(clsJson2Objects.setUserInfo(result));
                    //setProfileGetter();
                }
            }
        });

        // API通信のPOST処理
        profileGetter.setParams(strURL, body);
        profileGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawUserInfo(clsUserInfo userInfo){
        HttpImageView profImage = (HttpImageView) findViewById(R.id.profile_image);
        EditText profileCommentEdit = (EditText) findViewById(R.id.profileCommentEdit);

        profImage.setImageUrl(userInfo.getImageURL(), getResources().getDimensionPixelSize(R.dimen.home_profile_image), getApplicationContext(), true);
        profileCommentEdit.setHint(Html.fromHtml("<small><small>" + getResources().getString(R.string.homeAct_profileCommentHint) + "</small></small>"));
        profileCommentEdit.setText(userInfo.getComment());
        //setProfileGetter();

        // pushコメント機能の為にグローバル領域にコメントテキストを保存
        commonFucntion.setComment(getApplication(), userInfo.getComment());
    }

    private void getCountInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getOnPersonCount");
        body.put("user_id", String.valueOf(user_id));

        // ONカウント取得用API通信のコールバック
        AsyncPost onCountGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    drawCountInfo(clsJson2Objects.setCountInfo(result));
                    //setCountGetter();
                }
            }
        });
        // API通信のPOST処理
        onCountGetter.setParams(strURL, body);
        onCountGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawCountInfo(clsCountInfo countInfo){
        TextView onCountText = (TextView) findViewById(R.id.onCount);
        if( countInfo.getCount().equals("0") ){
            onCountText.setText(getResources().getString(R.string.homeAct_onCountZero));
            onCountText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_gray));
            allSwitchButton.setImageResource(R.drawable.home_button_off);
            refreshOnFlg = "off";
        }else{
            onCountText.setText(String.format(getResources().getString(R.string.homeAct_onCount), countInfo.getCount()));
            onCountText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_active_green));
            allSwitchButton.setImageResource(R.drawable.home_button_on_andr);
            refreshOnFlg = "on";
        }

        //setCountGetter();
    }

    private void drawBadgeInfo(String onCount, String hiCount){
        if( !onCount.equals("0") && !onCount.equals("") ){
            ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setText(onCount);
            ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setVisibility(View.VISIBLE);
        }else{
            ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setVisibility(View.GONE);
        }
        if( !hiCount.equals("0") && !hiCount.equals("") ){
            ((TextView) findViewById(R.id.tabHiButtonBadge)).setText(hiCount);
            ((TextView) findViewById(R.id.tabHiButtonBadge)).setVisibility(View.VISIBLE);
        }else{
            ((TextView) findViewById(R.id.tabHiButtonBadge)).setVisibility(View.GONE);
        }
    }

    private void getGroupInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getUserTagList");
        body.put("user_id", String.valueOf(user_id));

        // ONカウント取得用API通信のコールバック
        AsyncPost groupGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                    //setGroupGetter();
                }
            }
        });
        // API通信のPOST処理
        groupGetter.setParams(strURL, body);
        groupGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getMessage(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getSMSInfo");

        // プロフィール取得用API通信のコールバック
        AsyncPost messageGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    inviteMessage = clsJson2Objects.getElement(result,"article");
                    try {
                        inviteMessage = URLEncoder.encode(inviteMessage, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //setMessageGetter();
                    putLine();
                }
            }
        });
        // API通信のPOST処理
        messageGetter.setParams(strURL, body);
        messageGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawGroupInfo(ArrayList<clsGroupInfo> list){

        GroupsAdapter adapter = new GroupsAdapter(homeActivity.this);
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.home_cell_height);

        this.groupList = list;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // 実際のListViewに反映する
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        //params.height = list.size() * params.height;
        params.height = list.size() * cellHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();

        adapter.notifyDataSetChanged();
        //scrollView.fullScroll(ScrollView.FOCUS_UP);

        //this.setGroupGetter();
    }

    private String profileComment;
    private void checkComment(){
        profileComment = editText.getText().toString();

        if( profileComment.length() > 50 ){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setMessage(getResources().getString(R.string.homeAct_profileCommentError));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog = null;

                            profileComment = profileComment.substring(0,50);
                            editText.setText(profileComment);
                            sendComment(profileComment);
                            //setProfileSender();
                        }
                    }).setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            sendComment(profileComment);
            //this.setProfileSender();
        }
        commentFlg = true;
    }

    private void sendComment(String profileComment){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "setProfileComment");
        body.put("user_id", String.valueOf(user_id));
        body.put("profile_comment", profileComment);

        //グループ一斉Hi送信用API通信のコールバック
        AsyncPost groupOnSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                if(!isDestroy){
                    drawGroupOn();
                    //setGroupOnSender();
                }
            }
        });
        // API通信のPOST処理
        groupOnSender.setParams(strURL, body);
        groupOnSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendPushNotifyKey(String registId){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity","updateUserinfoSingleColumn");
        body.put("column_name", "push_notify_key");
        body.put("column_value", registId);
        body.put("user_id", String.valueOf(user_id));

        // プロフィール情報更新API通信のコールバック
        AsyncPost profileSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                //drawGroupOn();
                //setProfileSender();
            }
        });
        // API通信のPOST処理
        profileSender.setParams(strURL, body);
        profileSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateAll(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        if( refreshOnFlg.equals("off") ){
            body.put("entity","SendOnAllFriend");
        }else{
            body.put("entity","OffAll");
        }
        body.put("user_id", String.valueOf(user_id));
        body.put("profile_comment", profileComment);

        //グループ一斉Hi送信用API通信のコールバック
        AsyncPost groupOnSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                if(!isDestroy){
                    drawGroupOn();
                    //setGroupOnSender();
                }
            }
        });
        // API通信のPOST処理
        groupOnSender.setParams(strURL, body);
        groupOnSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendGroupHi(String groupIndex){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String groupId = this.groupList.get(Integer.parseInt(groupIndex)).getTagId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "SendHiGroupID");
        body.put("user_id", String.valueOf(user_id));
        body.put("tag_id", groupId);
        body.put("profile_comment", profileComment);

        //グループ一斉Hi送信用API通信のコールバック
        AsyncPost groupHiSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                if(!isDestroy){
                    drawGroupHi();
                    //setGroupHiSender();
                }
            }
        });
        // API通信のPOST処理
        groupHiSender.setParams(strURL,body);
        groupHiSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawGroupHi(){
        AsyncDraw groupHiEfect = new AsyncDraw(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
            }
        });
        //this.setGroupHiSender();

    }

    private void sendGroupOn(String groupIndex){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String groupId = this.groupList.get(Integer.parseInt(groupIndex)).getTagId();
        String onFlg = this.groupList.get(Integer.parseInt(groupIndex)).getOnFlg();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        if( onFlg.equals("0") ){
            onFlg = "1";
        }else{
            onFlg = "0";
        }

        body.put("entity", "SendOnGroupID");
        body.put("user_id", String.valueOf(user_id));
        body.put("tag_id", groupId);
        body.put("on_flg", onFlg);
        body.put("profile_comment", profileComment);

        //グループ一斉Hi送信用API通信のコールバック
        AsyncPost groupOnSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                if(!isDestroy){
                    drawGroupOn();
                    //setGroupOnSender();
                }
            }
        });
        // API通信のPOST処理
        groupOnSender.setParams(strURL, body);
        groupOnSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getSysInfo(){
        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getSysInfoAndroid");

        // システム情報取得用API通信のコールバック
        AsyncPost sysInfoGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    sysInfo = clsJson2Objects.setSysInfo(result);
                    checkUpdate(getApplicationContext());
                    checkD064();
                    //setSysInfoGetter();
                }
            }
        });
        // API通信のPOST処理
        sysInfoGetter.setParams(strURL, body);
        sysInfoGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getBadgeInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getTagBadgeCount");
        body.put("user_id", String.valueOf(user_id));

        AsyncPost badgeInfoGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    String onCount = clsJson2Objects.getElement(result,"on_count");
                    String hiCount = clsJson2Objects.getElement(result,"hi_count");
                    drawBadgeInfo(onCount, hiCount);
                    //setBadgeInfoGetter();
                }
            }
        });

        // API通信のPOST処理
        badgeInfoGetter.setParams(strURL, body);
        badgeInfoGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawGroupOn(){

        // データ再取得・描画
        this.getUserInfo();
        this.getCountInfo();
        this.getGroupInfo();

        //this.setGroupOnSender();
    }

    private class GroupsAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater = null;

        public GroupsAdapter(Context context){
            this.context = context;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return groupList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.home_view_cell01,parent,false);

            ((TextView)convertView.findViewById(R.id.cellGroupName)).setText(
                    String.format("%s(%s)", groupList.get(position).getTagName(), groupList.get(position).getTagCount()));

            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            if( groupList.get(position).getOnFlg().equals("1") ){
                ImageView switchButton = (ImageView)convertView.findViewById(R.id.cellSwitchButton);
                switchButton.setImageResource(R.drawable.list_button_on);
            }

            //((UrlImageView)convertView.findViewById(R.id.group_icon)).setImageUrl("file:./group"+groupList.get(position).getTagId()+".jpg");


            Bitmap bm = commonFucntion.loadBitmapCache(getApplicationContext(), "group"+groupList.get(position).getTagId()+".jpg");
            if( bm != null ){
                ((ImageView)convertView.findViewById(R.id.group_icon)).setImageBitmap(bm);
                bm = null;
            }

            EffectImageView targetImage = (EffectImageView)convertView.findViewById(R.id.all_hi);
            targetImage.setSwitchEffect(R.drawable.loading_hi, 2000);

            return convertView;
        }
    }

    // プライバシーポリシーボタン
    public void openPrivacyPolicy(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),privacyPolicyActivity.class);
        startActivity(intent);
    }

    // ONリストボタン
    public void openOnList(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),onListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    // homeボタン
    public void openHome(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),homeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Hiリストボタン
    public void openHiList(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),hiListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    // 友達リストボタン
    public void openFriendList(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),friendListActivity.class);
        startActivity(intent);
        this.finish();
    }

    // 全員ON・OFFボタン
    public void allOnOff(View view){
        profileComment = "";

        if( refreshOnFlg.equals("on") ){
            updateAll();
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setMessage(getResources().getString(R.string.homeAct_allOn_confirm));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //updateAll();

                            if( refreshOnFlg.equals("on") ){
                                updateAll();
                            }else{

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(homeActivity.this);
                                //setViewにてビューを設定します。
                                final EditText editView = new EditText(homeActivity.this);

                                if( commonFucntion.getComment(getApplication()).equals("") ){
                                    editView.setHint(Html.fromHtml("<small><small>" + getResources().getString(R.string.homeAct_profileCommentHint) + "</small></small>"));
                                    editView.setSingleLine();
                                    alertDialogBuilder.setMessage("コメントを付けますか？\nあなたの居場所ややりたい事をコメントに(50文字以内)");
                                    alertDialogBuilder.setView(editView);
                                    alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    //キーボードを隠す(ポップアップの時はキーボード格納のイベントを意図的に無視させる)
                                                    openKeyBoard = false;
                                                    commentFlg = false;
                                                    //inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                                    if (getCurrentFocus() != null) {
                                                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                                    }
                                                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                                                    profileComment = editView.getText().toString();
                                                    editText.setText(profileComment);
                                                    commonFucntion.setComment(getApplication(), profileComment);

                                                    updateAll();
                                                    profileComment = null;

                                                    dialog.dismiss();
                                                    dialog = null;

                                                    //背景にフォーカスを移す
                                                    allSwitchButton.requestFocus();

                                                    footerLayout.setVisibility(View.VISIBLE);
                                                    ViewGroup.LayoutParams params = footerLayout.getLayoutParams();
                                                    params.height = getResources().getDimensionPixelSize(R.dimen.footer_height);
                                                    footerLayout.setLayoutParams(params);

                                                }
                                            });
                                    alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    dialog = null;
                                                    return;
                                                }
                                            });
                                    alertDialogBuilder.setCancelable(false);
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }else{
                                    updateAll();
                                }
                            }

                            dialog.dismiss();
                            dialog = null;

                        }
                    });
            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog = null;

                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }



        /*
        if( refreshOnFlg.equals("on") ){
            updateAll();
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setMessage( getResources().getString(R.string.homeAct_allOn_confirm) );
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateAll();

                            dialog.dismiss();
                            dialog = null;

                        }
                    });
            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog = null;

                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        */
    }

    /*
    // グループON・OFFボタン
    public void groupOnOff(View view){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendGroupIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        sendGroupOn(sendGroupIndex);
        sendGroupIndex = null;
    }
    */
    // グループON・OFFボタン
    public void groupOnOff(View view){
        profileComment = "";

        RelativeLayout cell = (RelativeLayout) view.getParent();
        for (int i = 0; i < cell.getChildCount(); i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if (i == 0) {
                    TextView hiddenText = (TextView) childview;
                    sendGroupIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        String onFlg = this.groupList.get(Integer.parseInt(sendGroupIndex)).getOnFlg();

        if( onFlg.equals("1") ){
            sendGroupOn(sendGroupIndex);
            sendGroupIndex = null;
            profileComment = null;
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            if( commonFucntion.getComment(getApplication()).equals("") ){
                //setViewにてビューを設定します。
                final EditText editView = new EditText(homeActivity.this);
                editView.setHint(Html.fromHtml("<small><small>" + getResources().getString(R.string.homeAct_profileCommentHint) + "</small></small>"));
                editView.setSingleLine();
                alertDialogBuilder.setMessage("コメントを付けますか？\nあなたの居場所ややりたい事をコメントに(50文字以内)");
                alertDialogBuilder.setView(editView);

                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                profileComment = editView.getText().toString();
                                editText.setText(profileComment);
                                commonFucntion.setComment(getApplication(), profileComment);

                                sendGroupOn(sendGroupIndex);
                                sendGroupIndex = null;
                                profileComment = null;

                                //キーボードを隠す(ポップアップの時はキーボード格納のイベントを意図的に無視させる)
                                openKeyBoard = false;
                                commentFlg = false;
                                inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                //背景にフォーカスを移す
                                listView.requestFocus();

                                footerLayout.setVisibility(View.VISIBLE);
                                ViewGroup.LayoutParams params = footerLayout.getLayoutParams();
                                params.height = getResources().getDimensionPixelSize(R.dimen.footer_height);
                                footerLayout.setLayoutParams(params);

                                dialog.dismiss();
                                dialog = null;

                            }
                        });
                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dialog = null;
                                return;
                            }
                        });
                alertDialogBuilder.setCancelable(false);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }else{
                sendGroupOn(sendGroupIndex);
                sendGroupIndex = null;
                profileComment = null;
            }
        }
    }

    // プロフィール画像リンク
    public void openProfileEdit(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),profileEditActivity.class);
        startActivity(intent);
    }

    // グループを選択
    public void groupEdit(View view){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        RelativeLayout cell = (RelativeLayout)view;
        int childCount = ((RelativeLayout)view).getChildCount();
        ViewGroup vg = (ViewGroup) view;
        String selectedName = "";

        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendGroupIndex = hiddenText.getText().toString();
                }
            }
        }

        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),groupEditActivity.class);
        intent.putExtra("groupId", groupList.get(Integer.parseInt(sendGroupIndex)).getTagId());
        intent.putExtra("groupName", groupList.get(Integer.parseInt(sendGroupIndex)).getTagName());
        startActivityForResult(intent, 0);
    }

    // グループ一括Hi
    View effectTargetView;
    public void groupHi(View view){

        effectTargetView = view;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendGroupIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        alertDialogBuilder.setMessage( getResources().getString(R.string.homeAct_groupHi_confirm) );
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if( commonFucntion.getComment(getApplication()).equals("") ){
                            //setViewにてビューを設定します。
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(homeActivity.this);
                            final EditText editView = new EditText(homeActivity.this);
                            editView.setHint(Html.fromHtml("<small><small>" + getResources().getString(R.string.homeAct_profileCommentHint) + "</small></small>"));
                            editView.setSingleLine();
                            alertDialogBuilder.setMessage("コメントを付けますか？\nあなたの居場所ややりたい事をコメントに(50文字以内)");
                            alertDialogBuilder.setView(editView);

                            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            commentFlg = false;
                                            profileComment = editView.getText().toString();
                                            editText.setText(profileComment);
                                            commonFucntion.setComment(getApplication(), profileComment);

                                            sendGroupHi(sendGroupIndex);
                                            sendGroupIndex = null;
                                            profileComment = null;

                                            //キーボードを隠す(ポップアップの時はキーボード格納のイベントを意図的に無視させる)
                                            openKeyBoard = false;
                                            inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                            //背景にフォーカスを移す
                                            listView.requestFocus();

                                            footerLayout.setVisibility(View.VISIBLE);
                                            ViewGroup.LayoutParams params = footerLayout.getLayoutParams();
                                            params.height = getResources().getDimensionPixelSize(R.dimen.footer_height);
                                            footerLayout.setLayoutParams(params);

                                            dialog.dismiss();
                                            dialog = null;
                                            commentFlg = true;

                                        }
                                    });
                            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            dialog = null;
                                            return;
                                        }
                                    });
                            alertDialogBuilder.setCancelable(false);
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }else{
                            ((EffectImageView)effectTargetView).doEffect();
                            effectTargetView = null;
                            sendGroupHi(sendGroupIndex);
                            sendGroupIndex = null;
                        }

                        dialog.dismiss();
                        dialog = null;
                        commentFlg = true;

                    }
                });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialog = null;
                        commentFlg = true;

                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        profileComment = null;

    }

    // グループを追加
    public void addGroup(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),groupEditActivity.class);
        intent.putExtra("groupId", "");
        startActivityForResult(intent, 0);
    }
    // QRを読み取り
    public void readQr(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),profileEditReadQrActivity.class);
        startActivityForResult(intent, 0);
    }

    // 自分のQRを表示
    public void drawQr(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),profileEditDrawQrActivity.class);
        startActivityForResult(intent, 0);
    }

    // 招待ボタン
    public void openInviteFriends(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),inviteFriendsActivity.class);
        startActivity(intent);
    }

    // ID検索ボタン
    public void addId(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),searchFriendActivity.class);
        //intent.putExtra("groupId", "");
        //startActivityForResult(intent, 0);
        startActivity(intent);
    }

    // LINEを開く
    public void openLineApp(View view){
        // 招待メッセージを取得
        this.getMessage();
    }

    // 招待ボタン
    public void openInviteFacebook(){
        String appLinkUrl = "https://fb.me/231239513882666";
        String previewImageUrl = " http://app.webshark.co.jp/on/fb_inv.jpg";

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    .setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog.show(homeActivity.this,content);
        }
    }

    // 招待の選択から開く
    public void addInv(View view){
        final String[] items = {"LINE", "Facebook", "SMS", getResources().getString(R.string.cancel)};
        new AlertDialog.Builder(homeActivity.this)
                .setCancelable(true)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // item_which pressed
                        switch (which) {
                            case 0:
                                // 招待メッセージを取得してLINEをコールする
                                getMessage();
                                return;
                            case 1:
                                // FACEBOOKString appLinkUrl, previewImageUrl;
                                openInviteFacebook();
                                return;
                            case 2:
                                // SMS
                                Intent intentSMS = new Intent(getApplicationContext(),inviteFriendsActivity.class);
                                startActivity(intentSMS);
                            default:
                                return;
                        }

                    }
                })
                .show();
    }

    // リストを広げる・しまう
    public void groupOpenClose(View view){
        // 一方通行で開くだけ
        //ListView gl = (ListView)findViewById(R.id.listView1);
        SwipeListView gl = (SwipeListView)findViewById(R.id.listView1);
        ImageView iv = (ImageView)findViewById(R.id.groupArrow);
        if( gl.getVisibility() == View.VISIBLE ){
            gl.setVisibility(View.GONE);
            iv.setImageResource(R.drawable.home_icon_arrow_down);
        }else{
            gl.setVisibility(View.VISIBLE);
            iv.setImageResource(R.drawable.home_icon_arrow_up);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    // ダイアログ表示など特定の処理を行いたい場合はここに記述
                    // 親クラスのdispatchKeyEvent()を呼び出さずにtrueを返す
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * EditText編集時に背景をタップしたらキーボードを閉じるようにするタッチイベントの処理
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

        // キーボードが出ていた時はイベントをカット
        if( openKeyBoard ){
            return false;
        }else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

        return false;
    }

    private void registerInBackground() throws Exception{
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String strRegId = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    strRegId = gcm.register("544964133972");
                } catch (Exception ex) {
                    //msg = "Error :" + ex.getMessage();
                    //Toast.makeText(getApplicationContext(),"emurator?", Toast.LENGTH_LONG).show();
                    return "";
                }
                return strRegId;
            }

            @Override
            protected void onPostExecute(String strRegId) {
                // GCMを介して得られた端末IDをDBに登録する
                sendPushNotifyKey(strRegId);
            }
        }.execute(null, null, null);
    }

    private void checkUpdate(Context context){
        forceUpdate = false;

        PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        if( versionCode <= sysInfo.getForceUpdateVersion() ){
            forceUpdate = true;
            // 強制更新対象に含まれる場合はキャンセルなし

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(getResources().getString(R.string.homeAct_ForceUpdate));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
                            googlePlayIntent.setData(Uri.parse(sysInfo.getStoreUrl()));
                            startActivity(googlePlayIntent);

                            dialog.dismiss();
                            dialog = null;
                            finish();

                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if( versionCode < sysInfo.getNewestVersion() ){
            // 最新ではない
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(getResources().getString(R.string.homeAct_ForceUpdate));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
                            googlePlayIntent.setData(Uri.parse(sysInfo.getStoreUrl()));
                            startActivity(googlePlayIntent);

                            dialog.dismiss();
                            dialog = null;
                            finish();

                        }
                    });
            alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onGlobal onGlobal = (onGlobal) getApplication();
                            onGlobal.setShareData("checkUpdate","chcked");

                            dialog.dismiss();
                            dialog = null;

                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        }
    }

    private void checkD064(){
        // サンドボックスでアフィリエイトフ無い時だけブラウザを起動する
        if( commonFucntion.getD064Flg(this.getApplicationContext()).equals("0") ){
            commonFucntion.setD064Flg(this.getApplicationContext());
            int user_id = commonFucntion.getUserID(this.getApplicationContext());
            Intent d064Intent = new Intent(Intent.ACTION_VIEW);
            String d064Url = sysInfo.getD064BaseUrl() + String.format(sysInfo.getD064UrlParams(),String.valueOf(user_id));
            d064Intent.setData(Uri.parse(d064Url));
            startActivity(d064Intent);
        }

        // FB認証チェック
        AccessToken accessToken = commonFucntion.checkFbLogin(getApplicationContext());
        if( accessToken != null ){
            getFbFriendsFromSession(accessToken);
        }
    }

    private void putLine(){
        // 招待メッセージを添えてLINE起動する
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("line://msg/text/" + this.inviteMessage));
        startActivity(intent);
    }

// --- FB自動登録関連 --------------------------------------------------------------------------------
    private AsyncPost fbIdListSender;
    String fbIdList = null;
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

    private void addFbFriends(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "createFriendFromFacebookID");
        body.put("user_id", String.valueOf(user_id));
        body.put("facebook_id_list", fbIdList);

        // プロフィール取得用API通信のコールバック
        AsyncPost fbIdListSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //setFbIdListSender();
            }
        });
        // API通信のPOST処理
        fbIdListSender.setParams(strURL, body);
        fbIdListSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // DEBUG ONLY!!!
    private AsyncPost debugSender;
    private void delAllFriend(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "deleteAllFriend");
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        debugSender.setParams(strURL, body);
        //debugSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setDebugSender(){
        // API通信のコールバック
        debugSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(clsJson2Objects.isOK(result)){
                    Toast.makeText(getApplicationContext(),"全削除しました！", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),"全削除できるのはテストユーザーだけです！", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getBadgeInfo();

        }
    };
}
