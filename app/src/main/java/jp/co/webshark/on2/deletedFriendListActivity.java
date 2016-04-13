package jp.co.webshark.on2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
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
import jp.co.webshark.on2.customViews.ResponceReceiver;
import jp.co.webshark.on2.customViews.UpdateReceiver;
import jp.co.webshark.on2.customViews.WrapTextView;

public class deletedFriendListActivity extends Activity {

    private RelativeLayout mainLayout;
    private ArrayList<clsFriendInfo> friendList;
    private ListView listView;
    private ScrollView scrollView;
    //private AsyncPost friendGetter;
    //private AsyncPost returnSender;
    private String sendIndex;
    private View eventTriggerView;
    private boolean isDestroy = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_friend_list);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        // 画面上のオブジェクト
        listView = (ListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);

    }
    @Override
    public void onResume(){
        System.gc();
        super.onResume();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getFriendList();
    }
    @Override
    public void onDestroy(){
        isDestroy = true;
        super.onDestroy();

        //this.friendGetter = null;
        //this.returnSender = null;
        this.sendIndex = null;
        this.listView = null;
        this.friendList = null;
        this.scrollView = null;
        this.mainLayout = null;

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

    private void getFriendList(){
        int user_id = commonFucntion.getUserID(getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getDelFriendList");
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
                    drawFriendList(clsJson2Objects.setFriendList(result));
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
            scrollView.setVisibility(View.VISIBLE);

            FriendsAdapter adapter = new FriendsAdapter(deletedFriendListActivity.this);
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
            scrollView.setVisibility(View.GONE);
        }
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
            convertView = layoutInflater.inflate(R.layout.del_friend_list_cell001,parent,false);

            HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.member_image);
            profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getResources().getDimensionPixelSize(R.dimen.friend_list_cell_image), parent.getContext(), true);

            ((TextView)convertView.findViewById(R.id.cell_member_name)).setText(friendList.get(position).getName());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            return convertView;
        }
    }

    // 友達復帰ボタン
    public void returnFriend(View view){

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

        sendReturn(sendIndex);
        sendIndex = null;
    }

    private void sendReturn(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendList.get(Integer.parseInt(sendIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "backFriend");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        //new commonApiConnector(getBaseContext()).requestTask(body, strURL);

        // プロフィール取得用API通信のコールバック
        AsyncPost returnSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    getFriendList();
                }
            }
        });

        // API通信のPOST処理
        returnSender.setParams(strURL, body);
        returnSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // 戻るリンク
    public void backFriendList(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),friendListActivity.class);
        startActivityForResult(intent, 0);
    }

}
