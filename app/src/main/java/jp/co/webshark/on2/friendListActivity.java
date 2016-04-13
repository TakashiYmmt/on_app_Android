package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.webshark.on2.customViews.CustomScrollView;
import jp.co.webshark.on2.customViews.DetectableKeyboardEventLayout;
import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.ResponceReceiver;
import jp.co.webshark.on2.customViews.SwipeListView;
import jp.co.webshark.on2.customViews.UpdateReceiver;
import jp.co.webshark.on2.customViews.UrlImageView;
import jp.co.webshark.on2.customViews.WrapTextView;


public class friendListActivity extends Activity {

    private RelativeLayout mainLayout;
    private LinearLayout footerLayout;
    private ArrayList<clsFriendInfo> friendList;
    private ArrayList<clsFriendInfo> friendFull;
    private SwipeListView listView;
    private FriendsAdapter adapter;
    private RelativeLayout nothingView;
    private CustomScrollView scrollView;
    //private AsyncPost friendGetter;
    //private AsyncPost badgeInfoGetter;
    //private AsyncPost onSender;
    //private AsyncPost hiSender;
    //private AsyncPost flgSender;
    private String sendIndex;
    private View eventTriggerView;
    private String refreshOnFlg;
    private InputMethodManager inputMethodManager;
    private boolean openKeyBoard;
    private UpdateReceiver upReceiver;
    private IntentFilter intentFilter;
    private boolean isDestroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        footerLayout = (LinearLayout)findViewById(R.id.footer);

        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // 画面上のオブジェクト
        listView = (SwipeListView) findViewById(R.id.listView1);
        scrollView = (CustomScrollView) findViewById(R.id.scroll_body);
        nothingView = (RelativeLayout) findViewById(R.id.nothingLayout);

