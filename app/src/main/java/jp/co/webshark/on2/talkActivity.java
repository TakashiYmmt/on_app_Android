package jp.co.webshark.on2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.webshark.on2.customViews.DetectableKeyboardEventLayout;
import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.FlexListView;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.UpdateReceiver;
import jp.co.webshark.on2.customViews.WrapTextView;

public class talkActivity extends commonActivity {
    private String friendId;
    private String talkId;
    private Bitmap friendImage;
    private InputMethodManager inputMethodManager;
    private boolean openKeyBoard;
    //private AsyncPost talkGetter;
    //private AsyncPost talkSender;
    private RelativeLayout mainLayout;
    private EditText inputObject;
    private ArrayList<clsTalkInfo> talkLog;
    private FlexListView listView;
    private ScrollView scrollView;
    private String strUserId;
    private UpdateReceiver upReceiver;
    private IntentFilter intentFilter;
    private clsFriendInfo friendInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        // 画面上のオブジェクト
        listView = (FlexListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);
        inputObject = (EditText)findViewById(R.id.idInputEditText);

        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        DetectableKeyboardEventLayout root = (DetectableKeyboardEventLayout)findViewById(R.id.body);
        root.setKeyboardListener(new DetectableKeyboardEventLayout.KeyboardListener() {

            @Override
            public void onKeyboardShown() {
                //Log.d(TAG, "keyboard shown");
            }

            @Override
            public void onKeyboardHidden() {
                //Log.d(TAG, "keyboard shown");
            }
        });

        int user_id = commonFucntion.getUserID(getApplicationContext());
        strUserId = String.valueOf(user_id);

        upReceiver = new UpdateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(upReceiver, intentFilter);

