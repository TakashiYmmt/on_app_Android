package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.webshark.on2.customViews.HttpImageView;


public class hiListActivity extends Activity {

    private RelativeLayout mainLayout;
    private ArrayList<clsFriendInfo> hiList;
    private ListView listView;
    private ScrollView scrollView;
    private AsyncPost hiGetter;
    private AsyncPost hiSender;
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
        //scrollView.scrollTo(0,0);
    }

    @Override
    public void onResume(){
        super.onResume();

        // コールバックの初期化
        this.setHiGetter();
        this.setHiSender();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getHiList();
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

    private void getHiList(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getHiList");
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        hiGetter.setParams(strURL, body);
        hiGetter.execute();
    }

    private void sendHi(String sendHiIndex){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String friendId = this.hiList.get(Integer.parseInt(sendHiIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "sendHi");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // API通信のPOST処理
        hiSender.setParams(strURL,body);
        hiSender.execute();
    }

    private void drawHiList(ArrayList<clsFriendInfo> list){

        FriendsAdapter adapter = new FriendsAdapter(hiListActivity.this);
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.hi_list_cell_height);

        this.hiList = list;
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

        this.setHiGetter();
    }

    private class FriendsAdapter extends BaseAdapter {

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
            convertView = layoutInflater.inflate(R.layout.hi_list_view_cell01,parent,false);

            // ((TextView)convertView.findViewById(R.id.cellGroupName)).setText(String.format("%s(%s)", groupList.get(position).getTagName(), groupList.get(position).getTagCount()));
            long lastHiSeconds = Long.parseLong(friendList.get(position).getTimeAgo());
            if( lastHiSeconds/3600 > 0 ){
                ((TextView)convertView.findViewById(R.id.cellHiTime)).setText(String.format("%d時間前",lastHiSeconds/3600));
            }else if( lastHiSeconds/60 > 0 ){
                ((TextView)convertView.findViewById(R.id.cellHiTime)).setText(String.format("%d分前",lastHiSeconds/60));
            }else{
                ((TextView)convertView.findViewById(R.id.cellHiTime)).setText("たった今");
            }

            ((TextView) convertView.findViewById(R.id.cellHiName)).setText(friendList.get(position).getName());
            ((TextView)convertView.findViewById(R.id.cellHiComment)).setText(friendList.get(position).getProfileComment());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellHiProfileImage);
            profImage.setImageUrl(friendList.get(position).getImageURL(), parent.getContext());

            return convertView;
        }
    }

    // Hiボタン
    public void sendHi(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 5 ){
                    TextView hiddenText = (TextView)childview;
                    sendHiIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        sendHi(sendHiIndex);
        sendHiIndex = null;
    }

    // ONリストボタン
    public void openOnList(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),onListActivity.class);
        startActivity(intent);
    }

    // homeボタン
    public void openHome(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),homeActivity.class);
        startActivity(intent);
    }

    // Hiリストボタン
    public void openHiList(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),hiListActivity.class);
        startActivity(intent);
    }

    // 友達リストボタン
    public void openFriendList(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),friendListActivity.class);
        startActivity(intent);
    }
}
