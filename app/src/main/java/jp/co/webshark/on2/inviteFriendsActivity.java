package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class inviteFriendsActivity extends Activity {
    private RelativeLayout mainLayout;
    private ArrayList<clsFriendInfo> inviteList;
    private ListView listView;
    private ScrollView scrollView;
    private RelativeLayout nothingView;
    private String sendIndex;
    private AsyncPost friendsGetter;
    private AsyncPost messageGetter;
    private String inviteMessage;
    private String[] items;
    private AsyncPost checkFriendSender;
    private String strJsonArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        // 画面上のオブジェクト
        listView = (ListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);
        nothingView = (RelativeLayout) findViewById(R.id.nothingLayout);

        nothingView.setVisibility(View.GONE);

        onGlobal onGlobal = (onGlobal) this.getApplication();
        inviteList = (ArrayList<clsFriendInfo>) onGlobal.getShareData("myAddress");
        if( inviteList == null ) {
            getAddressList();
        }
        int user_id = commonFucntion.getUserID(getApplicationContext());

        strJsonArray = commonFucntion.getArrayTelJsonList(getApplicationContext());

        // プロフィール取得用API通信のコールバック
        checkFriendSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {}
        });

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "checkUserFriend");
        body.put("user_id", String.valueOf(user_id));
        body.put("json_array_tel", strJsonArray);

        // API通信のPOST処理
        checkFriendSender.setParams(strURL, body);
        checkFriendSender.execute();
        items = null;
    }

    private void getAddressList(){
        //Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        ContentResolver contentResolver = this.getContentResolver();
        // Cursor c = contentResolver.query(intent.getData(), null, null, null, null);
        String[] proj = null;
        String selection = null;
        String[] args = null;
        String sort = ContactsContract.CommonDataKinds.Contactables.PHONETIC_NAME;

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, proj, selection, args, sort);

        inviteList = new ArrayList<clsFriendInfo>();

        if (cursor.moveToFirst()) {
            String id = null;
            String name = null;

            do {
                id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name = cursor.getString(cursor.getColumnIndex("display_name"));

                clsFriendInfo cfl = new clsFriendInfo();
                cfl.setName(name);  // 名前をセット

                // 電話番号とメールアドレスをリスト化して取得
                cfl.setArrPhone(getPhoneNumber(id));
                cfl.setArrEMail(getEmail(id));

                // 表示配列に格納
                if( !cfl.getArrPhone().isEmpty() ){
                    inviteList.add(cfl);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        try{
            onGlobal onGlobal = (onGlobal) getApplicationContext();
            onGlobal.setShareData("myAddress",null);
            onGlobal.setShareData("myAddress",inviteList);
        }catch (Exception e){}
    }

    // IDから電話番号を取得
    private ArrayList<String> getPhoneNumber(String id) {
        ArrayList<String> result = new ArrayList<String>();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                , null
                , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id
                , null
                , ContactsContract.CommonDataKinds.Phone.NUMBER
        );
        if (cursor.moveToFirst()) {
            String lastInsert = "";
            do {
                String cursorData = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "");
                if( !lastInsert.equals(cursorData) ){
                    result.add(cursorData);
                    lastInsert = cursorData;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    // IDからメールアドレスを取得
    private ArrayList<String> getEmail(String id) {
        ArrayList<String> result = new ArrayList<String>();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI
                , null
                , ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + id
                , null
                , null
        );
        if (cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    @Override
    public void onResume(){
        super.onResume();

        // コールバック初期化
        setFriendsGetter();
        setMessageGetter();

        // 画面初期化時に描画する分はここで
        getFriendList();

        // 画面再描画のタイミングでAPIから取れる情報を取っておく
        getMessage();
        items = null;
    }

    // APIコールバック定義
    private void setFriendsGetter(){
        // プロフィール取得用API通信のコールバック
        friendsGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                //drawUserInfo(clsJson2Objects.setUserInfo(result));
                drawInviteList(inviteList, clsJson2Objects.setTelephoneMap(result));
                setFriendsGetter();
            }
        });
    }
    // APIコールバック定義
    private void setMessageGetter(){
        // プロフィール取得用API通信のコールバック
        messageGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                inviteMessage = clsJson2Objects.getElement(result,"article");
                setMessageGetter();
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
        friendsGetter.setParams(strURL, body);
        friendsGetter.execute();
    }

    private void getMessage(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getSMSInfo");

        // API通信のPOST処理
        messageGetter.setParams(strURL, body);
        messageGetter.execute();
    }

    private void drawInviteList(ArrayList<clsFriendInfo> list,HashMap<String,String> telephoneMap){

        FriendsAdapter adapter = new FriendsAdapter(inviteFriendsActivity.this);
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.invite_friends_cell_height);
        ArrayList<clsFriendInfo> inviteTarget = new ArrayList<clsFriendInfo>();

        if( list.size() > 0 ){
            for( clsFriendInfo friend : list){
                boolean invite = true;
                for( String telephoneNumber : friend.getArrPhone() ){
                    if( telephoneMap.containsKey(telephoneNumber) ){
                        invite = false;
                    }
                }
                if( invite ){
                    inviteTarget.add(friend);
                }
            }
        }


        if( inviteTarget.size() > 0 ) {
            inviteList = inviteTarget;
            nothingView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            adapter.setFriendList(inviteTarget);
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
            convertView = layoutInflater.inflate(R.layout.invite_friends_view_cell01,parent,false);

            ((TextView) convertView.findViewById(R.id.cellName)).setText(friendList.get(position).getName());
            ((TextView)convertView.findViewById(R.id.cellTel)).setText(friendList.get(position).getArrPhone().get(0));
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            return convertView;
        }
    }


    // 戻るリンク
    public void inviteFriendClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        finish();
    }

    public void sendSMS(View view){

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 選択メニューの設定
        if( inviteList.get(Integer.parseInt(sendIndex)).getArrEMail().size() > 0 ){
            builder.setTitle(getResources().getString(R.string.inviteFriendsAct_selectMail));
            items = (String[])inviteList.get(Integer.parseInt(sendIndex)).getArrEMail().toArray(new String[0]);
        }else{
            builder.setTitle(getResources().getString(R.string.inviteFriendsAct_selectPhone));
            items = (String[])inviteList.get(Integer.parseInt(sendIndex)).getArrPhone().toArray(new String[0]);
        }
        // 表示アイテムを指定する //
        builder.setItems(items, mItemListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    // アイテムのリスナー //
    DialogInterface.OnClickListener mItemListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {

            // 選択メニューの設定
            if( inviteList.get(Integer.parseInt(sendIndex)).getArrEMail().size() > 0 ){

                Uri uri = Uri.parse ("mailto:"+items[which]);
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.inviteFriendsAct_mailSubject));
                intent.putExtra(Intent.EXTRA_TEXT,inviteMessage);
                startActivity(intent);
            }else{
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                //電話番号の指定
                Uri smsNumber = Uri.parse ("sms:"+items[which]);
                intent.setData(smsNumber);
                //本文の指定
                intent.putExtra("sms_body", inviteMessage);
                //Activityの起動
                startActivity(Intent.createChooser(intent, "Pick a SMS App"));
            }
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        //コンテキストメニューの設定
        if( inviteList.get(Integer.parseInt(sendIndex)).getArrEMail().size() > 0 ){
            menu.setHeaderTitle(getResources().getString(R.string.inviteFriendsAct_selectMail));
            for( int i = 0 ; i < inviteList.get(Integer.parseInt(sendIndex)).getArrEMail().size() ; i++ ){
                menu.add(0, i, 0, inviteList.get(Integer.parseInt(sendIndex)).getArrEMail().get(i));
            }
        }else{
            menu.setHeaderTitle(getResources().getString(R.string.inviteFriendsAct_selectPhone));
            for( int i = 0 ; i < inviteList.get(Integer.parseInt(sendIndex)).getArrPhone().size() ; i++ ){
                menu.add(0, i, 0, inviteList.get(Integer.parseInt(sendIndex)).getArrPhone().get(i));
            }
        }
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
}
