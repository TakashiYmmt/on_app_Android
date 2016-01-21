package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import jp.co.webshark.on2.customViews.DetectableKeyboardEventLayout;
import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.UrlImageView;

public class groupEditActivity extends Activity {
    String groupId;
    String groupName;
    ImageView groupImageView;
    private AsyncPost groupMemberGetter;
    private AsyncPost groupSetter;
    private AsyncPost onSender;
    private AsyncPost hiSender;
    private AsyncPost flgSender;
    private AsyncPost groupMemberSetter;
    ArrayList<clsFriendInfo> groupMember;
    ArrayList<clsFriendInfo> friendAll;
    private ListView listView;
    private ScrollView scrollView;
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    private boolean openKeyBoard;
    private Uri mPictureUri;
    GroupMemberAdapter adapter;
    private String refreshOnFlg;
    private View eventTriggerView;
    private String sendIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        listView = (ListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);

        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");
        groupName = i.getStringExtra("groupName");

        groupImageView = (ImageView) findViewById(R.id.group_image);

        //
        if(!groupId.isEmpty()){
            EditText groupNameEdit = (EditText) findViewById(R.id.groupNameEdit);
            groupNameEdit.setText(groupName);

            Bitmap bm = commonFucntion.loadBitmapCache(getApplicationContext(), "group"+groupId+".jpg");
            if( bm != null ){
                groupImageView.setImageBitmap(bm);
            }
        }else{
            FrameLayout sepalator = (FrameLayout) findViewById(R.id.group_delete_line_frame);
            RelativeLayout cell = (RelativeLayout) findViewById(R.id.group_delete_frame);
            sepalator.setVisibility(View.GONE);
            cell.setVisibility(View.GONE);
        }

