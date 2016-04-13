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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;

import jp.co.webshark.on2.customViews.EffectImageView;
import jp.co.webshark.on2.customViews.EffectImageView2;
import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.ResponceReceiver;
import jp.co.webshark.on2.customViews.UpdateReceiver;

public class friendProfileActivity extends commonActivity {

    //private AsyncPost silentFlgSender;
    //private AsyncPost blockFlgSender;
    //private AsyncPost deleteFlgSender;
    private clsFriendInfo friendInfo;
    private View eventTriggerView;
    private InputMethodManager inputMethodManager;
    private boolean openKeyBoard;
    //private AsyncPost onSender;
    //private AsyncPost hiSender;
    private EffectImageView2 hiButton;
    private TextView hiButtonText;
    private ResponceReceiver resReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        hiButton = (EffectImageView2)findViewById(R.id.hiButton);
        hiButton.setSwitchEffect(R.drawable.loading_hi, 2000);
        hiButton.setImages(R.drawable.profile_send_hi, R.drawable.loading_hi_small);
        hiButtonText = (TextView)findViewById(R.id.hiButtonText);
        hiButtonText.setText(R.string.send);

        resReceiver = new ResponceReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(resReceiver, intentFilter);

        resReceiver.registerHandler(updateHandler);
    }

    @Override
    public void onResume(){
        super.onResume();

        // 画面初期化時にAPIから取得・描画する分はここで
        onGlobal onGlobal = (onGlobal) this.getApplication();
        friendInfo = (clsFriendInfo)onGlobal.getShareData("selectFrined");

        if( !friendInfo.getFriendId().equals("") ){
            // 友達一覧以外からの時はフラグ用IDがあるので参照項目値を切り替える
            if( friendInfo.getFlagsFriendId() != null ){
                String temp = friendInfo.getFriendId();
                friendInfo.setFriendId(friendInfo.getFlagsFriendId());
                friendInfo.setFlagsFriendId(temp);
            }
            drawFriendInfo();
        }

        if( friendInfo.getBlockFlg().equals("00") ){
            ((RelativeLayout)findViewById(R.id.talk_cell)).setVisibility(View.VISIBLE);
        }else{
            ((RelativeLayout)findViewById(R.id.talk_cell)).setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroy(){
        isDestroy = true;
        super.onDestroy();

        if( resReceiver != null ){
            unregisterReceiver(resReceiver);
        }
        System.gc();
    }

    private void drawFriendInfo(){
        HttpImageView profileImageView = (HttpImageView)findViewById(R.id.profile_image);
        profileImageView.setImageUrl(friendInfo.getImageURL(), getResources().getDimensionPixelSize(R.dimen.profile_edit_image_height), getApplicationContext(), true);
        ((TextView)findViewById(R.id.cellFriendName)).setText(friendInfo.getName());
        ((TextView)findViewById(R.id.nickName)).setText(friendInfo.getNickName());

        if( friendInfo.getNotificationOffFlg().equals("00") ){
            ((Button)findViewById(R.id.silentButton)).setText(getResources().getString(R.string.friendProfAct_doSilent));
        }else{
            ((Button)findViewById(R.id.silentButton)).setText(getResources().getString(R.string.friendProfAct_deSilent));
        }

        if( friendInfo.getBlockFlg().equals("00") ){
            ((Button)findViewById(R.id.blockButton)).setText(getResources().getString(R.string.friendProfAct_doBlock));
        }else{
            ((Button)findViewById(R.id.blockButton)).setText(getResources().getString(R.string.friendProfAct_deBlock));
        }

        if( friendInfo.getOnFlg().equals("0") ){
            ((ImageView)findViewById(R.id.cellSwitchButton)).setImageResource(R.drawable.list_button_off);
        }else{
            ((ImageView)findViewById(R.id.cellSwitchButton)).setImageResource(R.drawable.list_button_on);
        }
    }

    // 非通知ボタン
    public void actSilent(View view){

        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String friendId = friendInfo.getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        if( friendInfo.getNotificationOffFlg().equals("00") ){
            body.put("entity", "notifyOff");
        }else{
            body.put("entity", "notifyOn");
        }
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // プロフィール取得用API通信のコールバック
        AsyncPost silentFlgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(!isDestroy){
                    if( clsJson2Objects.isOK(result) ){
                        if( friendInfo.getNotificationOffFlg().equals("00") ){
                            friendInfo.setNotificationOffFlg("01");
                            ((Button)findViewById(R.id.silentButton)).setText(getResources().getString(R.string.friendProfAct_deSilent));
                        }else{
                            friendInfo.setNotificationOffFlg("00");
                            ((Button)findViewById(R.id.silentButton)).setText(getResources().getString(R.string.friendProfAct_doSilent));
                        }
                    }
                }
            }
        });
        // API通信のPOST処理
        silentFlgSender.setParams(strURL, body);
        silentFlgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // ブロックボタン
    public void actBlock(View view){

        int user_id = commonFucntion.getUserID(this.getApplicationContext());
        String friendId = friendInfo.getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        if( friendInfo.getBlockFlg().equals("00") ){
            body.put("entity", "blockOn");
        }else{
            body.put("entity", "blockOff");
        }
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);

        // プロフィール取得用API通信のコールバック
        AsyncPost blockFlgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(!isDestroy){
                    if( clsJson2Objects.isOK(result) ){
                        if( friendInfo.getBlockFlg().equals("00") ){
                            friendInfo.setBlockFlg("01");
                            ((Button)findViewById(R.id.blockButton)).setText(getResources().getString(R.string.friendProfAct_deBlock));
                            ((RelativeLayout)findViewById(R.id.talk_cell)).setVisibility(View.GONE);
                        }else{
                            friendInfo.setBlockFlg("00");
                            ((Button)findViewById(R.id.blockButton)).setText(getResources().getString(R.string.friendProfAct_doBlock));
                            ((RelativeLayout)findViewById(R.id.talk_cell)).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
        // API通信のPOST処理
        blockFlgSender.setParams(strURL, body);
        blockFlgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // 削除ボタン
    public void actDelete(View view){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(getResources().getString(R.string.friendProfAct_deleteConfirm,friendInfo.getName()));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int user_id = commonFucntion.getUserID(getApplicationContext());
                        String friendId = friendInfo.getFriendId();

                        String strURL = getResources().getString(R.string.api_url);
                        HashMap<String, String> body = new HashMap<String, String>();

                        body.put("entity", "delFriend");
                        body.put("user_id", String.valueOf(user_id));
                        body.put("friend_id", friendId);

                        // プロフィール取得用API通信のコールバック
                        AsyncPost deleteFlgSender = new AsyncPost(new AsyncCallback() {
                            public void onPreExecute() {}
                            public void onProgressUpdate(int progress) {}
                            public void onCancelled() {}
                            public void onPostExecute(String result) {
                                if(!isDestroy){
                                    if( clsJson2Objects.isOK(result) ){
                                        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
                                        Intent intent = new Intent();
                                        setResult(RESULT_CANCELED, intent);
                                        intent.putExtra("isUpdate", false);
                                        finish();
                                    }
                                }
                            }
                        });
                        // API通信のPOST処理
                        deleteFlgSender.setParams(strURL, body);
                        deleteFlgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

    // 戻るリンク
    public void friendProfileClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate", false);
        finish();
    }

    // ONボタン
    private String profileComment;
    public void sendON(View view){
        String onFlg = friendInfo.getOnFlg();
        if( onFlg.equals("1") ){
            eventTriggerView = view;
            sendON();
            profileComment = null;
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            if( commonFucntion.getComment(getApplication()).equals("") ){
                eventTriggerView = view;

                //setViewにてビューを設定します。
                final EditText editView = new EditText(friendProfileActivity.this);
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

                                sendON();
                                profileComment = null;

                                ////キーボードを隠す
                                //inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                ////背景にフォーカスを移す
                                //mainLayout.requestFocus();

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
                sendON();
            }
        }
    }

    private void sendON(){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendInfo.getFriendId();
        String onFlg = friendInfo.getOnFlg();

        if( onFlg.equals("0") ){
            onFlg = "1";
        }else{
            onFlg = "0";
        }

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "SendOnPersonal");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        body.put("on_flg", onFlg);
        body.put("profile_comment", profileComment);

        // ON送信用API通信のコールバック
        AsyncPost onSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(!isDestroy){
                    // 変更したフラグに応じてトリガーのボタン画像を差し替え
                    if( eventTriggerView != null ){
                        if( friendInfo.getOnFlg().equals("0") ){
                            ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_on);
                            friendInfo.setOnFlg("1");
                        }else{
                            ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_off);
                            friendInfo.setOnFlg("0");
                        }

                        eventTriggerView = null;
                    }
                }
            }
        });

        //// API通信のPOST処理
        onSender.setParams(strURL, body);
        onSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //commonApiConnector cac = new commonApiConnector(getBaseContext());
        //cac.requestTask(body, strURL);
    }

    // Hiボタン
    public void sendHi(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        //((EffectImageView2)view).doEffect();
        //((EffectImageView2)view).setSwitchEffect(R.drawable.loading_hi_small, 2000);
        hiButton = (EffectImageView2)view;

        if( commonFucntion.getComment(getApplication()).equals("") ){
            eventTriggerView = view;

            //setViewにてビューを設定します。
            final EditText editView = new EditText(friendProfileActivity.this);
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

                            hiButton.setSwitchEffect(R.drawable.loading_hi_small, 2000);
                            hiButton.setLabelObject(hiButtonText);
                            hiButton.doEffect();

                            sendHi();

                            profileComment = null;

                            ////キーボードを隠す
                            //inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            ////背景にフォーカスを移す
                            //mainLayout.requestFocus();

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
            hiButton.setSwitchEffect(R.drawable.loading_hi_small, 2000);
            hiButton.setLabelObject(hiButtonText);
            hiButtonText.setVisibility(View.INVISIBLE);
            hiButton.doEffect();

            sendHi();

            hiButtonText.setVisibility(View.VISIBLE);
        }
    }

    private void sendHi(){
        int user_id = commonFucntion.getUserID(getApplicationContext());
        String friendId = friendInfo.getFriendId();

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "sendHiFronFL");
        body.put("user_id", String.valueOf(user_id));
        body.put("friend_id", friendId);
        body.put("profile_comment", profileComment);

        // プロフィール取得用API通信のコールバック
        AsyncPost hiSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
            }
        });
        // API通信のPOST処理
        hiSender.setParams(strURL, body);
        hiSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //commonApiConnector cac = new commonApiConnector(getBaseContext());
        //cac.requestTask(body,strURL);

    }


    // 名前を変更
    public void editNickName(View view){
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),friendProfileEditNickNameActivity.class);
        intent.putExtra("nickName", friendInfo.getNickName());
        intent.putExtra("friendId", friendInfo.getFriendId());
        startActivityForResult(intent, 0);
    }


    // トークを開く
    public void openTalk(View view){

        HttpImageView friendImage = (HttpImageView)findViewById(R.id.profile_image);
        Bitmap imageBitmap = ((BitmapDrawable)friendImage.getDrawable()).getBitmap();

        // 友達プロフィール friend_id が逆向きなので、トーク用に整える
        String temp = friendInfo.getFlagsFriendId();
        friendInfo.setFlagsFriendId(friendInfo.getFriendId());
        friendInfo.setFriendId(temp);

        // 一方通行で開くだけ
        onGlobal onGlobal = (onGlobal) this.getApplication();
        onGlobal.setShareData("selectFrined",friendInfo);
        onGlobal.setShareData("friendImage", imageBitmap);

        Intent intent = new Intent(getApplicationContext(),talkActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null && requestCode == 0 && resultCode == 0) {
            return;
        }

        if (data != null) {
            boolean isUpdate = data.getBooleanExtra("isUpdate", false);
            if (isUpdate) {
                String nick = data.getStringExtra("nickName");
                friendInfo.setNickName(nick);
                Toast.makeText(this, getResources().getString(R.string.profEditAct_profUpdate), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String request = msg.getData().get("request").toString();
            String result = msg.getData().get("result").toString();

            if(!isDestroy){
                // ONボタン
                if( request.equals("SendOnPersonal") ){
                    // 変更したフラグに応じてトリガーのボタン画像を差し替え
                    if( friendInfo.getOnFlg().equals("0") ){
                        ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_on);
                        friendInfo.setOnFlg("1");
                    }else{
                        ((ImageView)eventTriggerView).setImageResource(R.drawable.list_button_off);
                        friendInfo.setOnFlg("0");
                    }

                    eventTriggerView = null;
                }
            }

            return;
        }
    };
}
