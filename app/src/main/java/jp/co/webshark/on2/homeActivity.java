package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.text.Html;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;

import jp.co.webshark.on2.customViews.DetectableKeyboardEventLayout;
import jp.co.webshark.on2.customViews.HttpImageView;

import static java.lang.Thread.sleep;

public class homeActivity extends Activity {

    private clsUserInfo userInfo;
    private ArrayList<clsGroupInfo> groupList;
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    private ListView listView;
    private ScrollView scrollView;
    private EditText editText;
    private AsyncPost profileSender;
    private AsyncPost profileGetter;
    private AsyncPost onCountGetter;
    private AsyncPost groupGetter;
    private AsyncPost groupHiSender;
    private AsyncPost groupOnSender;
    private String sendGroupIndex;
    private boolean openKeyBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // 画面上のオブジェクト
        listView = (ListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);
        editText = (EditText) findViewById(R.id.profileCommentEdit);

        DetectableKeyboardEventLayout root = (DetectableKeyboardEventLayout)findViewById(R.id.body);
        root.setKeyboardListener(new DetectableKeyboardEventLayout.KeyboardListener() {

            @Override
            public void onKeyboardShown() {
                //Log.d(TAG, "keyboard shown");
                openKeyBoard = true;
            }

            @Override
            public void onKeyboardHidden() {
                if( openKeyBoard ){
                    checkComment();
                    openKeyBoard = false;
                }
            }
        });
        // 実験
        ImageView logo = (ImageView) findViewById(R.id.navigationLogo);
        registerForContextMenu(logo);
    }

    // 実験
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        //コンテキストメニューの設定
        menu.setHeaderTitle("テスト用");
        //Menu.add(int groupId, int itemId, int order, CharSequence title)
        menu.add(0, 0, 0, "初期登録画面から始める");
        menu.add(0, 1, 0, "標準TABモードを使う");
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterViewCompat.AdapterContextMenuInfo info = (AdapterViewCompat.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;

        switch (item.getItemId()) {
            case 0:
                intent = new Intent(getApplicationContext(),telephoneActivity.class);
                startActivity(intent);
                return true;
            case 1:
                intent = new Intent(getApplicationContext(),home2Activity.class);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // APIコールバック定義
    private void setProfileGetter(){
        // プロフィール取得用API通信のコールバック
        profileGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                drawUserInfo(clsJson2Objects.setUserInfo(result));
            }
        });
    }
    private void setCountGetter(){
        // ONカウント取得用API通信のコールバック
        onCountGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                drawCountInfo(clsJson2Objects.setCountInfo(result));
            }
        });
    }
    private void setGroupGetter(){
        // ONカウント取得用API通信のコールバック
        groupGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                drawGroupInfo(clsJson2Objects.setGroupInfo(result));
            }
        });
    }
    private void setGroupHiSender(){
        //グループ一斉Hi送信用API通信のコールバック
        groupHiSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                drawGroupHi();
            }
        });
    }
    private void setGroupOnSender(){
        //グループ一斉Hi送信用API通信のコールバック
        groupOnSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                drawGroupOn();
            }
        });
    }
    private void setProfileSender(){
        // プロフィール情報更新API通信のコールバック
        profileSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawGroupInfo(clsJson2Objects.setGroupInfo(result));
                //drawGroupOn();
            }
        });
    }


    @Override
    public void onResume(){
        super.onResume();

        // コールバックの初期化
        this.setProfileGetter();
        this.setCountGetter();
        this.setGroupGetter();
        this.setGroupHiSender();
        this.setGroupOnSender();
        this.setProfileSender();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getUserInfo();
        this.getCountInfo();
        this.getGroupInfo();

        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

    }

    private void getUserInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getUserInfo");
        body.put("device_type", "2");
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        profileGetter.setParams(strURL, body);
        profileGetter.execute();
    }

    private void drawUserInfo(clsUserInfo userInfo){
        HttpImageView profImage = (HttpImageView) findViewById(R.id.profile_image);
        EditText profileCommentEdit = (EditText) findViewById(R.id.profileCommentEdit);

        profImage.setImageUrl(userInfo.getImageURL(), this.getApplicationContext());
        profileCommentEdit.setHint(Html.fromHtml("<small><small>" + getResources().getString(R.string.homeAct_profileCommentHint) + "</small></small>"));
        profileCommentEdit.setText(userInfo.getComment());
        setProfileGetter();
    }

    private void getCountInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getOnPersonCount");
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        onCountGetter.setParams(strURL, body);
        onCountGetter.execute();
    }

    private void drawCountInfo(clsCountInfo countInfo){
        TextView onCountText = (TextView) findViewById(R.id.onCount);
        if( countInfo.getCount().equals("0") ){
            onCountText.setText(getResources().getString(R.string.homeAct_onCountZero));
        }else{
            onCountText.setText(String.format(getResources().getString(R.string.homeAct_onCount), countInfo.getCount()));
        }

        setCountGetter();
    }

    private void getGroupInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getUserTagList");
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        groupGetter.setParams(strURL, body);
        groupGetter.execute();
    }

    private void drawGroupInfo(ArrayList<clsGroupInfo> list){

        GroupsAdapter adapter = new GroupsAdapter(homeActivity.this);
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.home_cell_hight);

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
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        this.setGroupGetter();
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
                            setProfileSender();
                        }
                    }).setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else{
            sendComment(profileComment);
            this.setProfileSender();
        }
    }

    private void sendComment(String profileComment){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "setProfileComment");
        body.put("user_id", String.valueOf(user_id));
        body.put("profile_comment", profileComment);

        // API通信のPOST処理
        groupOnSender.setParams(strURL, body);
        groupOnSender.execute();
    }

    private void sendGroupHi(String groupIndex){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String groupId = this.groupList.get(Integer.parseInt(groupIndex)).getTagId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "SendHiGroupID");
        body.put("user_id", String.valueOf(user_id));
        body.put("tag_id", groupId);

        // API通信のPOST処理
        groupHiSender.setParams(strURL,body);
        groupHiSender.execute();
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
        this.setGroupHiSender();

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

        // API通信のPOST処理
        groupOnSender.setParams(strURL,body);
        groupOnSender.execute();
    }

    private void drawGroupOn(){

        // データ再取得・描画
        this.getUserInfo();
        this.getCountInfo();
        this.getGroupInfo();

        this.setGroupOnSender();
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

    // グループON・OFFボタン
    public void groupOnOff(View view){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 5 ){
                    TextView hiddenText = (TextView)childview;
                    sendGroupIndex = hiddenText.getText().toString();
                    break;
                }
            }
        }

        sendGroupOn(sendGroupIndex);
        sendGroupIndex = null;
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
                selectedName = ((TextView) childview).getText().toString() ;
                break;
            }
        }

        alertDialogBuilder.setMessage( selectedName + "の編集を選択");
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
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

    // グループを選一括Hi
    public void groupHi(View view){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //imageViewBase = (ImageView)view;

        RelativeLayout cell = (RelativeLayout)view.getParent();
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 5 ){
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
                        sendGroupHi(sendGroupIndex);
                        //imageViewBase = null;
                        //imageViewEfect = null;
                        sendGroupIndex = null;

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

}