        setGroupMemberGetter();
        getGroupMember();

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
    }

    @Override
    public void onResume(){
        super.onResume();
        this.setGroupMemberGetter();
        this.setGroupMemberSetter();
        this.setGroupSetter();
        this.setOnSender();
        this.setHiSender();
        this.setFlgSender();

        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();
    }

    // APIコールバック定義
    private void setGroupMemberGetter(){
        // プロフィール取得用API通信のコールバック
        groupMemberGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                drawGroupMemberInfo(clsJson2Objects.setFriendList(result));
            }
        });
    }

    private void setGroupSetter(){
        // プロフィール取得用API通信のコールバック
        groupSetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // グループメンバー登録(更新)と画像セーブを行う
                if( clsJson2Objects.isOK(result) ){
                    if( groupId.isEmpty() ){
                        groupId = clsJson2Objects.getElement(result, "tag_id");
                    }else{
                        removeGroupMember();
                    }
                    for( int i = 0 ; i < groupMember.size() ; i++ ){
                        addGroupMember(groupMember.get(i).getFriendId());
                    }

                    Bitmap bm = ((BitmapDrawable)groupImageView.getDrawable()).getBitmap();
                    commonFucntion.createBitmapCache(getApplicationContext(), bm, "group"+groupId+".jpg");
                }
                setGroupSetter();

                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                intent.putExtra("isUpdate", false);
                finish();
            }
        });
    }

    private void setGroupMemberSetter(){
        // プロフィール取得用API通信のコールバック
        groupMemberSetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                setGroupMemberSetter();
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
                getGroupMember();
            }
        });
    }

    private void getGroupMember(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getFriendList");
        body.put("user_id", String.valueOf(user_id));
        body.put("tag_id", groupId);

        // API通信のPOST処理
        groupMemberGetter.setParams(strURL, body);
        groupMemberGetter.execute();
    }

    private void createGroup(String groupName){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "createTag");
        body.put("user_id", String.valueOf(user_id));
        body.put("name", groupName);

        // API通信のPOST処理
        groupSetter.setParams(strURL, body);
        groupSetter.execute();
    }

    private void updateGroup(String groupName){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "updateTagName");
        body.put("user_id", String.valueOf(user_id));
        body.put("tag_id", groupId);
        body.put("name", groupName);

        // API通信のPOST処理
        groupSetter.setParams(strURL, body);
        groupSetter.execute();
    }


    private void removeGroupMember(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "deleteAllFriendTag");
        body.put("tag_id", groupId);

        // API通信のPOST処理
        groupMemberSetter.setParams(strURL, body);
        groupMemberSetter.execute();
    }

    private void addGroupMember(String friend_id){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String, String>();

        body.put("entity", "setFriendTag");
        body.put("friend_id", friend_id);
        body.put("tag_id", groupId);

        // API通信のPOST処理
        AsyncPost setter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {}
        });
        setter.setParams(strURL, body);
        setter.execute();
    }

    private void removeGroup(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "deleteTag");
        body.put("user_id", String.valueOf(user_id));
        body.put("tag_id", groupId);

        // API通信のPOST処理
        groupSetter.setParams(strURL, body);
        groupSetter.execute();
    }

    private void drawGroupMemberInfo(ArrayList<clsFriendInfo> list){

        adapter = new GroupMemberAdapter(groupEditActivity.this);
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.group_cell_height);

        this.friendAll = list;

        // 選択済の友達だけ表示用配列に入れる
        groupMember = new ArrayList<clsFriendInfo>();
        for( int i = 0 ; i < friendAll.size() ; i++ ){
            if( friendAll.get(i).getSelected() ){
                groupMember.add(friendAll.get(i));
            }
        }
        //this.groupMember = list;
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

        this.setGroupMemberGetter();
    }

    // リスト部の配列連結アダプタ
    private class GroupMemberAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater = null;

        public GroupMemberAdapter(Context context){
            this.context = context;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return groupMember.size();
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
            convertView = layoutInflater.inflate(R.layout.group_view_cell02,parent,false);

            ((HttpImageView)convertView.findViewById(R.id.cellFriendProfileImage)).setImageUrl(groupMember.get(position).getImageURL(), getResources().getDimensionPixelSize(R.dimen.group_cell_height), parent.getContext(),true);
            ((TextView)convertView.findViewById(R.id.cellFriendName)).setText(groupMember.get(position).getName());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            EffectImageView targetImage = (EffectImageView)convertView.findViewById(R.id.cellFriendHiButton);
            targetImage.setSwitchEffect(R.drawable.loading_hi_small, 2000);

            if( groupMember.get(position).getOnFlg().equals("1") ){
                ImageView switchButton = (ImageView)convertView.findViewById(R.id.cellSwitchButton);
                switchButton.setImageResource(R.drawable.list_button_on);
            }

            if( groupMember.get(position).getNotificationOffFlg().equals("00") ){
                ImageView silentIcon = (ImageView)convertView.findViewById(R.id.cellIconSilent);
                silentIcon.setVisibility(View.GONE);
            }

            convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.GONE);
            /*
            if( groupMember.get(position).getBlockFlg().equals("00") ){
                convertView.findViewById(R.id.cellSwitchButton).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.GONE);
            }else{
                convertView.findViewById(R.id.cellSwitchButton).setVisibility(View.INVISIBLE);
                convertView.findViewById(R.id.cellDeBlockButton).setVisibility(View.VISIBLE);
            }
            */
            return convertView;
        }
    }

    // 保存ボタン
    String inputText;
    public void saveGroup(View view) {
        // メンバー・グループ名チェックが通ったら保存処理
        EditText groupName = (EditText) findViewById(R.id.groupNameEdit);
        SpannableStringBuilder sp = (SpannableStringBuilder)groupName.getText();
        inputText = sp.toString();
        if( groupMember.size() == 0 ){
            Toast.makeText(this, getResources().getString(R.string.groupEditAct_noMemberWarning), Toast.LENGTH_LONG).show();
            return;
        }else if( inputText.isEmpty() ){
            Toast.makeText(this, getResources().getString(R.string.groupEditAct_noTitleWarning), Toast.LENGTH_LONG).show();
            return;
        }



        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(getResources().getString(R.string.groupEditAct_saveConfirm));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // グループIDの有無で新規・更新を分岐
                        if (groupId.isEmpty()) {
                            createGroup(inputText);
                        } else {
                            updateGroup(inputText);
                        }
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

    // 削除ボタン
    public void deleteGroup(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(getResources().getString(R.string.groupEditAct_deleteConfirm));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeGroup();

                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED, intent);
                        intent.putExtra("isUpdate", false);
                        finish();
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

    // 戻るリンク
    public void groupEditClose(View view) {

        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate", false);
        finish();

    }

    // ONボタン
    public void sendON(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        String deleteIndex = "";
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
    private void sendON(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = groupMember.get(Integer.parseInt(sendIndex)).getFriendId();
        String onFlg = groupMember.get(Integer.parseInt(sendIndex)).getOnFlg();

        if( onFlg.equals("0") ){
            onFlg = "1";
        }else{
            onFlg = "0";
        }

        groupMember.get(Integer.parseInt(sendIndex)).setOnFlg(onFlg);
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
    private void sendDeBlock(String sendIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = groupMember.get(Integer.parseInt(sendIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "blockOff");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // API通信のPOST処理
        flgSender.setParams(strURL, body);
        flgSender.execute();
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
    private void sendHi(String sendHiIndex){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = groupMember.get(Integer.parseInt(sendHiIndex)).getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "sendHiFronFL");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // API通信のPOST処理
        hiSender.setParams(strURL, body);
        hiSender.execute();
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

        clsFriendInfo friendInfo = groupMember.get(Integer.parseInt(sendIndex));

        onGlobal onGlobal = (onGlobal) getApplication();
        onGlobal.setShareData("selectFrined", friendInfo);
        sendIndex = null;

        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),friendProfileActivity.class);
        startActivity(intent);
    }

    // 人から選ぶ
    public void memberList(View view){

        // 画面表示分の配列を全体配列のフラグに反映
        for( int i = 0 ; i < friendAll.size() ; i++ ){
            // 一旦フラグを伏せる
            friendAll.get(i).setSelected(false);

            for( int j = 0 ; j < groupMember.size() ; j++ ){
                if( friendAll.get(i).getFriendId().equals(groupMember.get(j).getFriendId()) ){
                    friendAll.get(i).setSelected(true);
                }
            }
        }

        onGlobal onGlobal = (onGlobal) this.getApplication();
        onGlobal.setShareData("groupMember",friendAll);

        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),groupMemberSelectActivity.class);
        startActivityForResult(intent, 0);
    }

    public void openLibrary(View view){
        try {
            // ギャラリーから選択
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);

            // カメラで撮影
            String filename = System.currentTimeMillis() + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            //mPictureUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mPictureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i2.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

            // ギャラリー選択のIntentでcreateChooser()
            Intent chooserIntent = Intent.createChooser(i, "Pick Image");
            // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { i2 });

            startActivityForResult(chooserIntent, 0);

            InputStream is = getResources().getAssets().open("image.jpg");
            Bitmap bm = BitmapFactory.decodeStream(is);
            groupImageView.setImageBitmap(bm);
        } catch (IOException e) {
            /* 例外処理 */
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if( data == null && requestCode == 0 && resultCode == 0 ){
            return;
        }

        if( data != null ){
            boolean isUpdate = data.getBooleanExtra("isUpdate",false);
            if( isUpdate ){

                onGlobal onGlobal = (onGlobal) this.getApplication();
                groupMember = (ArrayList<clsFriendInfo>)onGlobal.getShareData("groupMember");
                onGlobal.shareDataRemove("groupMember");
                adapter.notifyDataSetChanged();

                return;
            }
        }

        if (requestCode == 0) {

            if (resultCode != RESULT_OK) {
                if (mPictureUri != null) {
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            // 画像を取得
            Uri result = (data == null) ? mPictureUri : data.getData();
            //ImageView button = (ImageView) findViewById(R.id.imageButton);

            try {
                InputStream istream = null;
                Bitmap bitmap = null;

                if( data == null ){
                    istream = getContentResolver().openInputStream(mPictureUri);
                }else{
                    istream = getContentResolver().openInputStream(data.getData());
                }

                // --- 縮小処理 --- //

                // 画像サイズ情報を取得する
                BitmapFactory.Options imageOptions = new BitmapFactory.Options();
                imageOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(istream, null, imageOptions);

                istream.close();

                // もし、画像が大きかったら縮小して読み込む
                int imageSizeMax = getResources().getDimensionPixelSize(R.dimen.group_image) / 2;

                if( data == null ){
                    //File file = new File(mPictureUri.toString());
                    //istream = new FileInputStream(file);
                    istream = getContentResolver().openInputStream(mPictureUri);
                }else{
                    istream = getContentResolver().openInputStream(data.getData());
                }

                float imageScaleWidth = (float)imageOptions.outWidth / imageSizeMax;
                float imageScaleHeight = (float)imageOptions.outHeight / imageSizeMax;

                // もしも、縮小できるサイズならば、縮小して読み込む
                if (imageScaleWidth > 2 && imageScaleHeight > 2) {
                    BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();

                    // 縦横、小さい方に縮小するスケールを合わせる
                    int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));

                    // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
                    for (int i = 2; i <= imageScale; i *= 2) {
                        imageOptions2.inSampleSize = i;
                    }

                    bitmap = BitmapFactory.decodeStream(istream, null, imageOptions2);
                } else {
                    bitmap = BitmapFactory.decodeStream(istream);
                }
                istream.close();

                //groupImageView.setImageURI(result);
                groupImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // サイズ調整
            ViewGroup.LayoutParams params = groupImageView.getLayoutParams();
            // 縦幅に合わせる
            params.height = params.width;
            groupImageView.setLayoutParams(params);
        }
    }


    /**
     * EditText編集時に背景をタップしたらキーボードを閉じるようにするタッチイベントの処理
     */

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