        upReceiver.registerHandler(updateHandler);

    }

    private class ScrollDown implements Runnable {
        public void run() {
            ((ScrollView) findViewById(R.id.scroll_body)).fullScroll(View.FOCUS_DOWN);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // 画面初期化時にAPIから取得・描画する分はここで
        onGlobal onGlobal = (onGlobal) this.getApplication();
        friendInfo = (clsFriendInfo)onGlobal.getShareData("selectFrined");

        friendId = friendInfo.getFriendId();
        friendImage = (Bitmap)onGlobal.getShareData("friendImage");
        ((TextView)findViewById(R.id.navigationTitle)).setText(String.format(getResources().getString(R.string.talkAct_naviTitle), friendInfo.getName()));
        this.getTalkLog();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if( upReceiver != null ){
            unregisterReceiver(upReceiver);
        }
        this.scrollView = null;
        this.mainLayout = null;

        System.gc();
    }

    public void getTalkLog(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getTalkLog");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        body.put("talk_id", talkId);

        // プロフィール取得用API通信のコールバック
        AsyncPost talkGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(!isDestroy){
                    if(clsJson2Objects.isOK(result)){
                        talkLog = clsJson2Objects.setTalkLog(result);
                        drawTalkLog(talkLog);
                    }
                }

            }
        });
        // API通信のPOST処理
        talkGetter.setParams(strURL, body);
        //talkGetter.execute();
        talkGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void sendMessage(View view){
        if( !inputObject.getText().toString().equals("") ){
            if( friendInfo.getOnFlg().equals("0") ){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                //setViewにてビューを設定します。
                alertDialogBuilder.setMessage(String.format("%sにONしますか？\n(トークはON同士でしかできません)", friendInfo.getName()));
                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                friendInfo.setOnFlg("1");
                                sendMessage();

                                //キーボードを隠す
                                inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                //背景にフォーカスを移す
                                mainLayout.requestFocus();

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
            }else if( !inputObject.getText().toString().equals("") ){
                sendMessage();
            }
        }

    }

    private void sendMessage(){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String talkMessage = inputObject.getText().toString();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "addTalkMessage");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        body.put("talk_id", talkId);
        body.put("talk_message", talkMessage);

        // プロフィール取得用API通信のコールバック
        AsyncPost talkSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(!isDestroy){
                    if(clsJson2Objects.isOK(result)){
                        inputObject.setText("");
                        talkId = clsJson2Objects.getElement(result,"talk_id");
                        getTalkLog();
                    }
                }

            }
        });
        // API通信のPOST処理
        talkSender.setParams(strURL,body);
        talkSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // 戻るリンク
    public void talkClose(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),onListActivity.class);
        startActivityForResult(intent, 0);
    }

    private void drawTalkLog(ArrayList<clsTalkInfo> list){

        FriendsAdapter adapter = new FriendsAdapter(talkActivity.this);
        talkLog = list;

        if( list.size() > 0 ) {
            talkId = list.get(0).getTalkId();
            scrollView.setVisibility(View.VISIBLE);

            adapter.setTalkLog(list);
            adapter.setImageBitmap(friendImage);
            adapter.setUserId(strUserId);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            scrollView.post(new ScrollDown());

        }else{
            scrollView.setVisibility(View.GONE);
        }
    }

    static private class FriendsAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater = null;
        ArrayList<clsTalkInfo> talkLog;
        Bitmap imageBitmap;
        String strUserId;

        public FriendsAdapter(Context context){
            this.context = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setTalkLog(ArrayList<clsTalkInfo> talkLog) {
            this.talkLog = talkLog;
        }

        public void setImageBitmap(Bitmap friendImage) {
            this.imageBitmap = friendImage;
        }

        public void setUserId(String strUserId) {
            this.strUserId = strUserId;
        }


        @Override
        public int getCount() {
            return talkLog.size();
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
            if( strUserId.equals(talkLog.get(position).getTalkUserId()) ){
                convertView = layoutInflater.inflate(R.layout.talk_view_cell02,parent,false);
            }else{
                convertView = layoutInflater.inflate(R.layout.talk_view_cell01,parent,false);
                ((ImageView)convertView.findViewById(R.id.friendImage)).setImageBitmap(imageBitmap);
            }

            ((TextView)convertView.findViewById(R.id.cellMessage)).setText(talkLog.get(position).getMessage().trim());

            //((TextView)convertView.findViewById(R.id.cellSendTime)).setText(talkLog.get(position).getDatatime());
            long lastHiSeconds = Long.parseLong(talkLog.get(position).getDatatime());
            if( lastHiSeconds/(60*60*24) > 0 ){
                ((TextView) convertView.findViewById(R.id.cellSendTime)).setText(String.format(parent.getResources().getString(R.string.lc_DaysAgo), lastHiSeconds / (60*60*24)));
                //((TextView)convertView.findViewById(R.id.cellHiTime)).setText(String.format("%d時間前",lastHiSeconds/3600));
            }else if( lastHiSeconds/3600 > 0 ){
                ((TextView) convertView.findViewById(R.id.cellSendTime)).setText(String.format(parent.getResources().getString(R.string.lc_HoursAgo), lastHiSeconds / (60*60)));
                //((TextView)convertView.findViewById(R.id.cellHiTime)).setText(String.format("%d時間前",lastHiSeconds/3600));
            }else if( lastHiSeconds/60 > 0 ){
                ((TextView) convertView.findViewById(R.id.cellSendTime)).setText(String.format(parent.getResources().getString(R.string.lc_MinitsAgo), lastHiSeconds / (60)));
                //((TextView)convertView.findViewById(R.id.cellHiTime)).setText(String.format("%d分前",lastHiSeconds/60));
            }else{
                ((TextView)convertView.findViewById(R.id.cellSendTime)).setText(parent.getResources().getString(R.string.lc_JustNow));
                //((TextView)convertView.findViewById(R.id.cellHiTime)).setText("たった今");
            }

            return convertView;
        }
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

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!isDestroy){
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");

                getTalkLog();
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    };
}
