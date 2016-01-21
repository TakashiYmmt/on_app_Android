package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.UrlImageView;
import jp.co.webshark.on2.customViews.WrapTextView;


public class hiListActivity extends Activity {

    private RelativeLayout mainLayout;
    private ArrayList<clsFriendInfo> hiList;
    private ListView listView;
    private ScrollView scrollView;
    private RelativeLayout nothingView;
    private AsyncPost hiGetter;
    private AsyncPost hiSender;
    private AsyncPost flgSender;
    private String sendHiIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hi_list);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        // 画面上のオブジェクト
        listView = (ListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);
        nothingView = (RelativeLayout) findViewById(R.id.nothingLayout);

        nothingView.setVisibility(View.GONE);
        //scrollView.scrollTo(0,0);
    }

    @Override
    public void onResume(){
        System.gc();
        super.onResume();

        // コールバックの初期化
        this.setHiGetter();
        this.setHiSender();
        this.setFlgSender();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getHiList();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        this.nothingView = null;
        this.hiSender = null;
        this.listView = null;
        this.hiList = null;
        this.scrollView = null;
        this.mainLayout = null;

        System.gc();
    }

    // APIコールバック定義
    private void setHiGetter(){
        // プロフィール取得用API通信のコールバック
        hiGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                drawHiList(clsJson2Objects.setHiList(result));
            }
        });
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
    private void setFlgSender(){
        // ブロックフラグ送信用API通信のコールバック
        flgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                getHiList();
                setFlgSender();
            }
        });
    }

    private void getHiList(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getHiList");
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        hiGetter.setParams(strURL, body);
        hiGetter.execute();
    }

    private void sendHi(String sendHiIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendHiIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "sendHi");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // API通信のPOST処理
        hiSender.setParams(strURL, body);
        hiSender.execute();
    }

    private void sendDeBlock(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = hiList.get(Integer.parseInt(sendIndex)).getFlagsFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "blockOff");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.execute();
    }

    private void drawHiList(ArrayList<clsFriendInfo> list){

        FriendsAdapter adapter = new FriendsAdapter(hiListActivity.this);
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
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }else{
            nothingView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }
        this.setHiGetter();
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

            ((TextView) convertView.findViewById(R.id.cellHiName)).setText(friendList.get(position).getName());
            ((WrapTextView)convertView.findViewById(R.id.cellHiComment)).setText(friendList.get(position).getProfileComment());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            //HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellHiProfileImage);
            //profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getContext());
            HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellHiProfileImage);
            profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getResources().getDimensionPixelSize(R.dimen.hi_list_cell_image), parent.getContext(),true);

            EffectImageView targetImage = (EffectImageView)convertView.findViewById(R.id.switch_icon);
            targetImage.setSwitchEffect(R.drawable.loading_hi, 2000);

            if( friendList.get(position).getNotificationOffFlg().equals("00") ){
                ImageView silentIcon = (ImageView)convertView.findViewById(R.id.cellIconSilent);
                silentIcon.setVisibility(View.GONE);
            }

            if( friendList.get(position).getBlockFlg().equals("00") ){
                convertView.findViewById(R.id.switch_icon).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellHiComment).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.GONE);
            }else{
                convertView.findViewById(R.id.switch_icon).setVisibility(View.INVISIBLE);
                convertView.findViewById(R.id.cellHiComment).setVisibility(View.GONE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    }

    // Hiボタン
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

        ((EffectImageView)view).doEffect();
        sendHi(sendHiIndex);
        sendHiIndex = null;
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
