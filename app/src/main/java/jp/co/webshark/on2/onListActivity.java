package jp.co.webshark.on2;import android.app.Activity;import android.content.Context;import android.content.Intent;import android.os.Bundle;import android.view.LayoutInflater;import android.view.Menu;import android.view.MenuItem;import android.view.View;import android.view.ViewGroup;import android.view.inputmethod.InputMethodManager;import android.widget.BaseAdapter;import android.widget.ListView;import android.widget.RelativeLayout;import android.widget.ScrollView;import android.widget.TextView;import java.util.ArrayList;import java.util.HashMap;import jp.co.webshark.on2.customViews.HttpImageView;public class onListActivity extends Activity {    private RelativeLayout mainLayout;    private ListView listView;    private ScrollView scrollView;    private ArrayList<clsFriendInfo> onList;    private AsyncPost onGetter;    private AsyncPost hiSender;    private String sendIndex;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_on_list);        //画面全体のレイアウト        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);        // 画面上のオブジェクト        listView = (ListView) findViewById(R.id.listView1);        scrollView = (ScrollView) findViewById(R.id.scroll_body);    }    @Override    public void onResume(){        super.onResume();        // コールバックの初期化        this.setOnGetter();        this.setHiSender();        // 画面初期化時にAPIから取得・描画する分はここで        this.getOnList();    }    // APIコールバック定義    private void setOnGetter(){        // プロフィール取得用API通信のコールバック        onGetter = new AsyncPost(new AsyncCallback() {            public void onPreExecute() {}            public void onProgressUpdate(int progress) {}            public void onCancelled() {}            public void onPostExecute(String result) {                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする                drawOnList(clsJson2Objects.setOnList(result));            }        });    }    private void setHiSender(){        // プロフィール取得用API通信のコールバック        hiSender = new AsyncPost(new AsyncCallback() {            public void onPreExecute() {}            public void onProgressUpdate(int progress) {}            public void onCancelled() {}            public void onPostExecute(String result) {                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする                //drawHiList(clsJson2Objects.setUserInfo(result));                setHiSender();            }        });    }    private void getOnList(){        int user_id = commonFucntion.getUserID(this.getApplicationContext());        String strURL = getResources().getString(R.string.api_url);        HashMap<String,String> body = new HashMap<String,String>();        body.put("entity", "getOnList");        body.put("user_id", String.valueOf(user_id));        // API通信のPOST処理        onGetter.setParams(strURL, body);        onGetter.execute();    }    private void sendHi(String sendHiIndex){        int user_id = commonFucntion.getUserID(this.getApplicationContext());        String friendId = this.onList.get(Integer.parseInt(sendHiIndex)).getFriendId();        String strURL = getResources().getString(R.string.api_url);        HashMap<String,String> body = new HashMap<String,String>();        body.put("entity", "sendHi");        body.put("user_id", String.valueOf(user_id));        body.put("friend_id", friendId);        // API通信のPOST処理        hiSender.setParams(strURL, body);        hiSender.execute();    }    private void drawOnList(ArrayList<clsFriendInfo> list){        FriendsAdapter adapter = new FriendsAdapter(onListActivity.this);        int cellHeight = getResources().getDimensionPixelSize(R.dimen.on_list_cell_hight);        this.onList = list;        adapter.setFriendList(list);        listView.setAdapter(adapter);        adapter.notifyDataSetChanged();        // 実際のListViewに反映する        ViewGroup.LayoutParams params = listView.getLayoutParams();        params.height = list.size() * cellHeight;        listView.setLayoutParams(params);        listView.requestLayout();        adapter.notifyDataSetChanged();        scrollView.fullScroll(ScrollView.FOCUS_DOWN);        this.setOnGetter();    }    private class FriendsAdapter extends BaseAdapter {        Context context;        LayoutInflater layoutInflater = null;        ArrayList<clsFriendInfo> friendList;        public FriendsAdapter(Context context){            this.context = context;            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);        }        public void setFriendList(ArrayList<clsFriendInfo> friendList) {            this.friendList = friendList;        }        @Override        public int getCount() {            return friendList.size();        }        @Override        public Object getItem(int position) {            return null;        }        @Override        public long getItemId(int position) {            return 0;        }        @Override        public View getView(int position, View convertView, ViewGroup parent) {            convertView = layoutInflater.inflate(R.layout.on_list_view_cell01,parent,false);            ((TextView)convertView.findViewById(R.id.cellOnName)).setText(onList.get(position).getName());            ((TextView)convertView.findViewById(R.id.cellOnComment)).setText(onList.get(position).getProfileComment());            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));            HttpImageView profImage = (HttpImageView) convertView.findViewById(R.id.cellOnProfileImage);            profImage.setImageUrl(onList.get(position).getImageURL(), parent.getContext());            try {                long onUpdateTime = Long.parseLong(onList.get(position).getOnUpdateTime());                if( onUpdateTime/(24*60*60) > 0 ) {                    ((TextView) convertView.findViewById(R.id.cellLastOnTime)).setText(String.format("%d日前", onUpdateTime / (24*60*60)));                }else if( onUpdateTime/3600 > 0 ){                    ((TextView)convertView.findViewById(R.id.cellLastOnTime)).setText(String.format("%d時間前",onUpdateTime/3600));                }else if( onUpdateTime/60 > 0 ){                    ((TextView)convertView.findViewById(R.id.cellLastOnTime)).setText(String.format("%d分前",onUpdateTime/60));                }else{                    ((TextView)convertView.findViewById(R.id.cellLastOnTime)).setText("たった今");                }            } catch (NumberFormatException e) {                //return false;            }            return convertView;        }    }    // Hiボタン    public void sendHi(View view){        RelativeLayout cell = (RelativeLayout)view.getParent();        for (int i = 0 ; i < cell.getChildCount() ; i++) {            View childview = cell.getChildAt(i);            if (childview instanceof TextView) {                if( i == 5 ){                    TextView hiddenText = (TextView)childview;                    sendIndex = hiddenText.getText().toString();                    break;                }            }        }        sendHi(sendIndex);        sendIndex = null;    }    // ONリストボタン    public void openOnList(View view){        // 一方通行で開くだけ        Intent intent = new Intent(getApplicationContext(),onListActivity.class);        startActivity(intent);    }    // homeボタン    public void openHome(View view){        // 一方通行で開くだけ        Intent intent = new Intent(getApplicationContext(),homeActivity.class);        startActivity(intent);    }    // Hiリストボタン    public void openHiList(View view){        // 一方通行で開くだけ        Intent intent = new Intent(getApplicationContext(),hiListActivity.class);        startActivity(intent);    }    // 友達リストボタン    public void openFriendList(View view){        // 一方通行で開くだけ        Intent intent = new Intent(getApplicationContext(),friendListActivity.class);        startActivity(intent);    }}