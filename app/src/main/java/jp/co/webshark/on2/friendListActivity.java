package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.UrlImageView;
import jp.co.webshark.on2.customViews.WrapTextView;


public class friendListActivity extends Activity {

    private RelativeLayout mainLayout;
    private ArrayList<clsFriendInfo> friendList;
    private ListView listView;
    private RelativeLayout nothingView;
    private ScrollView scrollView;
    private AsyncPost friendGetter;
    private AsyncPost onSender;
    private AsyncPost hiSender;
    private AsyncPost flgSender;
    private String sendIndex;
    private View eventTriggerView;
    private String refreshOnFlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        // 画面上のオブジェクト
        listView = (ListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);
        nothingView = (RelativeLayout) findViewById(R.id.nothingLayout);

        nothingView.setVisibility(View.GONE);

        //scrollView.scrollTo(0,0);

        // 戻るボタン
        //setSpannableString(this.getWindow().getDecorView());
    }
    @Override
    public void onResume(){
        System.gc();
        super.onResume();

        // コールバックの初期化
        this.setFriendGetter();
        this.setHiSender();
        this.setOnSender();
        this.setFlgSender();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getFriendList();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

        this.nothingView = null;
        this.friendGetter = null;
        this.onSender = null;
        this.hiSender = null;
        this.sendIndex = null;
        this.listView = null;
        this.friendList = null;
        this.scrollView = null;
        this.mainLayout = null;

        System.gc();
    }

    private void setHiSender(){
        // プロフィール取得用API通信のコールバック
        hiSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawHiList(clsJson2Objects.setUserInfo(result));
                setHiSender();
            }
        });
    }
    private void setOnSender(){
        // ON送信用API通信のコールバック
        onSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                ////getFriendList();
                setOnSender();

                // 変更したフラグに応じてトリガーのボタン画像を差し替え
                if( refreshOnFlg.equals("0") ){
                    ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_off);
                }else{
                    ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_on);
                }
                eventTriggerView = null;
                refreshOnFlg = null;
            }
        });
    }
    private void setFlgSender(){
        // ON送信用API通信のコールバック
        flgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                ////getFriendList();
                setFlgSender();

                getFriendList();
            }
        });
    }

    static private class FriendsAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater = null;
        ArrayList<clsFriendInfo> friendList;

        public FriendsAdapter(Context context){
            this.context = context;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            convertView = layoutInflater.inflate(R.layout.frined_list_view_cell01,parent,false);

            //HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellFriendProfileImage);
            //profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getContext());
            HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellFriendProfileImage);
            profImage.setImageUrl(friendList.get(position).getImageURL(),parent.getResources().getDimensionPixelSize(R.dimen.friend_list_cell_image),parent.getContext(),true);

            ((TextView)convertView.findViewById(R.id.cellFriendName)).setText(friendList.get(position).getName());
            ((WrapTextView)convertView.findViewById(R.id.cellFriendComment)).setText(friendList.get(position).getProfileComment());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            EffectImageView targetImage = (EffectImageView)convertView.findViewById(R.id.cellFriendHiButton);
            targetImage.setSwitchEffect(R.drawable.list_button_hi_sending, 2000);

            if( friendList.get(position).getOnFlg().equals("1") ){
                ImageView switchButton = (ImageView)convertView.findViewById(R.id.cellSwitchButton);
                switchButton.setImageResource(R.drawable.list_button_on);
            }

            if( friendList.get(position).getNotificationOffFlg().equals("00") ){
                ImageView silentIcon = (ImageView)convertView.findViewById(R.id.cellIconSilent);
                silentIcon.setVisibility(View.GONE);
            }

            if( friendList.get(position).getBlockFlg().equals("00") ){
                convertView.findViewById(R.id.cellSwitchButton).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellFriendComment).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.GONE);
            }else{
                convertView.findViewById(R.id.cellSwitchButton).setVisibility(View.INVISIBLE);
                convertView.findViewById(R.id.cellFriendComment).setVisibility(View.GONE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.VISIBLE);
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

    private void setFriendGetter(){
        // ONカウント取得用API通信のコールバック
        friendGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                drawFriendList(clsJson2Objects.setFriendList(result));
            }
        });
    }

    private void getFriendList(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getFriendList");
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        friendGetter.setParams(strURL, body);
        friendGetter.execute();
    }

    private void drawFriendList(ArrayList<clsFriendInfo> list){

        int cellHeight = getResources().getDimensionPixelSize(R.dimen.friend_list_cell_height);
        this.friendList = list;

        if( list.size() > 0 ){
            nothingView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            FriendsAdapter adapter = new FriendsAdapter(friendListActivity.this);
            adapter.setFriendList(list);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            // 実際のListViewに反映する
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = list.size() * cellHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            adapter.notifyDataSetChanged();
            //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }else{
            nothingView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        this.setFriendGetter();
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

        // API通信のPOST処理
        hiSender.setParams(strURL,body);
        hiSender.execute();
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

        ((EffectImageView)view).doEffect();
        sendHi(sendIndex);
        sendIndex = null;
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

        // API通信のPOST処理
        onSender.setParams(strURL, body);
        onSender.execute();
    }

    private void sendDeBlock(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "blockOff");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.execute();
    }

    // ONボタン
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

        eventTriggerView = view;

        sendON(sendIndex);
        sendIndex = null;
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

        onGlobal onGlobal = (onGlobal) getApplication();
        onGlobal.setShareData("selectFrined",friendInfo);
        sendIndex = null;

        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),friendProfileActivity.class);
        startActivity(intent);
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
}
