package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

import jp.co.webshark.on2.customViews.HttpImageView;

public class friendProfileActivity extends Activity {

    private AsyncPost silentFlgSender;
    private AsyncPost blockFlgSender;
    private clsFriendInfo friendInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

    }

    @Override
    public void onResume(){
        super.onResume();

        // コールバックの初期化
        this.setSilentFlgSender();
        this.setBlockFlgSender();

        // 画面初期化時にAPIから取得・描画する分はここで
        onGlobal onGlobal = (onGlobal) this.getApplication();
        friendInfo = (clsFriendInfo)onGlobal.getShareData("selectFrined");

        if( !friendInfo.getFriendId().equals("") ){
            // 友達一覧以外からの時はフラグ用IDがあるので参照項目値を切り替える
            if( friendInfo.getFlagsFriendId() != null ){
                friendInfo.setFriendId(friendInfo.getFlagsFriendId());
            }
            drawFriendInfo();
        }
    }

    private void setBlockFlgSender(){
        // プロフィール取得用API通信のコールバック
        blockFlgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if( clsJson2Objects.isOK(result) ){
                    if( friendInfo.getBlockFlg().equals("00") ){
                        friendInfo.setBlockFlg("01");
                        ((Button)findViewById(R.id.blockButton)).setText(getResources().getString(R.string.friendProfAct_deBlock));
                    }else{
                        friendInfo.setBlockFlg("00");
                        ((Button)findViewById(R.id.blockButton)).setText(getResources().getString(R.string.friendProfAct_doBlock));
                    }
                }
                setBlockFlgSender();
            }
        });
    }

    private void setSilentFlgSender(){
        // プロフィール取得用API通信のコールバック
        silentFlgSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if( clsJson2Objects.isOK(result) ){
                    if( friendInfo.getNotificationOffFlg().equals("00") ){
                        friendInfo.setNotificationOffFlg("01");
                        ((Button)findViewById(R.id.silentButton)).setText(getResources().getString(R.string.friendProfAct_deSilent));
                    }else{
                        friendInfo.setNotificationOffFlg("00");
                        ((Button)findViewById(R.id.silentButton)).setText(getResources().getString(R.string.friendProfAct_doSilent));
                    }
                }
                setSilentFlgSender();
            }
        });
    }

    private void drawFriendInfo(){
        HttpImageView profileImageView = (HttpImageView)findViewById(R.id.profile_image);
        profileImageView.setImageUrl(friendInfo.getImageURL(), getResources().getDimensionPixelSize(R.dimen.profile_edit_image_height), getApplicationContext(),true);
        ((TextView)findViewById(R.id.nameTextView)).setText(friendInfo.getName());

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

        // API通信のPOST処理
        silentFlgSender.setParams(strURL, body);
        silentFlgSender.execute();
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

        // API通信のPOST処理
        blockFlgSender.setParams(strURL, body);
        blockFlgSender.execute();
    }

    // 戻るリンク
    public void friendProfileClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate", false);
        finish();
    }

}
