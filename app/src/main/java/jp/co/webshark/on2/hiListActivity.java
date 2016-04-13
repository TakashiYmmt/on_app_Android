package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.webshark.on2.customViews.CustomScrollView;
import jp.co.webshark.on2.customViews.DetectableKeyboardEventLayout;
import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.ResponceReceiver;
import jp.co.webshark.on2.customViews.SwipeListView;
import jp.co.webshark.on2.customViews.UpdateReceiver;
import jp.co.webshark.on2.customViews.UrlImageView;
import jp.co.webshark.on2.customViews.WrapTextView;


public class hiListActivity extends Activity {

    private RelativeLayout mainLayout;
    private ArrayList<clsFriendInfo> hiList;
    private ArrayList<clsFriendInfo> hiFull;
    private SwipeListView listView;
    private FriendsAdapter adapter;
    private CustomScrollView scrollView;
    private RelativeLayout nothingView;
    //private AsyncPost hiGetter;
    //private AsyncPost hiSender;
    //private AsyncPost flgSender;
    //private AsyncPost badgeInfoGetter;
    private String sendHiIndex;
    private InputMethodManager inputMethodManager;
    private boolean openKeyBoard;
    private boolean keepList;
    private UpdateReceiver upReceiver;
    private IntentFilter intentFilter;
    private boolean isDestroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hi_list);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // 画面上のオブジェクト
        listView = (SwipeListView) findViewById(R.id.listView1);
        scrollView = (CustomScrollView) findViewById(R.id.scroll_body);
        nothingView = (RelativeLayout) findViewById(R.id.nothingLayout);

        nothingView.setVisibility(View.GONE);
        //scrollView.scrollTo(0,0);

        DetectableKeyboardEventLayout root = (DetectableKeyboardEventLayout)findViewById(R.id.body);
        root.setKeyboardListener(new DetectableKeyboardEventLayout.KeyboardListener() {

            @Override
            public void onKeyboardShown() {
                //Log.d(TAG, "keyboard shown");
                openKeyBoard = true;
            }

            @Override
            public void onKeyboardHidden() {
                if (openKeyBoard) {
                    openKeyBoard = false;
                }
            }
        });
        ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setVisibility(View.GONE);

        upReceiver = new UpdateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(upReceiver, intentFilter);

        upReceiver.registerHandler(updateHandler);
        keepList = false;

        scrollView.setScrollToBottomListener(new CustomScrollView.ScrollToBottomListener() {
            @Override
            public void onScrollToBottom(CustomScrollView scrollView) {
                //Log.d("LOG", "messageType(FOOK): CatcheBottomEvent");
                if (addAdapter()) {
                    try {
                        Thread.sleep(250);
                        scrollView.eventFinish();
                    } catch (InterruptedException e) {
                    }
                } else {
                    scrollView.eventFinish();
                }
            }

        });

        scrollView.setScrollToTopListener(new CustomScrollView.ScrollToTopListener() {
            @Override
            public void onScrollToTop(CustomScrollView scrollView) {
                //Log.d("LOG", "messageType(FOOK): CatcheTopEvent");
                scrollView.eventFinish();
            }

        });
    }

    private boolean addAdapter() {
        int nowCount = hiList.size();
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.hi_list_cell_height);

        if( nowCount == hiFull.size() ){
            return false;
        }

        //Log.d("LOG", "messageType(FOOK):"+hiList.size()+":"+hiFull.size());
        for (int i = nowCount ; i < (nowCount + 15) ; i++) {
            if (i == hiFull.size()) {
                i = nowCount + 15;
            } else {
                hiList.add(hiFull.get(i));
            }
        }

        // 実際のListViewに反映する
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = hiList.size() * cellHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();

        adapter.notifyDataSetChanged();

        return true;
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

    @Override
    public void onResume(){
        System.gc();
        super.onResume();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getHiList();
        this.clearHiBadge();
        this.getBadgeInfo();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if( upReceiver != null ){
            unregisterReceiver(upReceiver);
        }
        this.nothingView = null;
        //this.hiSender = null;
        this.listView = null;
        this.hiList = null;
        this.scrollView = null;
        this.mainLayout = null;

        System.gc();
    }

    private void getHiList(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getHiList");
        body.put("user_id", String.valueOf(user_id));
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // プロフィール取得用API通信のコールバック
        AsyncPost hiGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(!isDestroy){
                    // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                    listView.closeOpenedItems();
                    hiFull = clsJson2Objects.setHiList(result);
                    hiList = new ArrayList<clsFriendInfo>();

                    for (int i = 0; i < 15; i++) {
                        if (i == hiFull.size()) {
                            i = 15;
                        } else {
                            hiList.add(hiFull.get(i));
                        }
                    }
                    drawHiList(hiList);
                }
            }
        });
        // API通信のPOST処理
        hiGetter.setParams(strURL, body);
        hiGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendHi(String sendHiIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendHiIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "sendHi");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        body.put("profile_comment", profileComment);
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // プロフィール取得用API通信のコールバック
        AsyncPost hiSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawHiList(clsJson2Objects.setUserInfo(result));
                //setHiSender();
            }
        });
        // API通信のPOST処理
        hiSender.setParams(strURL, body);
        hiSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendDeBlock(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendIndex)).getFlagsFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "blockOff");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // ブロックフラグ送信用API通信のコールバック
        AsyncPost flgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                if(!isDestroy){
                    getHiList();
                }
                //setFlgSender();
            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private class ScrollUp implements Runnable {
        public void run() {
            ((ScrollView) findViewById(R.id.scroll_body)).fullScroll(View.FOCUS_UP);
        }
    }
    private void drawHiList(ArrayList<clsFriendInfo> list){

        adapter = new FriendsAdapter(hiListActivity.this);
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.hi_list_cell_height);

        hiList = list;

        if( list.size() > 0 ) {
            nothingView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            adapter.setFriendList(list);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            // 実際のListViewに反映する
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = list.size() * cellHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            adapter.notifyDataSetChanged();
            if(!keepList){
                //scrollView.fullScroll(ScrollView.FOCUS_UP);
                scrollView.post(new ScrollUp());
            }

        }else{
            nothingView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }
        //this.setHiGetter();
        keepList = false;
    }

    private void getBadgeInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getTagBadgeCount");
        body.put("user_id", String.valueOf(user_id));
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // システム情報取得用API通信のコールバック
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
                }
                //setBadgeInfoGetter();
            }
        });
        // API通信のPOST処理
        badgeInfoGetter.setParams(strURL, body);
        badgeInfoGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void clearHiBadge(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "CommitBudgeCount");
        body.put("user_id", String.valueOf(user_id));
        body.put("badge_type", "1");
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // プロフィール取得用API通信のコールバック
        AsyncPost hiSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawHiList(clsJson2Objects.setUserInfo(result));
                //setHiSender();
            }
        });
        // API通信のPOST処理(hiSenderを借りる)
        hiSender.setParams(strURL, body);
        hiSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void drawBadgeInfo(String onCount, String hiCount){
        if( !onCount.equals("0") && !onCount.equals("") ){
            ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setText(onCount);
            ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setVisibility(View.VISIBLE);
        }else{
            ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setVisibility(View.GONE);
        }
    }


    static private class FriendsAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater = null;
        ArrayList<clsFriendInfo> friendList;

        public FriendsAdapter(Context context){
            this.context = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setFriendList(ArrayList<clsFriendInfo> friendList) {
            this.friendList = friendList;
        }

        @Override
        public int getCount() {
            return friendList.size();
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
            convertView = layoutInflater.inflate(R.layout.hi_list_view_cell01,parent,false);

            // ((TextView)convertView.findViewById(R.id.cellGroupName)).setText(String.format("%s(%s)", groupList.get(position).getTagName(), groupList.get(position).getTagCount()));
            long lastHiSeconds = Long.parseLong(friendList.get(position).getTimeAgo());
            if( lastHiSeconds/3600 > 0 ){
                ((TextView) convertView.findViewById(R.id.cellHiTime)).setText(String.format(parent.getResources().getString(R.string.lc_HoursAgo), lastHiSeconds / (60*60)));
                //((TextView)convertView.findViewById(R.id.cellHiTime)).setText(String.format("%d時間前",lastHiSeconds/3600));
            }else if( lastHiSeconds/60 > 0 ){
                ((TextView) convertView.findViewById(R.id.cellHiTime)).setText(String.format(parent.getResources().getString(R.string.lc_MinitsAgo), lastHiSeconds / (60)));
                //((TextView)convertView.findViewById(R.id.cellHiTime)).setText(String.format("%d分前",lastHiSeconds/60));
            }else{
                ((TextView)convertView.findViewById(R.id.cellHiTime)).setText(parent.getResources().getString(R.string.lc_JustNow));
                //((TextView)convertView.findViewById(R.id.cellHiTime)).setText("たった今");
            }

            //((TextView) convertView.findViewById(R.id.cellHiName)).setText(friendList.get(position).getName());
            ((WrapTextView)convertView.findViewById(R.id.cellHiComment)).setText(friendList.get(position).getProfileComment());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));
            ((TextView)convertView.findViewById(R.id.cellHiddenIndexBack)).setText(Integer.toString(position));

            if( friendList.get(position).getNickName().equals("") ){
                ((TextView)convertView.findViewById(R.id.cellHiName)).setText(friendList.get(position).getName());
                ((TextView)convertView.findViewById(R.id.cellHiNameBack)).setText(friendList.get(position).getName());
            }else{
                ((TextView)convertView.findViewById(R.id.cellHiName)).setText(friendList.get(position).getNickName());
                ((TextView)convertView.findViewById(R.id.cellHiNameBack)).setText(friendList.get(position).getNickName());
            }

            //HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellHiProfileImage);
            //profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getContext());
            HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellHiProfileImage);
            profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getResources().getDimensionPixelSize(R.dimen.hi_list_cell_image), parent.getContext(),true);

            EffectImageView targetImage = (EffectImageView)convertView.findViewById(R.id.switch_icon);
            targetImage.setSwitchEffect(R.drawable.loading_hi, 2000);

            /*
            if( friendList.get(position).getNotificationOffFlg().equals("00") ){
                ImageView silentIcon = (ImageView)convertView.findViewById(R.id.cellIconSilent);
                silentIcon.setVisibility(View.GONE);
            }
            */
            convertView.findViewById(R.id.cellIconSilent).setVisibility(View.GONE);
            if( friendList.get(position).getNotificationOffFlg().equals("00") ){
                ((TextView)convertView.findViewById(R.id.cellHiName)).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                ((TextView)convertView.findViewById(R.id.cellHiNameBack)).setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                ((Button)convertView.findViewById(R.id.swipeSwitchSilent)).setText(parent.getResources().getString(R.string.listAct_doSilent));
            }else{
                ((TextView)convertView.findViewById(R.id.cellHiName)).setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.list_icon_silent,0);
                ((TextView)convertView.findViewById(R.id.cellHiNameBack)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.list_icon_silent, 0);
                ((Button)convertView.findViewById(R.id.swipeSwitchSilent)).setText(parent.getResources().getString(R.string.listAct_deSilent));
            }

            if( friendList.get(position).getBlockFlg().equals("00") ){
                convertView.findViewById(R.id.switch_icon).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellHiComment).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.GONE);
                ((Button)convertView.findViewById(R.id.swipeSwitchBlock)).setText(parent.getResources().getString(R.string.listAct_doBlock));
            }else{
                convertView.findViewById(R.id.switch_icon).setVisibility(View.INVISIBLE);
                convertView.findViewById(R.id.cellHiComment).setVisibility(View.GONE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.VISIBLE);
                ((Button)convertView.findViewById(R.id.swipeSwitchBlock)).setText(parent.getResources().getString(R.string.listAct_deBlock));
            }

            return convertView;
        }
    }

    // Hiボタン
    private String profileComment;
    private View buttonView;
    public void sendHi(View view){
        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendHiIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        if( commonFucntion.getComment(getApplication()).equals("") ){
            buttonView = view;
            //setViewにてビューを設定します。
            final EditText editView = new EditText(hiListActivity.this);
            editView.setHint(Html.fromHtml("<small><small>" + getResources().getString(R.string.homeAct_profileCommentHint) + "</small></small>"));
            editView.setSingleLine();
            alertDialogBuilder.setMessage("コメントを付けますか？\nあなたの居場所ややりたい事をコメントに(50文字以内)");
            alertDialogBuilder.setView(editView);

            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            profileComment = editView.getText().toString();
                            commonFucntion.setComment(getApplication(), profileComment);

                            ((EffectImageView)buttonView).doEffect();
                            sendHi(sendHiIndex);
                            sendHiIndex = null;
                            profileComment = null;

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
                            return;
                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else{
            ((EffectImageView)view).doEffect();
            sendHi(sendHiIndex);
            sendHiIndex = null;
        }
    }


    // ブロック解除ボタン
    public void deBlock(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendHiIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        sendDeBlock(sendHiIndex);
        sendHiIndex = null;
    }

    // セル全体
    public void openTalk(View view){
        RelativeLayout cell = (RelativeLayout)view;
        Bitmap imageBitmap = null;
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendHiIndex = hiddenText.getText().toString();
                    //break;
                }
            }else if(childview instanceof HttpImageView){
                HttpImageView friendImage = (HttpImageView)childview;
                imageBitmap = ((BitmapDrawable)friendImage.getDrawable()).getBitmap();
            }
        }

        clsFriendInfo friendInfo = hiList.get(Integer.parseInt(sendHiIndex));

        if( friendInfo.getBlockFlg().equals("00") ){
            // 一方通行で開くだけ
            onGlobal onGlobal = (onGlobal) this.getApplication();
            onGlobal.setShareData("selectFrined",friendInfo);
            onGlobal.setShareData("friendImage",imageBitmap);
            sendHiIndex = null;

            Intent intent = new Intent(getApplicationContext(),talkActivity.class);
            startActivity(intent);
        }
    }

    public void openProfile(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendHiIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        clsFriendInfo friendInfo = hiList.get(Integer.parseInt(sendHiIndex));

        onGlobal onGlobal = (onGlobal) getApplication();
        onGlobal.setShareData("selectFrined",friendInfo);
        sendHiIndex = null;

        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),friendProfileActivity.class);
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
        this.finish();
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    // 戻るリンク
    public void backToHome(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),homeActivity.class);
        startActivityForResult(intent, 0);
    }

    // セル全体
    public void closeSwipe(View view){
        listView.closeOpenedItems();
    }

    private void sendDeBlock2(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendIndex)).getFlagsFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "blockOff");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // ON送信用API通信のコールバック
        AsyncPost flgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    getHiList();
                }
            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void sendDoBlock(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendIndex)).getFlagsFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "blockOn");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // ON送信用API通信のコールバック
        AsyncPost flgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    getHiList();
                }
            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendDeSilent(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendIndex)).getFlagsFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "notifyOn");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // ON送信用API通信のコールバック
        AsyncPost flgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    getHiList();
                }
            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendDoSilent(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendIndex)).getFlagsFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "notifyOff");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // ON送信用API通信のコールバック
        AsyncPost flgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    getHiList();
                }
            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    // ブロック(解除)ボタン(裏)
    public void swipeSwitchBlock(View view){
        LinearLayout cell = (LinearLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendHiIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        if( hiList.get(Integer.parseInt(sendHiIndex)).getBlockFlg().equals("00") ){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(String.format(getResources().getString(R.string.blockConfirm), hiList.get(Integer.parseInt(sendHiIndex)).getName()));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            sendDoBlock(sendHiIndex);
                            listView.closeOpenedItems();

                            sendHiIndex = null;
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
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            sendDeBlock(sendHiIndex);
            listView.closeOpenedItems();

            sendHiIndex = null;
        }
    }

    // 非通知(解除)ボタン
    public void swipeSwitchSilent(View view){
        LinearLayout cell = (LinearLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendHiIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        if( hiList.get(Integer.parseInt(sendHiIndex)).getNotificationOffFlg().equals("00") ){
            sendDoSilent(sendHiIndex);
        }else{
            sendDeSilent(sendHiIndex);
        }
        sendHiIndex = null;
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

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getBadgeInfo();
            keepList = true;
            getHiList();
            clearHiBadge();
        }
    };
}