        nothingView.setVisibility(View.GONE);

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
                    openKeyBoard = false;
                    footerLayout.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = footerLayout.getLayoutParams();
                    params.height = getResources().getDimensionPixelSize(R.dimen.footer_height);
                    footerLayout.setLayoutParams(params);
                }
            }
        });

        ((TextView) findViewById(R.id.tabHiButtonBadge)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tabOnImageButtonBadge)).setVisibility(View.GONE);
        //scrollView.scrollTo(0,0);

        upReceiver = new UpdateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(upReceiver, intentFilter);

        upReceiver.registerHandler(updateHandler);
        // 戻るボタン
        //setSpannableString(this.getWindow().getDecorView());

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
        int nowCount = friendList.size();
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.friend_list_cell_height);

        if( nowCount == friendFull.size() ){
            return false;
        }

        for (int i = nowCount ; i < (nowCount + 15) ; i++) {
            if (i == friendFull.size()) {
                i = nowCount + 15;
            } else {
                friendList.add(friendFull.get(i));
            }
        }

        if( friendList.size() == friendFull.size() ){
            ((RelativeLayout)findViewById(R.id.deleteFrame)).setVisibility(View.VISIBLE);
        }else{
            ((RelativeLayout)findViewById(R.id.deleteFrame)).setVisibility(View.GONE);
        }

        // 実際のListViewに反映する
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = friendList.size() * cellHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();

        adapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public void onResume(){
        System.gc();
        super.onResume();

        // コールバックの初期化
        /*
        this.setFriendGetter();
        this.setHiSender();
        this.setOnSender();
        this.setFlgSender();
        this.setBadgeInfoGetter();
        */

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getFriendList();
        this.getBadgeInfo();

        // スワイプ状態をリセットする
        listView.closeOpenedItems();
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
    public void onDestroy(){
        isDestroy = true;
        super.onDestroy();

        // スワイプ状態をリセットする
        listView.closeOpenedItems();

        if( upReceiver != null ){
            unregisterReceiver(upReceiver);
        }
        this.nothingView = null;
        //this.friendGetter = null;
        //this.onSender = null;
        //this.hiSender = null;
        this.sendIndex = null;
        this.listView = null;
        this.friendList = null;
        this.scrollView = null;
        this.mainLayout = null;

        System.gc();
    }

    static private class FriendsAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater = null;
        ArrayList<clsFriendInfo> friendList;
        SwipeListView listView;

        public FriendsAdapter(Context context){
            this.context = context;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setFriendList(ArrayList<clsFriendInfo> friendList) {
            this.friendList = friendList;
        }
        public void setListView(SwipeListView listView) {
            this.listView = listView;
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
            convertView = layoutInflater.inflate(R.layout.frined_list_view_cell01,parent,false);

            //HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellFriendProfileImage);
            //profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getContext());
            HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellFriendProfileImage);
            profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getResources().getDimensionPixelSize(R.dimen.friend_list_cell_image), parent.getContext(), true);

            ((WrapTextView)convertView.findViewById(R.id.cellFriendComment)).setText(friendList.get(position).getProfileComment());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));
            ((TextView)convertView.findViewById(R.id.cellHiddenIndexBack)).setText(Integer.toString(position));

            EffectImageView targetImage = (EffectImageView)convertView.findViewById(R.id.cellFriendHiButton);
            targetImage.setSwitchEffect(R.drawable.loading_hi_small, 2000);

            if( friendList.get(position).getNickName().equals("") ){
                ((TextView)convertView.findViewById(R.id.cellFriendName)).setText(friendList.get(position).getName());
                ((TextView)convertView.findViewById(R.id.cellFriendNameBack)).setText(friendList.get(position).getName());
            }else{
                ((TextView)convertView.findViewById(R.id.cellFriendName)).setText(friendList.get(position).getNickName());
                ((TextView)convertView.findViewById(R.id.cellFriendNameBack)).setText(friendList.get(position).getNickName());
            }


            if( friendList.get(position).getOnFlg().equals("1") ){
                ImageView switchButton = (ImageView)convertView.findViewById(R.id.cellSwitchButton);
                switchButton.setImageResource(R.drawable.list_button_on);
            }

            ((TextView)convertView.findViewById(R.id.cellFriendName)).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            ((TextView)convertView.findViewById(R.id.cellFriendNameBack)).setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            ((ImageView)convertView.findViewById(R.id.newBadge)).setVisibility(View.GONE);
            ((Button)convertView.findViewById(R.id.swipeSwitchSilent)).setText(parent.getResources().getString(R.string.listAct_doSilent));
            if( friendList.get(position).getNotificationOffFlg().equals("01") && friendList.get(position).getNewFlg().equals("1") ){
                //((TextView)convertView.findViewById(R.id.cellFriendName)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.friend_list_new,0,R.drawable.list_icon_silent,0);
                ((TextView)convertView.findViewById(R.id.cellFriendName)).setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.list_icon_silent,0);
                ((TextView)convertView.findViewById(R.id.cellFriendNameBack)).setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.list_icon_silent,0);
                ((ImageView)convertView.findViewById(R.id.newBadge)).setVisibility(View.VISIBLE);
                ((Button)convertView.findViewById(R.id.swipeSwitchSilent)).setText(parent.getResources().getString(R.string.listAct_deSilent));
            }else if( friendList.get(position).getNotificationOffFlg().equals("01") ){
                ((TextView)convertView.findViewById(R.id.cellFriendName)).setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.list_icon_silent,0);
                ((TextView)convertView.findViewById(R.id.cellFriendNameBack)).setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.list_icon_silent,0);
                ((ImageView)convertView.findViewById(R.id.newBadge)).setVisibility(View.GONE);
                ((Button)convertView.findViewById(R.id.swipeSwitchSilent)).setText(parent.getResources().getString(R.string.listAct_deSilent));
            }else if( friendList.get(position).getNewFlg().equals("1") ){
                //((TextView)convertView.findViewById(R.id.cellFriendName)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.friend_list_new,0,0,0);
                ((TextView)convertView.findViewById(R.id.cellFriendName)).setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                ((TextView)convertView.findViewById(R.id.cellFriendNameBack)).setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                ((ImageView)convertView.findViewById(R.id.newBadge)).setVisibility(View.VISIBLE);
            }

            if( friendList.get(position).getBlockFlg().equals("00") ){
                convertView.findViewById(R.id.cellSwitchButton).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellFriendComment).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.GONE);
                ((Button)convertView.findViewById(R.id.swipeSwitchBlock)).setText(parent.getResources().getString(R.string.listAct_doBlock));
            }else{
                convertView.findViewById(R.id.cellSwitchButton).setVisibility(View.INVISIBLE);
                convertView.findViewById(R.id.cellFriendComment).setVisibility(View.GONE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.VISIBLE);
                ((Button)convertView.findViewById(R.id.swipeSwitchBlock)).setText(parent.getResources().getString(R.string.listAct_deBlock));
            }

            try {
                long onUpdateTime = Long.parseLong(friendList.get(position).getOnUpdateTime());
                if( onUpdateTime/(24*60*60) > 0 ) {
                    ((TextView) convertView.findViewById(R.id.cellLastOnTime)).setText(String.format(parent.getResources().getString(R.string.lc_DaysAgo), onUpdateTime / (24 * 60 * 60)));
                    //((TextView) convertView.findViewById(R.id.cellLastOnTime)).setText(String.format("%d日前", onUpdateTime / (24*60*60)));
                }else if( onUpdateTime/3600 > 0 ){
                    ((TextView) convertView.findViewById(R.id.cellLastOnTime)).setText(String.format(parent.getResources().getString(R.string.lc_HoursAgo), onUpdateTime / (60*60)));
                    //((TextView)convertView.findViewById(R.id.cellLastOnTime)).setText(String.format("%d時間前",onUpdateTime/3600));
                }else if( onUpdateTime/60 > 0 ){
                    ((TextView) convertView.findViewById(R.id.cellLastOnTime)).setText(String.format(parent.getResources().getString(R.string.lc_MinitsAgo), onUpdateTime / (60)));
                    //((TextView)convertView.findViewById(R.id.cellLastOnTime)).setText(String.format("%d分前",onUpdateTime/60));
                }else{
                    ((TextView)convertView.findViewById(R.id.cellLastOnTime)).setText(parent.getResources().getString(R.string.lc_JustNow));
                    //((TextView)convertView.findViewById(R.id.cellLastOnTime)).setText("たった今");
                }
            } catch (NumberFormatException e) {
               //return false;
            }
            return convertView;
        }
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

    private void getFriendList(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getFriendList");
        body.put("user_id", String.valueOf(user_id));
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // ONカウント取得用API通信のコールバック
        AsyncPost friendGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    friendFull = clsJson2Objects.setFriendList(result);
                    friendList = new ArrayList<clsFriendInfo>();
                    for (int i = 0; i < 15; i++) {
                        if (i == friendFull.size()) {
                            i = 15;
                        } else {
                            friendList.add(friendFull.get(i));
                        }
                    }
                    drawFriendList(friendList);
                }
            }
        });
        // API通信のPOST処理
        friendGetter.setParams(strURL, body);
        friendGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void drawFriendList(ArrayList<clsFriendInfo> list){

        int cellHeight = getResources().getDimensionPixelSize(R.dimen.friend_list_cell_height);
        this.friendList = list;

        if( list.size() > 0 ){
            nothingView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            if( friendList.size() == friendFull.size() ){
                ((RelativeLayout)findViewById(R.id.deleteFrame)).setVisibility(View.VISIBLE);
            }else{
                ((RelativeLayout)findViewById(R.id.deleteFrame)).setVisibility(View.GONE);
            }

            adapter = new FriendsAdapter(friendListActivity.this);
            adapter.setFriendList(list);
            adapter.setListView(listView);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            // 実際のListViewに反映する
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = list.size() * cellHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            adapter.notifyDataSetChanged();
        }else{
            nothingView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        //this.setFriendGetter();
    }

    // 閉じるリンク作成
    private void setSpannableString(View view) {

        String fullText = getResources().getString(R.string.back);
        String linkText = getResources().getString(R.string.back);

        // リンク化対象の文字列、リンク先 URL を指定する
        Map<String, String> map = new HashMap<String, String>();
        map.put(linkText, "");

        // SpannableString の取得
        SpannableString ss = createSpannableString(fullText, map);

        // SpannableString をセットし、リンクを有効化する
        TextView textView = (TextView) view.findViewById(R.id.linkTextView);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString createSpannableString(String message, Map<String, String> map) {

        SpannableString ss = new SpannableString(message);

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            int start = 0;
            int end = 0;

            // リンク化対象の文字列の start, end を算出する
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                break;
            }

            // SpannableString にクリックイベント、パラメータをセットする
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {

                    //close();
                    openHome(null);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(Color.parseColor(getResources().getString(R.string.color_code_link_green)));
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return ss;
    }

    private void sendHi(String sendHiIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendHiIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "sendHiFronFL");
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
        hiSender.setParams(strURL,body);
        hiSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    // Hiボタン
    public void sendHi(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        if( commonFucntion.getComment(getApplication()).equals("") ){
            eventTriggerView = view;

            //setViewにてビューを設定します。
            final EditText editView = new EditText(friendListActivity.this);
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

                            ((EffectImageView) eventTriggerView).doEffect();
                            sendHi(sendIndex);
                            sendIndex = null;
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
            ((EffectImageView) view).doEffect();
            sendHi(sendIndex);
            sendIndex = null;
        }
    }

    private void sendON(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendIndex)).getFriendId();
        String onFlg = friendList.get(Integer.parseInt(sendIndex)).getOnFlg();

        if( onFlg.equals("0") ){
            onFlg = "1";
        }else{
            onFlg = "0";
        }

        friendList.get(Integer.parseInt(sendIndex)).setOnFlg(onFlg);
        refreshOnFlg = onFlg;

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "SendOnPersonal");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        body.put("on_flg", onFlg);
        body.put("profile_comment", profileComment);
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // ON送信用API通信のコールバック
        AsyncPost onSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                ////getFriendList();
                //setOnSender();
                if(!isDestroy){
                    // 変更したフラグに応じてトリガーのボタン画像を差し替え
                    if( refreshOnFlg.equals("0") ){
                        ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_off);
                    }else{
                        ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_on);
                    }
                    eventTriggerView = null;
                    refreshOnFlg = null;
                }
            }
        });
        // API通信のPOST処理
        onSender.setParams(strURL, body);
        onSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void sendDeBlock(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendIndex)).getFriendId();

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
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                ////getFriendList();
                //setFlgSender();
                if(!isDestroy){
                    getFriendList();
                }

            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    // ONボタン
    private String profileComment;
    public void sendON(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        String onFlg = this.friendList.get(Integer.parseInt(sendIndex)).getOnFlg();
        if( onFlg.equals("1") ){
            eventTriggerView = view;
            sendON(sendIndex);
            sendIndex = null;
            profileComment = null;
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            if( commonFucntion.getComment(getApplication()).equals("") ){
                eventTriggerView = view;

                //setViewにてビューを設定します。
                final EditText editView = new EditText(friendListActivity.this);
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

                                sendON(sendIndex);
                                sendIndex = null;
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
                eventTriggerView = view;
                sendON(sendIndex);
                sendIndex = null;
            }
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
                    sendIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        sendDeBlock(sendIndex);
        sendIndex = null;
    }

    public void openProfile(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        clsFriendInfo friendInfo = friendList.get(Integer.parseInt(sendIndex));

        // 友達リストは friend_id が逆向きなので、友達プロフィール用に整える
        String temp = friendInfo.getFlagsFriendId();
        friendInfo.setFlagsFriendId(friendInfo.getFriendId());
        friendInfo.setFriendId(temp);

        if( friendInfo.getBlockFlg().equals("00") ){
            onGlobal onGlobal = (onGlobal) getApplication();
            onGlobal.setShareData("selectFrined",friendInfo);
            sendIndex = null;

            // 一方通行で開くだけ
            Intent intent = new Intent(getApplicationContext(),friendProfileActivity.class);
            startActivity(intent);
        }
    }


    private void close() {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        finish();
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

    // 削除済み友達リストボタン
    public void openDeletedFriendList(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),deletedFriendListActivity.class);
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
    public void openTalk(View view){
        RelativeLayout cell = (RelativeLayout)view;
        Bitmap imageBitmap = null;

        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if (i == 0) {
                    TextView hiddenText = (TextView) childview;
                    sendIndex = hiddenText.getText().toString();
                    //break;
                }
            }else if(childview instanceof RelativeLayout) {
                HttpImageView friendImage = (HttpImageView)((RelativeLayout) childview).getChildAt(0);
                imageBitmap = ((BitmapDrawable)friendImage.getDrawable()).getBitmap();
            }
        }

        clsFriendInfo friendInfo = friendList.get(Integer.parseInt(sendIndex));

        if( friendInfo.getBlockFlg().equals("00") ){
            // 友達リストは friend_id が逆向きなので、トーク用に整える
            String temp = friendInfo.getFlagsFriendId();
            friendInfo.setFlagsFriendId(friendInfo.getFriendId());
            friendInfo.setFriendId(temp);

            onGlobal onGlobal = (onGlobal) getApplication();
            onGlobal.setShareData("selectFrined",friendInfo);
            onGlobal.setShareData("friendImage",imageBitmap);
            sendIndex = null;

            Intent intent = new Intent(getApplicationContext(),talkActivity.class);
            startActivity(intent);
        }
    }


    // セル全体
    public void closeSwipe(View view){
        listView.closeOpenedItems();
    }

    private void sendDoBlock(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendIndex)).getFriendId();

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
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                ////getFriendList();
                //setFlgSender();
                if(!isDestroy){
                    getFriendList();
                }

            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendDeSilent(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendIndex)).getFriendId();

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
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                ////getFriendList();
                //setFlgSender();
                if(!isDestroy){
                    getFriendList();
                }

            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendDoSilent(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendIndex)).getFriendId();

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
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                ////getFriendList();
                //setFlgSender();
                if(!isDestroy){
                    getFriendList();
                }

            }
        });
        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    // ブロック(解除)ボタン(裏)
    public void swipeSwitchBlock(View view){
        LinearLayout cell = (LinearLayout) view.getParent();

        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    sendIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        if( friendList.get(Integer.parseInt(sendIndex)).getBlockFlg().equals("00") ){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(String.format(getResources().getString(R.string.blockConfirm), friendList.get(Integer.parseInt(sendIndex)).getName()));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            sendDoBlock(sendIndex);
                            listView.closeOpenedItems();

                            sendIndex = null;
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
            sendDeBlock(sendIndex);
            listView.closeOpenedItems();

            sendIndex = null;
        }
    }

    // 非通知(解除)ボタン
    public void swipeSwitchSilent(View view){
        LinearLayout cell = (LinearLayout) view.getParent();

        listView.closeOpenedItems();

        for (int i = 0; i < cell.getChildCount(); i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if (i == 0) {
                    TextView hiddenText = (TextView)childview;
                    sendIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        if( friendList.get(Integer.parseInt(sendIndex)).getNotificationOffFlg().equals("00") ){
            sendDoSilent(sendIndex);
        }else{
            sendDeSilent(sendIndex);
        }
        sendIndex = null;
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

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 画面が生きている時はイベントを実行する
            if(!isDestroy){
                getBadgeInfo();
            }

        }
    };
}
